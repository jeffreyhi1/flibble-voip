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
package com.sipresponse.flibblecallmgr.internal.media;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ConcurrentHashMap;

import com.sipresponse.flibblecallmgr.CallManager;

public class MediaSocketManager
{
    private CallManager callMgr;

    private int portRangeStart;

    private int portRangeEnd;

    private boolean[] inUse;

    private ConcurrentHashMap<Integer, DatagramSocket> socketMap = new ConcurrentHashMap<Integer, DatagramSocket>();

    private Object sync = new Object();

    public MediaSocketManager(CallManager callMgr, int startPortRange,
            int endPortRange)
    {
        this.callMgr = callMgr;
        this.portRangeStart = startPortRange;
        this.portRangeEnd = endPortRange;
        inUse = new boolean[(endPortRange - startPortRange) / 2];
    }

    public int getNextAvailablePort()
    {
        int next = -1;
        synchronized (sync)
        {
            for (int i = 0; i < (portRangeEnd - portRangeStart) / 2; i=i+2)
            {
                if (false == inUse[i])
                {
                    DatagramSocket rtp = null;
                    DatagramSocket rtcp = null;
                    try
                    {
                        rtp = new DatagramSocket(portRangeStart + i );
                        rtcp = new DatagramSocket(portRangeStart + i + 1);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                   
                    if (isReceivingData(rtp))
                    {
                        continue;
                    }
                    if (null != rtp && null != rtcp)
                    {
                        next = i + portRangeStart;
                        inUse[i] = true;
                        rtp.close();
                        rtcp.close();
                        break;
                    }
                }
            }
        }
        return next;
    }

    public DatagramSocket getSocket(int port)
    {
        DatagramSocket socket = null;
        synchronized (sync)
        {
            socket = socketMap.get(new Integer(port));
            if (null == socket)
            {
                try
                {
                    socket = new DatagramSocket(port);
                    socketMap.put(new Integer(port), socket);
                }
                catch (SocketException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return socket;
    }

    public void removeSocket(int port)
    {
        DatagramSocket socket = socketMap.get(new Integer(port));
        if (null != socket)
        {
            removeSocket(socket, port);
        }
    }

    public void removeSocket(DatagramSocket socket)
    {
        removeSocket(socket, socket.getLocalPort());
    }

    private void removeSocket(DatagramSocket socket, int port)
    {
        synchronized (sync)
        {
            if (null != socket)
            {
                try
                {
                    socket.close();
                }
                catch (Exception e)
                {
                }
            }
            if (port >= portRangeStart && port <= portRangeEnd)
            {
                if (port % 2 == 0)
                {
                    removeSocket(port + 1); // remove the RTCP socket as well
                    inUse[port - portRangeStart] = false;
                }
            }
            socketMap.remove(new Integer(port));
        }
    }
    
    private boolean isReceivingData(final DatagramSocket rtp)
    {
        boolean receivingData = false;
        SocketChecker checker = new SocketChecker(rtp);
        checker.start();
        try
        {
            Thread.sleep(100);
        }
        catch (InterruptedException e)
        {
        }
        receivingData = checker.receivingData;
        return receivingData;
    }
    
    private class SocketChecker extends Thread
    {
        private boolean receivingData;
        private DatagramSocket socket;
        public SocketChecker(DatagramSocket socket)
        {
            this.socket = socket;
        }
        public void run()
        {
            byte[] buff = new byte[2048];
            DatagramPacket p = new DatagramPacket(buff, buff.length);
            try
            {
                socket.receive(p);
            }
            catch (IOException e)
            {
                return;
            }
            receivingData = true;
            return;
        }
        public boolean isReceivingData()
        {
            return receivingData;
        }
    }


}
