package com.sipresponse.flibblecallmgr.plugin.jmf;

import java.util.Vector;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.sound.sampled.Mixer.Info;

import com.sipresponse.flibblecallmgr.internal.media.RtpHelper;

public class MicrophoneThread extends Thread
{
    private Vector<MicrophoneListener> listeners = new Vector<MicrophoneListener>();
    private static MicrophoneThread instance;
    private Object sync = new Object();
    private AudioFormat format;
    private boolean stop = false;
    private float gain = 0.5f;
    private float overrideGain = -0.01f;
    private int audioChunkSize;
    private int deviceIndex = 0;
    
    public static synchronized MicrophoneThread getInstance(int deviceIndex)
    {
        if (null == instance)
        {
            instance = new MicrophoneThread(deviceIndex);
            instance.deviceIndex = deviceIndex;
        }
        return instance;
    }
    public static synchronized MicrophoneThread getInstance()
    {
        if (null == instance)
        {
            instance = new MicrophoneThread(2);
        }
        return instance;
    }
    
    private MicrophoneThread(int deviceIndex)
    {
        this.deviceIndex = deviceIndex;
        this.setPriority(Thread.MAX_PRIORITY);
        if (System.getProperty("os.name").toUpperCase().indexOf("MAC") > -1)
        {
            format = new AudioFormat(44100, 16, 1, true, true);
            audioChunkSize = 1764;
        }
        else
        {
            format = new AudioFormat(8000, 16, 1, true, true);
            audioChunkSize = 320;
        }
        start();
    }
    public static void shutdown()
    {
        instance.stopRunning();
        instance.interrupt();
        instance = null;
    }
    public void stopRunning()
    {
        stop = true;
    }
    public void run()
    {
        byte[] b = null; 
        TargetDataLine targetDataLine = null;
        try
        {
            if (System.getProperty("os.name").toUpperCase().indexOf("MAC") > -1)
            {
                Info[] mixerInfo = AudioSystem.getMixerInfo();
                targetDataLine = AudioSystem.getTargetDataLine(format, mixerInfo[deviceIndex]);
            }
            else
            {
                targetDataLine = AudioSystem.getTargetDataLine(format);
            }
            targetDataLine.open(format, audioChunkSize * 4);
            targetDataLine.start();                

        }
        catch (LineUnavailableException e)
        {
            e.printStackTrace();
        }
        long lastTime = now(); 
        while (stop == false)
        {
            b = new byte[audioChunkSize * 6];
            int numRead = targetDataLine.read(b, 0, b.length);
            if (now() - lastTime < 18)
            {
                try
                {
                    long sleepTime = 1;
                    sleepTime = Math.max(1, 18 - (now() - lastTime));                    
                    Thread.sleep(sleepTime);
                }
                catch (InterruptedException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            lastTime = now();
            // NEED TO SMOOTH OUT AND DEJITTER HERE
            if (numRead > 0 )
            {
                float gainToUse = gain;
                if (overrideGain >= 0.0)
                {
                    gainToUse = overrideGain;
                }
                short adjusted = 0;
                byte[] adjustedBytes = new byte[2];
                for (int i = 0; i < numRead; i=i+2)
                {
                    adjusted = 
                        (short)RtpHelper.bigEndianBytesToShort(b, i);
                    adjusted = (short)(2.0f * gainToUse * (float)adjusted);
                    adjustedBytes = RtpHelper.shortToByteArray(adjusted);
                    b[i] = adjustedBytes[0];
                    b[i+1] = adjustedBytes[1];
                }
                fireBuffer(b);
            }
        }
    }
    
    public void addListener(MicrophoneListener listener)
    {
        System.out.println("MicrophoneThread.addListener: " + listener);
        synchronized (sync)
        {
            listeners.add(listener);
        }
    }
    
    public void removeListener(MicrophoneListener listener)
    {
        System.out.println("MicrophoneThread.removeListener: " + listener);
        synchronized (sync)
        {
            listeners.remove(listener);
        }
    }
    
    private void fireBuffer(byte[] buffer)
    {
        synchronized (sync)
        {
            for (MicrophoneListener l : listeners)
            {
                l.onMicrophoneRead(buffer);
            }
        }
    }
    
    public void setOverrideGain(int gain)
    {
        this.overrideGain = (float) gain / 100.0f;   
    }
    public void setGain(int gain)
    {
        this.gain = (float) gain / 100.0f;   
    }
    private long now()
    {
        return System.nanoTime() / 1000000;        
    }
}
