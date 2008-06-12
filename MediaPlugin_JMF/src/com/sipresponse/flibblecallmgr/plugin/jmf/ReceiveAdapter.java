/*******************************************************************************
 *   Copyright 2007-2008 SIP Response
 *   Copyright 2007-2008 Michael D. Cohen
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
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.media.MediaSocketManager;
import com.sipresponse.flibblecallmgr.internal.util.Signal;

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
    private Signal inputStreamSignal = new Signal();
    
    DatagramSocket dataSock;

    DatagramSocket ctrlSock;

    InetAddress addr;
    private CallManager callMgr;
    private String callHandle;
    private String lineHandle;
    
    public ReceiveAdapter(CallManager callMgr,
                          String address,
                          int port,
                          String lineHandle,
                          String callHandle)
    {
        this.callMgr = callMgr;
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
    
    
    /**
     * An inner class to implement an PushSourceStream based on UDP sockets.
     */
    class SocketInputStream extends Thread implements PushSourceStream
    {
        DatagramSocket sock;
        InetAddress addr;
        int port;
        boolean done = false;
        SourceTransferHandler sth = null;
        private boolean ignore;
        public SocketInputStream(DatagramSocket sock, InetAddress addr, int port)
        {
            
            this.sock = sock;
            this.addr = addr;
            this.port = port;
        }

        public int read(byte buffer[], int offset, int length)
        {
            DatagramPacket p= new DatagramPacket(buffer, offset, length, addr,
                    port);
            p.setAddress(addr);
            p.setPort(port);
            p.setData(buffer, offset, length);
            try
            {
                sock.receive(p);
                if (ignore == true)
                {
                    for (int i = 0; i < 160 ; i++)
                    {
                        buffer[i + 12] = 0;
                    }
                }
            }
            catch (Exception e)
            {
                return -1;
            }
            if (true == isDtmfEvent(p.getData()))
            {
                if (!isDtmfEnd(p.getData()))
                {
                    processDtmfEvent(p.getData());
                    readUntilDtmfEventEnd(sock, addr, port, buffer, offset, length, 3000);
                }
                return read(buffer, offset, length);
            }
            else if (getPayloadType(p.getData()) != 0)
            {
                return read(buffer, offset, length);
            }
                
            inputStreamSignal.notifyResponseEvent();
            return p.getLength();
        }

        public synchronized void start()
        {
            super.start();
            if (sth != null)
            {
                inputStreamSignal.notifyResponseEvent();
            }
            setPriority(Thread.MAX_PRIORITY);
        }

        public synchronized void kill()
        {
            done = true;
            inputStreamSignal.notifyResponseEvent();
        }

        public int getMinimumTransferSize()
        {
            return 2 * 1024; // twice the MTU size, just to be safe.
        }

        public void setTransferHandler(SourceTransferHandler sth)
        {
            this.sth = sth;
            inputStreamSignal.notifyResponseEvent();
        }

        // Not applicable.
        public ContentDescriptor getContentDescriptor()
        {
             return new ContentDescriptor(ContentDescriptor.RAW);
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
                try
                {
                    boolean ret = inputStreamSignal.waitForSignal(25);
                    if (false == ret)
                    {
                        Thread.sleep(1);
                        continue;
                    }
                }
                catch (InterruptedException e)
                {
                    done = true;
                    return;
                }
                if (sth != null && !done)
                {
                    sth.transferData(this);
                }
            }
        }
    }
    
    private byte getPayloadType(byte[] buffer)
    {
        // the payload type id is in the 2nd byte of the rtp
        byte payloadType = buffer[1];
        
        // do a bitwise-and to remove the first bit of this byte (the marker bit)
        payloadType &= 0x7F;
        
        return payloadType;
    }
    private boolean isDtmfEvent(byte[] buffer)
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
    
    
    private boolean isDtmfEnd(byte[] buffer)
    {
        boolean ret = false;
        
        // check the highest bit of byte 13
        int endMarkerBit = buffer[13] & 0x80;
        if (endMarkerBit != 0)
        {
            ret = true;
        }
        else
        {
            ret = false;
        }
        return ret;
    }
    
    private void readUntilDtmfEventEnd(DatagramSocket sock,
            InetAddress addr,
            int port,
            byte[] buffer,
            int offset,
            int length,
            int timeout)
    {
        long startTime = System.currentTimeMillis();
        while (true)
        {
            DatagramPacket p = new DatagramPacket(buffer, offset, length, addr,
                    port);
            try
            {
                sock.receive(p);
            }
            catch (IOException e)
            {
                return;
            }
            if (true == isDtmfEvent(p.getData()) && true == isDtmfEnd(p.getData()))
            {
                break;
            }
            long now = System.currentTimeMillis();
            if (now - startTime > timeout)
            {
                break;
            }
        }
        
    }
    
    private void processDtmfEvent(byte[] buffer)
    {
        int code = buffer[12];
        InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.MEDIA,
                EventCode.MEDIA_DTMF,
                EventReason.MEDIA_NORMAL,
                lineHandle,
                callHandle,
                new Integer(code)));
        
    }
    
}
