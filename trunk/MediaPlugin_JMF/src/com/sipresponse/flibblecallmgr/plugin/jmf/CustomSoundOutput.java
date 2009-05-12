package com.sipresponse.flibblecallmgr.plugin.jmf;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;

import javax.media.Buffer;
import javax.media.GainControl;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.SourceDataLine;

import com.sipresponse.flibblecallmgr.SpeakerLevelListener;
import com.sipresponse.flibblecallmgr.internal.media.RtpHelper;
import com.sun.media.Log;
import com.sun.media.renderer.audio.device.JavaSoundOutput;

public class CustomSoundOutput extends JavaSoundOutput
{
    static Object                           initSync = new Object();
    private int                             deviceIndex;
    private javax.sound.sampled.AudioFormat afmt;
    private CustomSoundRenderer             renderer;
    private FileOutputStream                fos;
    private DataOutputStream                dos;
    private final boolean bTestFileOutput = false;  // SET TO TRUE TO TEST THE SAVE-TO-FILE FEATURE

    public CustomSoundOutput(CustomSoundRenderer renderer, int deviceIndex)
    {
        super();
        this.renderer = renderer;
        this.deviceIndex = deviceIndex;
        
        if (bTestFileOutput)
        {
            try
            {
                fos = new FileOutputStream("/lastcall.raw");
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            dos = new DataOutputStream(fos);
        }
    }

    void stop()
    {
        if (bTestFileOutput)
        {
            try
            {
                fos.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    private static Vector<SpeakerLevelListener> listeners = new Vector<SpeakerLevelListener>();

    public static void addListener(SpeakerLevelListener listener)
    {
        synchronized (listeners)
        {
            if (!listeners.contains(listener))
            {
                listeners.add(listener);
            }
        }
    }

    public static void removeListener(SpeakerLevelListener listener)
    {
        synchronized (listeners)
        {
            listeners.remove(listener);
        }
    }

    private static int count;

    @Override
    public int write(byte[] out, int offset, int len)
    {
        int ret = super.write(out, offset, len);

        if (bTestFileOutput)
        {
            try
            {
                dos.write(out, offset, len);
            }
            catch (IOException ioe)
            {
                System.out.println("IO error: " + ioe);
            }
        }
        count++;
        if (listeners.size() > 0 && (count % 5) == 0)
        {
            int peak = 0;
            int avg = 0;
            byte[] chunk = out;
            for (int i = 0; i < chunk.length; i = i + 2)
            {
                if (isBigEndian())
                {
                    peak = Math.max(peak, Math.abs(RtpHelper
                            .bigEndianBytesToShort(chunk, i)));
                }
                else
                {
                    peak = Math.max(peak, Math.abs(RtpHelper
                            .littleEndianBytesToShort(chunk, i)));
                }
            }

            double gain = 0.0;
            if (gc != null)
            {
                gain = 2.0f * renderer.getGainLevel();
            }

            // System.err.println("Peak = " + peak);
            // final double level = avg / (32768.0);
            double level = peak / (327);

            level = 100.0 * Math.pow(level, 0.5) / Math.pow(100.0, 0.5);

            final int iLevel = (int) (((double) level * gain) + 0.5);

            for (SpeakerLevelListener l : listeners)
            {
                l.onSpeakerLevel(iLevel);
            }
        }
        return ret;
    }

    public boolean initialize(javax.media.format.AudioFormat format, int bufSize)
    {
        synchronized (initSync)
        {
            DataLine.Info info;

            afmt = convertFormat(format);

            info = new DataLine.Info(SourceDataLine.class, afmt, bufSize);

            try
            {

                if (!AudioSystem.isLineSupported(info))
                {
                    Log.warning("DataLine not supported: " + format);
                    return false;
                }
                Mixer.Info mixerInfo = null;
                mixerInfo = AudioSystem.getMixerInfo()[deviceIndex];
                // dataLine = AudioSystem.getSourceDataLine(afmt, mixerInfo);
                dataLine = (SourceDataLine) AudioSystem.getLine(info);
                System.err.println("FORMAT = " + afmt.toString());
                dataLine.open(afmt, bufSize);

            }
            catch (Exception e)
            {
                Log.warning("Cannot open audio device: " + e);
                return false;
            }

            this.format = format;
            this.bufSize = bufSize;

            if (dataLine == null)
            {
                Log.warning("JavaSound unsupported format: " + format);
                return false;
            }

            try
            {
                gc = (FloatControl) dataLine
                        .getControl(FloatControl.Type.MASTER_GAIN);
                mc = (BooleanControl) dataLine
                        .getControl(BooleanControl.Type.MUTE);
            }
            catch (Exception e)
            {
                Log.warning("JavaSound: No gain control");
            }

            try
            {
                rc = (FloatControl) dataLine
                        .getControl(FloatControl.Type.SAMPLE_RATE);
            }
            catch (Exception e)
            {
                Log.warning("JavaSound: No rate control");
            }

            return true;
        }
    }

    public boolean isBigEndian()
    {
        boolean ret = true;

        if (!afmt.isBigEndian())
        {
            ret = false;
        }
        return ret;
    }

}
