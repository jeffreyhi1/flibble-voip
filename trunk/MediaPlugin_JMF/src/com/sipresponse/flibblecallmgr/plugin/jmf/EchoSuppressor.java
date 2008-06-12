package com.sipresponse.flibblecallmgr.plugin.jmf;

import com.sipresponse.flibblecallmgr.SpeakerLevelListener;

public class EchoSuppressor implements SpeakerLevelListener
{
    private int windowMS = 50;
    float weight = 1.0f / ((float)windowMS / 20.0f);
    float runningAvg = 10.0f;
    private float suppressionLevel;
    private boolean enabled;    
    private static EchoSuppressor instance;
    public static synchronized EchoSuppressor getInstance()
    {
        if (instance == null)
        {
            instance = new EchoSuppressor();
        }
        return instance;
    }
    
    private EchoSuppressor()
    {
        this.instance = this;
        CustomSoundOutput.addListener(this);
    }
    
    public void cleanUp()
    {
        CustomSoundOutput.removeListener(this);
    }
    
    public void setEnabled(boolean enabled)
    {
        if (enabled)
        {
            System.out.println("EchoSuppressor.setEnabled true");
        }
        else
        {
            System.out.println("EchoSuppressor.setEnabled false");
        }
        this.enabled = enabled;
    }
    public void setSuppressionLevel(float suppressionLevel)
    {
        this.suppressionLevel = suppressionLevel;
    }
    
    public void onSpeakerLevel(int level)
    {
       if (level > runningAvg)
       {
          runningAvg = level; 
       }
       else
       {
           runningAvg = (runningAvg * (1.0f - weight)) + ((float)level * weight);
       }
       
       if (this.enabled == true && 
           runningAvg > 7)
       {
           MicrophoneThread.getInstance().setOverrideGain(0);
       }
       else
       {
           MicrophoneThread.getInstance().setOverrideGain(-1);
       }
    }
}
