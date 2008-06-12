package com.sipresponse.flibblecallmgr.internal.net;

import java.net.InetAddress;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import net.java.stun4j.StunAddress;
import net.java.stun4j.client.NetworkConfigurationDiscoveryProcess;
import net.java.stun4j.client.StunDiscoveryReport;

import com.sipresponse.flibblecallmgr.internal.util.HostPort;
import com.sipresponse.flibblecallmgr.internal.util.Signal;


public class StunDiscovery
{
    private static StunDiscovery instance;
    ConcurrentHashMap<HostPort,HostPort> privateToPublicMap = new ConcurrentHashMap<HostPort,HostPort>();
    private String stunServer;
    private int stunServerPort;
    public static synchronized StunDiscovery getInstance()
    {
        if (null == instance)
        {
            instance = new StunDiscovery();
        }
        return instance;
    }
    private StunDiscovery()
    {
    }
    
    public void removeBinding(HostPort privateHostPort)
    {
        HostPort removedGuy = privateToPublicMap.remove(privateHostPort);
        if (null == removedGuy)
        {
            System.err.println("no binding removed: " + privateHostPort.toString());
            dumpBindings();
        }
        else
        {
            System.err.println("removed binding: " + privateHostPort.toString());
        }
    }
    public Signal discoverPublicIpAsync(final String ip, final int privatePort)
    {
        final Signal signal = new Signal();
        new Thread() 
        {
            public void run()
            {
                try
                {
                    discoverPublicIp(ip, privatePort, signal);
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
    
    public HostPort discoverPublicIp(String ip, int privatePort, Signal doneSignal) throws Exception
    {
        HostPort publicHostPort = null;
        if (stunServerPort < 1)
        {
            stunServerPort = 3478;
        }
        HostPort privateHostPort = new HostPort(ip, privatePort);
        // first, check in our map
        publicHostPort = privateToPublicMap.get(privateHostPort);
        if (publicHostPort != null)
        {
            return publicHostPort;
        }
        publicHostPort = new HostPort();
        try
        {
            StunAddress localAddr = new StunAddress(ip, privatePort); 


            StunAddress serverAddr = new StunAddress(stunServer, stunServerPort);
            NetworkConfigurationDiscoveryProcess addressDiscovery =
                new NetworkConfigurationDiscoveryProcess( 
                            localAddr, serverAddr);

            addressDiscovery.start();

            StunDiscoveryReport stunReport = addressDiscovery.determineAddress(); 
            
            StunAddress stunAddress = stunReport.getPublicAddress();
            
            publicHostPort.setHost(InetAddress.getByAddress(stunAddress.getAddressBytes()).getHostAddress());
            publicHostPort.setPort(stunAddress.getPort());
            privateToPublicMap.put(privateHostPort, publicHostPort);

            addressDiscovery.shutDown();
            if (null != doneSignal)
            {
                doneSignal.notifyResponseEvent();
            }
            return publicHostPort;
        }
        catch (Exception ste)
        {
            ste.printStackTrace();
            if (null != doneSignal)
            {
                doneSignal.notifyResponseEvent();
            }
            return publicHostPort;
        }
    }
    public void setStunServer(String stunServer)
    {
        this.stunServer = stunServer;
    }
    public void setStunServerPort(int stunServerPort)
    {
        this.stunServerPort = stunServerPort;
    }
    private void dumpBindings()
    {
        System.err.println("Stun hostport map:");
        Enumeration<HostPort> hostports = privateToPublicMap.keys();
        while (hostports.hasMoreElements())
        {
            HostPort privatehp = hostports.nextElement();
            HostPort publichp = privateToPublicMap.get(privatehp);
            System.out.println("\t" + privatehp + "\t" + publichp );
        }
    }

}
