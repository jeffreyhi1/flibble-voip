package com.sipresponse.flibblecallmgr.plugin.jmf;

import java.io.IOException;

import javax.media.Buffer;
import javax.media.Control;
import javax.media.Format;
import javax.media.format.AudioFormat;
import javax.media.protocol.BufferTransferHandler;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.PushBufferStream;

import com.sipresponse.flibblecallmgr.internal.util.Signal;

public class MicrophoneStream implements PushBufferStream, MicrophoneListener//, Runnable
{
    protected Control[] controls = new Control[0];
    private MicrophoneThread micThread;
    private AudioFormat format;
    private byte[] data;
    private BufferTransferHandler transferHandler;
    private Signal readSignal = new Signal();
    private boolean started = false;
    private int audioChunkSize;

    public MicrophoneStream()
    {
        if (System.getProperty("os.name").toUpperCase().indexOf("MAC") > -1)
        {
            audioChunkSize = 1764;
            format = new AudioFormat(AudioFormat.LINEAR, 44100.0, 16, 1, 1, 1);
        }
        else
        {
            audioChunkSize = 320;
            format = new AudioFormat(AudioFormat.LINEAR, 8000.0, 16, 1, 1, 1);
        }
        data = new byte[audioChunkSize * 6];

        micThread = MicrophoneThread.getInstance();
        micThread.addListener(this);
    }

    public Format getFormat()
    {
        return format;
    }

    public void read(Buffer buffer) throws IOException
    {
        buffer.setData(data);
        buffer.setFormat(format);
        buffer.setDuration(5 * 20000);
        buffer.setLength(data.length);
        readSignal.notifyResponseEvent();
    }

    public void setTransferHandler(BufferTransferHandler handler)
    {
        System.out.println("MicrophoneStream.setTransferHanlder");
        this.transferHandler = handler;
    }

    public boolean endOfStream()
    {
        return false;
    }

    public ContentDescriptor getContentDescriptor()
    {
        return new ContentDescriptor(ContentDescriptor.RAW);
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

    public void stop()
    {
        started = false;
        micThread.removeListener(this);
    }

    public void onMicrophoneRead(byte[] chunk)
    {
        System.arraycopy(chunk, 0, data, 0, chunk.length);
        if (transferHandler != null)
            transferHandler.transferData(this);
        //readSignal.waitForSignal(1000);
    }

    public long getContentLength()
    {
        return LENGTH_UNKNOWN;
    }

}
