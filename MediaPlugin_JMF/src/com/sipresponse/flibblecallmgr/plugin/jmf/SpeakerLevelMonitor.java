package com.sipresponse.flibblecallmgr.plugin.jmf;

import java.util.Vector;

import javax.media.Buffer;

import com.sipresponse.flibblecallmgr.SpeakerLevelListener;
import com.sipresponse.flibblecallmgr.internal.media.RtpHelper;

public class SpeakerLevelMonitor extends com.sun.media.codec.audio.rc.RateCvrt
{
    private static Vector<SpeakerLevelListener> listeners = new Vector<SpeakerLevelListener>();
    private SpeakerLevelMonitor()
    {
    }

    @Override
    public int process(Buffer in, Buffer out)
    {
        int ret = super.process(in, out);
        
        if (listeners.size() < 1)
            return ret;
        int peak = 0;
        int avg = 0;
        byte[] chunk = (byte[])out.getData();
        for (int i = 0; i < chunk.length; i=i+2)
        {
            peak = Math.max(peak, Math.abs(RtpHelper.bigEndianBytesToShort(chunk, i)));
        }
        
        //System.err.println("Peak = " + peak);
        //final double level = avg /  (32768.0);
        double level = peak /  (327);
       
        level =  100.0 * Math.pow(level, 0.5) / Math.pow(100.0, 0.5);
        
        final int iLevel = (int)(level + 0.5);
        
        for (SpeakerLevelListener l : listeners)
        {
            l.onSpeakerLevel(iLevel);
        }
        return ret;
    }
    
    public static void addListener(SpeakerLevelListener listener)
    {
        listeners.add(listener);
    }

}
