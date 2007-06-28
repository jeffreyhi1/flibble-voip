/*******************************************************************************
 *   Copyright 2007 SIP Response
 *   Copyright 2007 Michael D. Cohen
 *
 *      mike _AT_ sipresponse.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.sipresponse.flibblecallmgr.plugin.jmf;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.BitSet;

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.RTPConnector;
import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.media.MediaSocketManager;

public class ReceiveAdapter implements RTPConnector
{

    private DatagramSocket rtpSocket;
    private DatagramSocket rtcpSocket;
//    private SocketOutputStream rtpOutputStream;
//    private SocketOutputStream rtcpOutputStream;

    private SocketInputStream dataInStrm;
    private SocketInputStream ctrlInStrm = null;
    private MediaSocketManager socketMgr;
    private String address;
    private int port;
    
    DatagramSocket dataSock;

    DatagramSocket ctrlSock;

    InetAddress addr;

    
    public ReceiveAdapter(CallManager callMgr,
                          String address,
                          int port)
    {
        this.address = address;
        this.port = port;
        socketMgr = InternalCallManager.getInstance().getMediaSocketManager(callMgr);
        rtpSocket = socketMgr.getSocket(port);
        rtcpSocket = socketMgr.getSocket(port+1);
    }
    
    public void close()
    {
        socketMgr.removeSocket(port);
    }

    /**
     * Returns an input stream to receive the RTCP data.
     */
    public PushSourceStream getControlInputStream() throws IOException
    {
        if (ctrlInStrm == null)
        {
            if (null == ctrlSock)
            {
                ctrlSock = this.socketMgr.getSocket(port+1);
            }
            ctrlInStrm = new SocketInputStream(ctrlSock, addr, port + 1);
            ctrlInStrm.start();
        }
        return ctrlInStrm;
    }

    public OutputDataStream getControlOutputStream() throws IOException
    {
        return null;
    }

    /**
     * Returns an input stream to receive the RTP data.
     */
    public PushSourceStream getDataInputStream() throws IOException
    {
        if (dataInStrm == null)
        {
            if (null == dataSock)
            {
                dataSock = this.socketMgr.getSocket(port);
            }
            dataInStrm = new SocketInputStream(dataSock, addr, port);
            dataInStrm.start();
        }
        return dataInStrm;
    }

    public OutputDataStream getDataOutputStream() throws IOException
    {
        return null;
    }

    public double getRTCPBandwidthFraction()
    {
        return -1;
    }

    public double getRTCPSenderBandwidthFraction()
    {
        return -1;
    }

    public int getReceiveBufferSize()
    {
        try
        {
            return rtpSocket.getReceiveBufferSize();
        }
        catch (Exception e)
        {
            return -1;
        }
    }

    public int getSendBufferSize()
    {
        return 0;
    }

    public void setReceiveBufferSize(int size) throws IOException
    {
        rtpSocket.setReceiveBufferSize(size);
    }

    public void setSendBufferSize(int arg0) throws IOException
    {
    }
    
    class SocketOutputStream implements OutputDataStream
    {
        DatagramSocket sock;
        InetAddress addr;
        int port;

        public SocketOutputStream(DatagramSocket sock, InetAddress addr, int port)
        {
            this.sock = sock;
            this.addr = addr;
            this.port = port;
        }

        public int write(byte data[], int offset, int len)
        {
            try
            {
                sock.send(new DatagramPacket(data, offset, len, addr, port));
            }
            catch (Exception e)
            {
                return -1;
            }
            return len;
        }
    }
    /**
     * An inner class to implement an PushSourceStream based on UDP sockets.
     */
    class SocketInputStream extends Thread implements PushSourceStream
    {

        DatagramSocket sock;

        InetAddress addr;

        int port;

        boolean done = false;

        boolean dataRead = false;

        SourceTransferHandler sth = null;

        public SocketInputStream(DatagramSocket sock, InetAddress addr, int port)
        {
            this.sock = sock;
            this.addr = addr;
            this.port = port;
        }

        public int read(byte buffer[], int offset, int length)
        {
            DatagramPacket p = new DatagramPacket(buffer, offset, length, addr,
                    port);
            try
            {
                sock.receive(p);
            }
            catch (IOException e)
            {
                return -1;
            }
            if (true == checkForDtmf(p.getData()))
            {
                processDtmfEvent(p.getData());
                return read(buffer, offset, length);
            }
            synchronized (this)
            {
                dataRead = true;
                notify();
            }
            return p.getLength();
        }

        public synchronized void start()
        {
            super.start();
            if (sth != null)
            {
                dataRead = true;
                notify();
            }
        }

        public synchronized void kill()
        {
            done = true;
            notify();
        }

        public int getMinimumTransferSize()
        {
            return 2 * 1024; // twice the MTU size, just to be safe.
        }

        public synchronized void setTransferHandler(SourceTransferHandler sth)
        {
            this.sth = sth;
            dataRead = true;
            notify();
        }

        // Not applicable.
        public ContentDescriptor getContentDescriptor()
        {
            return null;
        }

        // Not applicable.
        public long getContentLength()
        {
            return LENGTH_UNKNOWN;
        }

        // Not applicable.
        public boolean endOfStream()
        {
            return false;
        }

        // Not applicable.
        public Object[] getControls()
        {
            return new Object[0];
        }

        // Not applicable.
        public Object getControl(String type)
        {
            return null;
        }

        /**
         * Loop and notify the transfer handler of new data.
         */
        public void run()
        {
            while (!done)
            {
                synchronized (this)
                {
                    while (!dataRead && !done)
                    {
                        try
                        {
                            wait();
                        }
                        catch (InterruptedException e)
                        {
                        }
                    }
                    dataRead = false;
                }

                if (sth != null && !done)
                {
                    sth.transferData(this);
                }
            }
        }
    }
    
    private boolean checkForDtmf(byte[] buffer)
    {
        boolean ret = false;
        
        // assuming that this is an RTP packet, get the payload id
        // if the payload id is set to DTMF, then this is a DTMF event
       
        // the payload type id is in the 2nd byte of the rtp
        byte payloadType = buffer[1];
        
        // do a bitwise-and to remove the first bit of this byte (the marker bit)
        payloadType &= 0x7F;
        
        if (payloadType == 101)  // TODO - although commonly used, 101 is not THE
                                 //        DTMF payload type.  The payload type is
                                 //        "dynamic" and negotiated in the SDP
        {
            ret = true;
        }
        return ret;
    }
    
    private void processDtmfEvent(byte[] buffer)
    {
        
    }
    
}
