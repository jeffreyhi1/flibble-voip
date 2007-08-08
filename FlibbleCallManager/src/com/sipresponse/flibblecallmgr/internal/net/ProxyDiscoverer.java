package com.sipresponse.flibblecallmgr.internal.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Vector;

import com.sipresponse.flibblecallmgr.internal.util.HostPort;

public class ProxyDiscoverer
{
    public HostPort selectBestIpAddress(String proxyAddress, int proxyPort)
    {
        HostPort bestHostPort = null;
        String ipAddress = null;
        Vector<String> ipAddresses = getAllIpAddresses();
        
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
    
    private class DiscoveryUdpServer
    {
        public DiscoveryUdpServer(String localIp,
                String proxyAddress,
                int proxyPort )
        {
            
        }
    }
}
