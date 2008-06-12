/*******************************************************************************
 *   Copyright 2007-2008 SIP Response
 *   Copyright 2007-2008 Michael D. Cohen
 *
 *      mike _AT_ sipresponse.com
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package com.sipresponse.flibblecallmgr.plugin.jmf;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Vector;

import javax.media.Codec;
import javax.media.Controller;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Format;
import javax.media.IncompatibleSourceException;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoProcessorException;
import javax.media.Processor;
import javax.media.Time;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.ContentDescriptor;
import javax.media.protocol.DataSource;
import javax.media.protocol.PushBufferDataSource;
import javax.media.protocol.PushBufferStream;
import javax.media.rtp.RTPManager;
import javax.media.rtp.SendStream;
import javax.media.rtp.rtcp.SourceDescription;

import com.ibm.media.codec.audio.PCMToPCM;
import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.MediaSourceType;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;
import com.sun.media.codec.audio.rc.RateCvrt;

public class Transmitter
{
    private CallManager callMgr;

    private String callHandle;

    private String destIp;

    private int destPort;

    private int srcPort;

    private Processor processor = null;

    private RTPManager rtpMgr;

    private DataSource dataOutput = null;

    private MediaSourceType mediaSourceType;

    private String mediaFilename;

    private boolean loop;

    private DataSource ds;
    private MicrophoneSource micSource;
    private SendStream sendStream;
    private FlibbleMediaProvider mediaProvider;
    private SendAdapter sendAdapter;
    private Call[] otherCalls;
    
    public Transmitter(FlibbleMediaProvider mediaProvider,
            CallManager callMgr,
            String callHandle,
            String destIp,
            int destPort,
            int srcPort,
            MediaSourceType mediaSourceType,
            String mediaFilename,
            boolean loop,
            Call[] otherCalls)
    {
        this.mediaProvider = mediaProvider;
        this.callMgr = callMgr;
        this.callHandle = callHandle;
        this.destIp = destIp;
        this.destPort = destPort;
        this.srcPort = srcPort;
        this.mediaSourceType = mediaSourceType;
        this.mediaFilename = mediaFilename;
        this.loop = loop;
        this.otherCalls = otherCalls;
        start();
    }

    private synchronized String start()
    {
        String result;

        // Create a processor for the specified media locator
        // and program it to output JPEG/RTP
        result = createProcessor();
        if (result != null)
            return result;

        // Create an RTP session to transmit the output of the
        // processor to the specified IP address and port no.
        result = createTransmitter();
        if (result != null)
        {
            processor.close();
            processor = null;
        }
        return result;
    }

    /**
     * Stops the transmission if already started
     */
    public void stop()
    {
//        new Thread()
//        {
//            public void run()
//            {
                synchronized (this)
                {
                   sendAdapter.close();
                    if (processor != null)
                    {
                        
                        processor.stop();
                        processor.close();
                        
                        processor = null;
                        rtpMgr.removeTargets("");
                        rtpMgr.dispose();
                    }
                }
//            }
//        }.start();
    }

    public void sendDtmf(int dtmfCode)
    {
        if (null != sendAdapter)
        {
            sendAdapter.sendDtmf(dtmfCode);
        }
    }

    
    private DataSource createMergingDataSource(DataSource mainSource)
    {
        DataSource mergedDataSource = null;
        Vector<DataSource> vDataSources = new Vector<DataSource>();
        DataSource[] dataSources = null;
        int count = 0;
        for (int i = 0; i < otherCalls.length; i++)
        {
           if (null != otherCalls[i] &&
               !callHandle.equals(otherCalls[i].getHandle()) &&
               otherCalls[i].isConnected())
           {
               JmfPlugin mediaProvider = (JmfPlugin)otherCalls[i].getMediaProvider();
               if (null != mediaProvider)
               {
                   count++;
                   vDataSources.add(mediaProvider.getIncomingDataSource());
               }
           }
        }
        if (count > 0)
        {
            vDataSources.add(mainSource);
            dataSources = new DataSource[vDataSources.size()];
            vDataSources.copyInto(dataSources);
            try
            {
                mergedDataSource = Manager.createMergingDataSource(dataSources);
            }
            catch (IncompatibleSourceException e)
            {
                e.printStackTrace();
            }
        }
        if (null != mergedDataSource)
        {
            try
            {
                mergedDataSource.connect();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            try
            {
                mergedDataSource.start();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return mergedDataSource;
    }
        
    private String createProcessor()
    {
        if (mediaSourceType == MediaSourceType.MEDIA_SOURCE_MICROPHONE)
        {
            try
            {
                micSource = new MicrophoneSource();
                ds = micSource;
                ds.connect();
                if (otherCalls != null)
                {
                    ds = this.createMergingDataSource(micSource);
                }
            }
            catch (Exception e)
            {
                return "Couldn't create DataSource";
            }
        }
        if (mediaSourceType == MediaSourceType.MEDIA_SOURCE_FILE)
        {
            try
            {
                ds = javax.media.Manager.createDataSource(new MediaLocator(
                        "file:///" + mediaFilename));
            }
            catch (Exception e)
            {
                return "Couldn't create DataSource";
            }
        }

        // Try to create a processor to handle the input media locator
        try
        {
            processor = javax.media.Manager.createProcessor(ds);
            processor.addControllerListener(new StateListener(this));
            
        }
        catch (NoProcessorException npe)
        {
            return "Couldn't create processor";
        }
        catch (IOException ioe)
        {
            return "IOException creating processor";
        }

        // Wait for it to configure
        boolean result = waitForState(processor, Processor.Configured);
        if (result == false)
            return "Couldn't configure processor";

        // Get the tracks from the processor
        TrackControl[] tracks = processor.getTrackControls();

        // Do we have atleast one track?
        if (tracks == null || tracks.length < 1)
            return "Couldn't find tracks in processor";

        // Set the output content descriptor to RAW_RTP
        // This will limit the supported formats reported from
        // Track.getSupportedFormats to only valid RTP formats.
        ContentDescriptor cd = new ContentDescriptor(ContentDescriptor.RAW_RTP);
        processor.setContentDescriptor(cd);

        Format supported[];
        Format chosen = null;
        boolean atLeastOneTrack = false;

        // Program the tracks.
        for (int i = 0; i < tracks.length; i++)
        {
            Format format = tracks[i].getFormat();
            if (tracks[i].isEnabled())
            {
                supported = tracks[i].getSupportedFormats();

                // We've set the output content to the RAW_RTP.
                // So all the supported formats should work with RTP.
                // We'll just pick the first one.
                if (supported.length > 0)
                {
                    for (int j = 0; j < supported.length; j++)
                    {
                        if (supported[j].getEncoding().equals("ULAW/rtp"))
                        {
                            chosen = supported[j];
                        }
                    }
                    tracks[i].setFormat(chosen);
                    System.err
                            .println("Track " + i + " is set to transmit as:");
                    System.err.println("  " + chosen);
                    atLeastOneTrack = true;
                }
                else
                    tracks[i].setEnabled(false);
            }
            else
                tracks[i].setEnabled(false);
        }

        if (!atLeastOneTrack)
            return "Couldn't set any of the tracks to a valid RTP format";

        Codec[] codecs = null;
        
        if (System.getProperty("os.name").toUpperCase().indexOf("MAC") > -1)
        {
            codecs = new Codec[2];
        }
        else
        {
            codecs = new Codec[1];
        }
        int codecIndex = 0;
        if (System.getProperty("os.name").toUpperCase().indexOf("MAC") > -1)
        {
            codecIndex++;
            com.ibm.media.codec.audio.rc.RCModule rcMod = new com.ibm.media.codec.audio.rc.RCModule();
            RateCvrt rateconverter = new RateCvrt();
            codecs[0] = rcMod;
            
            codecs[1] = new com.ibm.media.codec.audio.ulaw.JavaEncoder();
        }
        else
        {
            com.ibm.media.codec.audio.AudioPacketizer packetizer = null;
    
            packetizer = new com.sun.media.codec.audio.ulaw.Packetizer();
            ((com.sun.media.codec.audio.ulaw.Packetizer) packetizer)
                    .setPacketSize(160);
    
            codecs[codecIndex] = packetizer;
        }
        
        try
        {
            tracks[0].setCodecChain(codecs);
            tracks[0].setFormat(chosen);
        }
        catch (javax.media.UnsupportedPlugInException e)
        {
            e.printStackTrace();
        }

        processor.realize();
        result = waitForState(processor, Controller.Realized);
        if (result == false)
            return "Couldn't realize processor";

        // Get the output data source of the processor
        dataOutput = processor.getDataOutput();

        processor.start();

        return null;
    }

    /**
     * Use the RTPManager API to create sessions for each media track of the
     * processor.
     */
    private String createTransmitter()
    {

        // Cheated. Should have checked the type.
        PushBufferDataSource pbds = (PushBufferDataSource) dataOutput;
        PushBufferStream pbss[] = pbds.getStreams();

        int port;
        SourceDescription srcDesList[];

        for (int i = 0; i < pbss.length; i++)
        {
            try
            {
                rtpMgr = RTPManager.newInstance();

                // Initialize the RTPManager with the RTPSocketAdapter
                sendAdapter = new SendAdapter(callMgr, destIp,
                        srcPort, destPort);
                rtpMgr.initialize(sendAdapter);

                System.err.println("Created RTP session: " + destIp + " "
                        + destPort);

                sendStream = rtpMgr.createSendStream(dataOutput, i);
                sendStream.start();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        return null;
    }

    private Integer stateLock = new Integer(0);

    private boolean failed = false;

    Integer getStateLock()
    {
        return stateLock;
    }

    void setFailed()
    {
        failed = true;
    }

    private synchronized boolean waitForState(Processor p, int state)
    {
        failed = false;

        // Call the required method on the processor
        if (state == Processor.Configured)
        {
            p.configure();
        }
        else if (state == Processor.Realized)
        {
            p.realize();
        }

        // Wait until we get an event that confirms the
        // success of the method, or a failure event.
        // See StateListener inner class
        while (p.getState() < state && !failed)
        {
            synchronized (getStateLock())
            {
                try
                {
                    getStateLock().wait();
                }
                catch (InterruptedException ie)
                {
                    return false;
                }
            }
        }

        if (failed)
            return false;
        else
            return true;
    }

    class StateListener implements ControllerListener
    {
        Transmitter transmitter;

        StateListener(Transmitter transmitter)
        {
            this.transmitter = transmitter;
        }

        public void controllerUpdate(ControllerEvent ce)
        {
            // If there was an error during configure or
            // realize, the processor will be closed
            if (ce instanceof ControllerClosedEvent)
                setFailed();

            // All controller events, send a notification
            // to the waiting thread in waitForState method.
            if (ce instanceof ControllerEvent)
            {
                if (loop == true && ce instanceof EndOfMediaEvent
                        && mediaSourceType == MediaSourceType.MEDIA_SOURCE_FILE)
                {
                    processor.setMediaTime(new Time(0));
                    try
                    {
                        processor.start();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else if (loop == false && ce instanceof EndOfMediaEvent
                        && mediaSourceType == MediaSourceType.MEDIA_SOURCE_FILE)
                {
                    EndOfMediaEvent endOfMedia = (EndOfMediaEvent)ce;
                    Call call = InternalCallManager.getInstance()
                            .getCallByHandle(callHandle);
                    InternalCallManager.getInstance().fireEvent(
                            transmitter.callMgr,
                            new Event(EventType.MEDIA,
                                    EventCode.MEDIA_END_OF_FILE,
                                    EventReason.MEDIA_NORMAL,
                                    call.getLineHandle(),
                                    callHandle,
                                    mediaFilename));
                }
                synchronized (getStateLock())
                {
                    getStateLock().notifyAll();
                }
            }
        }
    }

    private long now() { return System.nanoTime() / 1000000; }
}
