package com.sipresponse.flibblecallmgr.internal.util;

public class HostPort
{
    private String host;

    private int port;

    public HostPort()
    {

    }

    public HostPort(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    /*
     * public static byte[] stringAddressToByteArray(String s) { byte[] addr =
     * new byte[4];
     * 
     * return addr; }
     */
    @Override
    public int hashCode()
    {
        return new String(host + port).hashCode();
    }

    @Override
    public boolean equals(Object other)
    {
        HostPort otherHostPort = (HostPort)other;
        boolean bRet = false;
        if (host.equals(otherHostPort.host) &&
            port == otherHostPort.port)
        {
            bRet = true;
        }
        return bRet;
    }
    
    public String toString()
    {
        return host + ":" + port;
    }
}
