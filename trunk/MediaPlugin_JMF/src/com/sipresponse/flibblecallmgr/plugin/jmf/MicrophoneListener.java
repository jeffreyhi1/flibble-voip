package com.sipresponse.flibblecallmgr.plugin.jmf;

public interface MicrophoneListener
{
    public void onMicrophoneRead(byte[] chunk);
}
