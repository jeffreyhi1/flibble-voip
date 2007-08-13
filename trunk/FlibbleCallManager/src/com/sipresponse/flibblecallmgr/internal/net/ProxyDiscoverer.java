package com.sipresponse.flibblecallmgr.internal.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.sipresponse.flibblecallmgr.internal.util.HostPort;
import com.sipresponse.flibblecallmgr.internal.util.Signal;

public class ProxyDiscoverer
{
    private ConcurrentHashMap<String,DiscoveryUdpServer> serverMap = 
        new ConcurrentHashMap<String,DiscoveryUdpServer>();
    private ConcurrentHashMap<String,HostPort> hostPortMap = 
        new ConcurrentHashMap<String,HostPort>();
    private Vector<HostPort> hostPortVector =
        new Vector<HostPort>();
    private String proxyAddress;
    private int proxyPort;
    private Signal signal = new Signal();
    
    public HostPort selectBestIpAddress(String proxyAddress,
                                        int proxyPort,
                                        int desiredLocalPort)
    {
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
        HostPort bestHostPort = null;
        String ipAddress = null;
        Vector<String> ipAddresses = getAllIpAddresses();
        
        // For each IP, create a udp server for discovery
        for (int i = 0; i < ipAddresses.size(); i++)
        {
            // create a udp server for discovery
            createDiscoveryServer(ipAddresses.get(i), desiredLocalPort);
        }
        
        // send probes from all IP addresses, waiting
        // for a response on each of the discovery servers
        for (HostPort hp : hostPortVector)
        {
            // create a udp server for discovery
            sendProbe(hp);
        }
        
        // wait for the first response
        bestHostPort = waitForProbeResponse();
        
        return bestHostPort;
    }
    
    private Vector<String> getAllIpAddresses()
    {
        Vector<String> addressVector = new Vector<String>();
        
        try
        {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (true == interfaces.hasMoreElements())
            {
                NetworkInterface i = interfaces.nextElement();
                Enumeration<InetAddress> addresses = i.getInetAddresses();
                while (true == addresses.hasMoreElements())
                {
                    InetAddress address = addresses.nextElement();
                    if (!address.isAnyLocalAddress() &&
                        !address.isLoopbackAddress() &&
                        !address.isLinkLocalAddress() &&
                        !address.getHostAddress().equals("127.0.0.1")  &&
                        !(address instanceof Inet6Address))
                    {
                        addressVector.add(address.getHostAddress());
                    }
                }
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return addressVector;
    }

    private void createDiscoveryServer(String localAddress,
                                        int localPort)
    {
        DiscoveryUdpServer server = new DiscoveryUdpServer(localAddress, localPort);
        if (null != server)
        {
            HostPort hp = server.createServer();
            if (null != hp)
            {
                serverMap.put(localAddress, server);
                hostPortMap.put(localAddress, hp);
                hostPortVector.add(hp);
            }
            
        }
    }
    
    private void sendProbe(HostPort hp)
    {
        String optionsMsg = 
            "OPTIONS sip:bob@" + proxyAddress + ":" + proxyPort + " SIP/2.0\r\n" +
            "Via: SIP/2.0/UDP " + hp.getHost() + ":" + hp.getPort() + "\r\n" +
            "From: Alice <sip:alice@wonderland.com>;tag=abc\r\n" + 
            "To: Bob <sip:bob@"+  proxyAddress + ":" + proxyPort +">\r\n" +
            "Call-ID: 123Flibble\r\n\r\n";
        DiscoveryUdpServer server = serverMap.get(hp.getHost());
        if (null != server)
        {
            server.send(optionsMsg);
        }
    }
    
    private HostPort waitForProbeResponse()
    {
        HostPort hp = null;
        
        if (true == signal.waitForSignal(4000))
        {
            hp = (HostPort)signal.getData();
        }
        return hp;
    }
    
    private class DiscoveryUdpServer extends Thread
    {
        private DatagramSocket socket;
        private HostPort hp = null;
        private String localIp;
        private int requestedPort;
        private boolean started = false;
        private boolean stop = false;
        public DiscoveryUdpServer(String localIp,
                int localPort)
        {
            this.localIp = localIp;
            this.requestedPort = localPort;
        }
        
        public HostPort createServer()
        {
            for (int i= 0; i < 20; i++)
            {
                try
                {
                    socket = new DatagramSocket(new InetSocketAddress(localIp, requestedPort));
                }
                catch (SocketException e)
                {
                    requestedPort++;
                    continue;
                }
                hp = new HostPort();
                hp.setHost(localIp);
                hp.setPort(requestedPort);
                start();
                while (started == false)
                {
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e)
                    {
                    }
                }
                break;
            }
            return hp;
        }
        
        public boolean send(String msg)
        {
            boolean ret = false;
            if (null != socket)
            {
                DatagramPacket p = null;
                try
                {
                    p = new DatagramPacket(msg.getBytes(),
                            0,
                            msg.length(),
                            InetAddress.getByName(proxyAddress),
                            proxyPort);
                    socket.send(p);
                    ret = true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            return ret;
        }

        public void close()
        {
            stop = true;
            if (null != socket)
            {
                socket.close();
            }
        }
        
        public void run()
        {
            byte[] response = new byte[16384];
            started = true;
            while (stop == false)
            {
                try
                {
                    DatagramPacket p = new DatagramPacket(response,
                                                          0,
                                                          response.length,
                                                          new InetSocketAddress(hp.getHost(), hp.getPort()));
                    socket.receive(p);
                    if (p.getLength() > 0)
                    {
                        synchronized (signal)
                        {
                            signal.setData(hp);
                            signal.notifyResponseEvent();
                            socket.close();
                            stop = true;
                        }
                    }
                }
                catch (Exception e)
                {
                    break;
                }
            }
        }
    }
    
}
