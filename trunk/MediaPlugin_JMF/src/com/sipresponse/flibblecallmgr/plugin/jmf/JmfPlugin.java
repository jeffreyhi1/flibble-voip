package com.sipresponse.flibblecallmgr.plugin.jmf;

import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;

public class JmfPlugin extends FlibbleMediaProvider
{
    private Receiver receiver;
    private Transmitter transmitter;
    
    @Override
    public void initializeRtpReceive(String address, int port)
    {
    }

    @Override
    public void startRtpReceive(String address, int port)
    {
    }

    @Override
    public void stopRtpReceive(String address, int port)
    {
    }
    
    @Override
    public void initializeRtpSend(String destIp, int destPort)
    {
    }
    
    @Override
    public void startRtpSend(String destIp, int destPort)
    {
    }

    @Override
    public void stopRtpSend(String destIp, int destPort)
    {
    }

}
