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
    private String domain;
    private Signal signal = new Signal();
    
    public HostPort selectBestIpAddress(String proxyAddress,
                                        String domain,
                                        int proxyPort,
                                        int desiredLocalPort,
                                        boolean bStun)
    {
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
        this.domain = domain;
        HostPort bestHostPort = null;
        String ipAddress = null;
        Vector<String> ipAddresses = getAllIpAddresses();
     
        // For each IP, create a udp server for discovery
        for (int i = 0; i < ipAddresses.size(); i++)
        {
            HostPort hp = new HostPort(ipAddresses.get(i), desiredLocalPort);
            hostPortMap.put(ipAddresses.get(i), hp);
            hostPortVector.add(hp);
        }
        
        // send probes from all IP addresses, waiting
        // for a response on each of the discovery servers
        boolean couldSend = false;
        for (HostPort hp : hostPortVector)
        {
            // create a udp server for discovery
            sendProbe(hp, bStun);
        }
        
        // wait for the first response
        bestHostPort = waitForProbeResponse();
    
        Enumeration enumeration = serverMap.elements();
        while (enumeration.hasMoreElements())
        {
            DiscoveryUdpServer server = (DiscoveryUdpServer) enumeration.nextElement();
            server.close();
        }
            
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

    private DiscoveryUdpServer createDiscoveryServer(String localAddress,
                                        int localPort)
    {
        DiscoveryUdpServer server = new DiscoveryUdpServer(localAddress, localPort);
        if (null != server)
        {
            HostPort hp = server.createServer();
            if (null != hp)
            {
                serverMap.put(localAddress, server);
            }
            
        }
        return server;
    }
    
    private boolean sendProbe(final HostPort localHp, final boolean bStun)
    {
        new Thread()
        {
            public void run()
            {
                System.err.println("Sending Probe " + localHp.getHost() + " " + localHp.getPort());
                HostPort hpToUse = localHp;
                if (bStun == true)
                {
                    try
                    {
                        hpToUse = StunDiscovery.getInstance().discoverPublicIp(localHp.getHost(), localHp.getPort(), null);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }               
                DiscoveryUdpServer server = createDiscoveryServer(localHp.getHost(), localHp.getPort());
                if (null != server)
                {
                    String optionsMsg = 
                        "INFO sip:" + domain + " SIP/2.0\r\n" +
                        "Via: SIP/2.0/UDP " + hpToUse.getHost() + ":" + hpToUse.getPort() + ";branch=z9hg4Ka113eabcdefabcdef" + System.currentTimeMillis() + "\r\n" +
                        "CSeq: 1 INFO\r\n" +      
                        "From: Alice <sip:Alice@"+  domain +">;tag=1348\r\n" +
                        "To: Bob <sip:bob@"+  domain +">\r\n" +
                        "Contact: <sip:alice@" + server.localIp + ":" + server.socket.getLocalPort() +">\r\n" +
                        "Call-ID: 123Flibble\r\n\r\n";
                    server.send(optionsMsg);
                }
            }
        }.start();
        return true;
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
        public DatagramSocket socket;
        private HostPort hp = null;
        public String localIp;
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
