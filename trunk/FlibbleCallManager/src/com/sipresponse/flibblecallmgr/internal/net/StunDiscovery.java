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
 *  
 * CODE IN THIS FILE IS A WORK DERIVED FROM "JSTUN": 
 * 
 * JSTUN Copyright (c) 2005 Thomas King <king@t-king.de> - All rights
 * reserved.
 * 
 * JSTUN is licensed under either the GNU Public License (GPL),
 * or the Apache 2.0 license. Copies of both license agreements are
 * included in the JSTUN distribution.
 */
package com.sipresponse.flibblecallmgr.internal.net;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;

import com.sipresponse.flibblecallmgr.internal.util.Signal;

import de.javawi.jstun.attribute.ChangeRequest;
import de.javawi.jstun.attribute.ChangedAddress;
import de.javawi.jstun.attribute.ErrorCode;
import de.javawi.jstun.attribute.MappedAddress;
import de.javawi.jstun.attribute.MessageAttribute;
import de.javawi.jstun.header.MessageHeader;
import de.javawi.jstun.test.DiscoveryInfo;

public class StunDiscovery
{
    private int timeoutInitValue = 8000;
    private boolean nodeNatted;
    private String publicIp;
    
    public Signal discoverPublicIpAsync(final String stunServer, final String ip, final int port)
    {
        final Signal signal = new Signal();
        new Thread() 
        {
            public void run()
            {
                try
                {
                    discoverPublicIp(stunServer, ip, port, signal);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    signal.notifyResponseEvent();
                }
            }
        }.start();
        return signal;
    }
    
    public boolean discoverPublicIp(String stunServer, String ip, int port, Signal doneSignal) throws Exception
    {
        if (port < 1)
        {
            port = 3478;
        }
        DiscoveryInfo di = new DiscoveryInfo(InetAddress.getByName(ip));
        DatagramSocket sock;
        int timeSinceFirstTransmission = 0;
        int timeout = timeoutInitValue;
        while (true)
        {
            try
            {
                sock = new DatagramSocket(new InetSocketAddress(ip, 0));
                sock.setReuseAddress(true);
                sock.connect(InetAddress.getByName(stunServer), port);
                sock.setSoTimeout(timeout);

                MessageHeader sendMH = new MessageHeader(
                        MessageHeader.MessageHeaderType.BindingRequest);
                sendMH.generateTransactionID();

                ChangeRequest changeRequest = new ChangeRequest();
                sendMH.addMessageAttribute(changeRequest);

                byte[] data = sendMH.getBytes();
                DatagramPacket send = new DatagramPacket(data, data.length);
                sock.send(send);
                // Binding Request sent

                MessageHeader receiveMH = new MessageHeader();
                while (!(receiveMH.equalTransactionID(sendMH)))
                {
                    DatagramPacket receive = new DatagramPacket(new byte[200],
                            200);
                    sock.receive(receive);
                    receiveMH = MessageHeader.parseHeader(receive.getData());
                }

                MappedAddress ma = (MappedAddress) receiveMH
                        .getMessageAttribute(MessageAttribute.MessageAttributeType.MappedAddress);
                ChangedAddress ca = (ChangedAddress) receiveMH
                        .getMessageAttribute(MessageAttribute.MessageAttributeType.ChangedAddress);
                ErrorCode ec = (ErrorCode) receiveMH
                        .getMessageAttribute(MessageAttribute.MessageAttributeType.ErrorCode);
                if (ec != null)
                {
                    di.setError(ec.getResponseCode(), ec.getReason());
                    // Message header contains errorcode message attribute.
                    if (null != doneSignal)
                    {
                        doneSignal.notifyResponseEvent();
                    }
                    return false;
                }
                if ((ma == null) || (ca == null))
                {
                    di
                            .setError(
                                    700,
                                    "The server is sending incomplete response (Mapped Address and Changed Address message attributes are missing). The client should not retry.");
                    // Response does not contain a mapped address or changed
                    // address message attribute.
                    if (null != doneSignal)
                    {
                        doneSignal.notifyResponseEvent();
                    }
                    return false;
                }
                else
                {
                    di.setPublicIP(ma.getAddress().getInetAddress());
                    if ((ma.getPort() == sock.getLocalPort())
                            && (ma.getAddress().getInetAddress().equals(sock
                                    .getLocalAddress())))
                    {
                        // Node is not natted.
                        nodeNatted = false;
                    }
                    else
                    {
                        publicIp = di.getPublicIP().getHostAddress();
                        nodeNatted = true;
                        // Node is natted
                    }
                    if (null != doneSignal)
                    {
                        doneSignal.notifyResponseEvent();
                    }
                    return true;
                }
            }
            catch (SocketTimeoutException ste)
            {
                if (timeSinceFirstTransmission < 7900)
                {
                    //  Socket timeout while receiving the response.
                    timeSinceFirstTransmission += timeout;
                    int timeoutAddValue = (timeSinceFirstTransmission * 2);
                    if (timeoutAddValue > 1600)
                        timeoutAddValue = 1600;
                    timeout = timeoutAddValue;
                }
                else
                {
                    // node is not capable of udp communication
                    // Socket timeout while receiving the response. Maximum retry limit exceed. Give up.
                    di.setBlockedUDP();
                    // Node is not capable of udp communication.
                    if (null != doneSignal)
                    {
                        doneSignal.notifyResponseEvent();
                    }
                    return false;
                }
            }
        }
    }

    public boolean isNodeNatted()
    {
        return nodeNatted;
    }

    public String getPublicIp()
    {
        return publicIp;
    }

    public void setNodeNatted(boolean nodeNatted)
    {
        this.nodeNatted = nodeNatted;
    }
}
