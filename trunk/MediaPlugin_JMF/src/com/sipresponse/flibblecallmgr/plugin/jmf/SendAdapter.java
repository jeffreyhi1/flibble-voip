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

import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushSourceStream;
import javax.media.protocol.SourceTransferHandler;
import javax.media.rtp.OutputDataStream;
import javax.media.rtp.RTPConnector;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.media.MediaSocketManager;
import com.sipresponse.flibblecallmgr.internal.media.RtpHelper;
import com.sipresponse.flibblecallmgr.plugin.jmf.ReceiveAdapter.SocketOutputStream;

public class SendAdapter implements RTPConnector
{
    private MediaSocketManager socketMgr;
    private String address;
    private int srcPort;
    private int destPort;
    private SocketOutputStream rtpOutputStream;
    private SocketOutputStream rtcpOutputStream;
    private DatagramSocket rtpSocket;
    private DatagramSocket rtcpSocket;
    private int seqNo;
    private int dtmfStartSeqNo;
    private int timestamp;
    private int ssid;
    
    public SendAdapter(CallManager callMgr,
            String address,
            int srcPort,
            int destPort)
    {
        this.address = address;
        this.srcPort = srcPort;
        this.destPort = destPort;
        socketMgr = InternalCallManager.getInstance().getMediaSocketManager(callMgr);
    }
    
    public void close()
    {
    }

    public PushSourceStream getControlInputStream() throws IOException
    {
        return new DummyInputStream();
    }

    public OutputDataStream getControlOutputStream() throws IOException
    {
        if (rtcpOutputStream == null)
        {
            if (null == rtcpSocket)
            {
                rtcpSocket = this.socketMgr.getSocket(srcPort+1);
            }
            rtcpOutputStream = new SocketOutputStream(rtcpSocket, InetAddress.getByName(address), destPort+1);
        }
        return rtcpOutputStream;
    }

    public PushSourceStream getDataInputStream() throws IOException
    {
        return new DummyInputStream();
    }

    public OutputDataStream getDataOutputStream() throws IOException
    {
        if (rtpOutputStream == null)
        {
            if (null == rtpSocket)
            {
                rtpSocket = socketMgr.getSocket(srcPort);
            }
            rtpOutputStream = new SocketOutputStream(rtpSocket, InetAddress.getByName(address), destPort);
        }
        return rtpOutputStream;
    }

    public double getRTCPBandwidthFraction()
    {
        return 0;
    }

    public double getRTCPSenderBandwidthFraction()
    {
        return 0;
    }

    public int getReceiveBufferSize()
    {
        return 0;
    }

    public int getSendBufferSize()
    {
        return 0;
    }

    public void setReceiveBufferSize(int arg0) throws IOException
    {
    }

    public void setSendBufferSize(int arg0) throws IOException
    {
    }

    public void sendDtmf(int code)
    {
        if (rtpOutputStream != null)
        {
            rtpOutputStream.sendDtmf(code);
        }
    }
    
    /**
     * An inner class to implement an OutputDataStream based on UDP sockets.
     */
    class SocketOutputStream implements OutputDataStream
    {

        DatagramSocket sock;
        InetAddress addr;
        int port;
        private Object sync = new Object();

        public SocketOutputStream(DatagramSocket sock, InetAddress addr, int port)
        {
            this.sock = sock;
            this.addr = addr;
            this.port = port;
        }
        
        public void sendDtmf(int code)
        {
            synchronized (sync)
            {
                dtmfStartSeqNo = seqNo + 1;
                byte dtmfEvent[] = null;
                dtmfEvent = RtpHelper.createDtmfEvent(101,
                        seqNo + 1, 
                        timestamp + 160,
                        ssid,
                        code,
                        true,
                        false);
                
                
                try
                {
                    sock.send(new DatagramPacket(dtmfEvent, 0, dtmfEvent.length, addr, port));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                
                dtmfEvent = RtpHelper.createDtmfEvent(101,
                        seqNo + 2, 
                        timestamp + 160,
                        ssid,
                        code,
                        false,
                        true);  
                try
                {
                    sock.send(new DatagramPacket(dtmfEvent, 0, dtmfEvent.length, addr, port));
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
                                
            }
        }

        public int write(byte[] data, int offset, int len)
        {
            synchronized (sync)
            {
                try
                {
                    seqNo = RtpHelper.getSeqNo(data);
                    if (seqNo <= (dtmfStartSeqNo + 1))
                    {
                        return len;
                    }
                            
                    timestamp = RtpHelper.getTimestamp(data);
                    ssid = RtpHelper.getSSID(data);
                    sock.send(new DatagramPacket(data, offset, len, addr, port));
                    Thread.sleep(15);  //smooth out the inter-arrival jitter
                }
                catch (Exception e)
                {
                    return -1;
                }
            }
            return len;
        }
    }
    
    public class DummyInputStream implements PushSourceStream
    {

     public int getMinimumTransferSize()
     {
         return 0;
     }

     public int read(byte[] arg0, int arg1, int arg2) throws IOException
     {
         // TODO Auto-generated method stub
         return -1;
     }

     public void setTransferHandler(SourceTransferHandler arg0)
     {
         
     }

     public boolean endOfStream()
     {
         return true;
     }

     public ContentDescriptor getContentDescriptor()
     {
         return null;
     }

     public long getContentLength()
     {
         return 0;
     }

     public Object getControl(String arg0)
     {
         return null;
     }

     public Object[] getControls()
     {
         return null;
     }

    }    

}
