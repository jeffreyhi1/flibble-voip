package com.sipresponse.flibblecallmgr.plugin.jmf;

import java.io.IOException;

import javax.media.Time;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferStream;

import com.sun.media.protocol.BasicPushBufferDataSource;

public class MicrophoneSource extends BasicPushBufferDataSource
{

    private MicrophoneStream stream;
    private boolean connected = false;
    
    public MicrophoneSource()
    {
        stream = new MicrophoneStream();
    }
    @Override
    public PushBufferStream[] getStreams()
    {
        return new PushBufferStream[] { stream };
    }

    @Override
    public void connect() throws IOException
    {
        connected = true;
    }

    @Override
    public void disconnect()
    {
        connected = false;
    }

    @Override
    public String getContentType()
    {
        return ContentDescriptor.RAW;
    }


    public Object[] getControls()
    {
        return controls;
    }

    public Object getControl(String controlType)
    {
        try
        {
            Class cls = Class.forName(controlType);
            Object cs[] = getControls();
            for (int i = 0; i < cs.length; i++)
            {
                if (cls.isInstance(cs[i]))
                    return cs[i];
            }
            return null;

        }
        catch (Exception e)
        {
            return null;
        }
    }
    
    @Override
    public void start() throws IOException
    {
        started = true;
    }

    @Override
    public void stop() throws IOException
    {
        if (started && connected)
        {
//            stream.stop();
        }
        started = false;
    }
    
    public void stopStream()
    {
        stream.stop();
    }
    
    public Time getDuration()
    {
        return new Time(5 * 20000L);
    }

}
