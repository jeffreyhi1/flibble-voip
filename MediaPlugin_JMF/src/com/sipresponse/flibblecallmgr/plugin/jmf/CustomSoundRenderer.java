package com.sipresponse.flibblecallmgr.plugin.jmf;

import java.util.Vector;

import javax.media.Buffer;
import javax.media.Format;
import javax.media.PlugInManager;
import javax.media.format.AudioFormat;

import com.sipresponse.flibblecallmgr.SpeakerLevelListener;
import com.sipresponse.flibblecallmgr.internal.media.RtpHelper;
import com.sun.media.renderer.audio.JavaSoundRenderer;
import com.sun.media.renderer.audio.device.AudioOutput;

public class CustomSoundRenderer extends JavaSoundRenderer
{
    private CustomSoundOutput customSoundOutput;

    int deviceIndex;

    private static boolean registered = false;

    public CustomSoundRenderer(int device)
    {
        super();
        if (false == registered)
        {
            registered = true;

            Format ulawFormat;
            Format linearFormat;
            ulawFormat = new javax.media.format.AudioFormat(AudioFormat.ULAW);
            linearFormat = new javax.media.format.AudioFormat(
                    AudioFormat.LINEAR);
            Format[] supportedFormats = new javax.media.Format[2];
            supportedFormats[0] = linearFormat;
            supportedFormats[1] = ulawFormat;

            PlugInManager
                    .addPlugIn(
                            "com.sipresponse.flibblecallmgr.plugin.jmf.CustomSoundRenderer",
                            supportedFormats,
                            new AudioFormat[0],
                            PlugInManager.RENDERER);

        }
        this.deviceIndex = device;
    }

    protected AudioOutput createDevice(javax.media.format.AudioFormat format)
    {
        customSoundOutput = new CustomSoundOutput(this, deviceIndex);
        return customSoundOutput;
    }
    
    public double getGainLevel()
    {
        double gain = 0.0;
        if (null != gainControl)
        {
            gain = gainControl.getLevel();
        }
        return gain;
    }

}
