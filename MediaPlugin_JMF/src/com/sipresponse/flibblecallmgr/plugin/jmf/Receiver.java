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

import javax.media.Codec;
import javax.media.ConfigureCompleteEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.NoProcessorException;
import javax.media.NotConfiguredError;
import javax.media.Player;
import javax.media.Processor;
import javax.media.RealizeCompleteEvent;
import javax.media.UnsupportedPlugInException;
import javax.media.control.BufferControl;
import javax.media.control.TrackControl;
import javax.media.format.AudioFormat;
import javax.media.rtp.Participant;
import javax.media.rtp.RTPControl;
import javax.media.rtp.RTPManager;
import javax.media.rtp.ReceiveStream;
import javax.media.rtp.ReceiveStreamListener;
import javax.media.rtp.SessionListener;
import javax.media.rtp.event.ByeEvent;
import javax.media.rtp.event.NewParticipantEvent;
import javax.media.rtp.event.NewReceiveStreamEvent;
import javax.media.rtp.event.ReceiveStreamEvent;
import javax.media.rtp.event.RemotePayloadChangeEvent;
import javax.media.rtp.event.SessionEvent;
import javax.media.rtp.event.StreamMappedEvent;
import javax.media.protocol.DataSource;
import com.sipresponse.flibblecallmgr.CallManager;

public class Receiver implements ReceiveStreamListener, SessionListener, 
    ControllerListener
{
    private RTPManager rtpMgr;
    private String callHandle;
    private Object dataSync = new Object();
    private Processor p;
    private DataSource ds;
    private ReceiveStream stream;
    private int volume;
    private boolean enableEchoSuppression = false;
    private float percentSuppression = 0.0f;
    private int port;
    private ReceiveAdapter adapter;
    public Receiver(CallManager callMgr, String lineHandle, String callHandle, String address, int port)
    {
        this.port = port;
        rtpMgr = RTPManager.newInstance();
        rtpMgr.addSessionListener(this);
        rtpMgr.addReceiveStreamListener(this);
        this.callHandle = callHandle;
        // Initialize the RTPManager with the RTPSocketAdapter
        adapter = new ReceiveAdapter(
                    callMgr,
                    address, 
                    port,
                    lineHandle,
                    callHandle);
        rtpMgr.initialize(adapter);
    }
    
    public void stop()
    {
        if (stream != null)
        {
            try
            {
                stream.getDataSource().stop();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (p != null)
        {
            p.stop();
            p.close();
        }
        rtpMgr.dispose();
    }
    /**
     * SessionListener.
     */
    public synchronized void update(SessionEvent evt)
    {
        if (evt instanceof NewParticipantEvent)
        {
        }
    }


    /**
     * ReceiveStreamListener
     */
    public synchronized void update( ReceiveStreamEvent evt)
    {
        if (evt instanceof NewReceiveStreamEvent)
        {
            RTPManager mgr = (RTPManager)evt.getSource();
            Participant participant = evt.getParticipant(); // could be null.
            stream = evt.getReceiveStream();  // could be null.
            DataSource ds = stream.getDataSource();
    
            BufferControl bc = (BufferControl) ds.getControl("javax.media.control.BufferControl");
            if (bc != null)
            {
                System.out.println("BufferControl - Setting buffer length");
                bc.setBufferLength(500);
                bc.setMinimumThreshold(200);
            }
            // create a processor by passing datasource to the Media Manager
            try
            {
                p =  Manager.createProcessor(ds);
            }
            catch (NoProcessorException e1)
            {
                e1.printStackTrace();
            }
            catch (IOException e1)
            {
                e1.printStackTrace();
            }
    
            if (p == null)
                return;
    
            p.configure();
            try
            {
                waitForState(Processor.Configured);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
            p.setContentDescriptor(null);
            BufferControl bc2 = (BufferControl) mgr.getControl("javax.media.control.BufferControl");
            if (bc2 != null)
            {
                System.out.println("BufferControl (rtp) - Setting buffer length");
                bc2.setBufferLength(500);
                bc2.setMinimumThreshold(200);
            }
    
            // Obtain the track controls.
            TrackControl tc[] = p.getTrackControls();
    
            if (tc == null)
            {
                System.err.println("no track controls");
                return;
            }
    
            // Search for the track control for audio
            TrackControl audioTrack = null;
            for (int i = 0; i < tc.length; i++)
            {
                if (tc[i].getFormat() instanceof AudioFormat)
                {
                    audioTrack = tc[i];
                    break;
                }
            }
    
            try
            {
                Codec codec[] = null;
                
                codec = new Codec[1];
                codec[0] = new com.sun.media.codec.audio.ulaw.DePacketizer();
                //codec[1] = new com.ibm.media.codec.audio.ulaw.JavaDecoder();

                CustomSoundRenderer renderer = new CustomSoundRenderer(0);
                EchoSuppressor.getInstance().setEnabled(this.enableEchoSuppression);
                EchoSuppressor.getInstance().setSuppressionLevel(0.90f);
                audioTrack.setCodecChain(codec);
                audioTrack.setRenderer(renderer);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
    
            p.realize();
            try
            {
                waitForState(Processor.Realized);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            setVolume(volume);
            p.start();
            try
            {
                waitForState(Processor.Started);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (evt instanceof StreamMappedEvent)
        {

            if (stream != null && stream.getDataSource() != null)
            {
            }
        }
        else if (evt instanceof ByeEvent)
        {

        }

    }


    /**
     * ControllerListener for the Players.
     */
    public synchronized void controllerUpdate(ControllerEvent ce)
    {

        Processor p = (Processor) ce.getSourceController();

        if (p == null)
            return;


        if (ce instanceof ConfigureCompleteEvent)
        {
        }
        // Get this when the internal players are realized.
        if (ce instanceof RealizeCompleteEvent)
        {
            p.start();
        }

        if (ce instanceof ControllerErrorEvent)
        {
            p.removeControllerListener(this);
            System.err.println("AVReceive3 internal error: " + ce);
        }

    }
    
    public DataSource getDataSource()
    {
        return ds;
    }
    
    private void waitForState(int state) throws Exception
    {
        int count = 0;
        while (true)
        {
            try
            {
                Thread.sleep(10);
            }
            catch (Exception e)
            {
                
            }
            if (p.getState() == state)
            {
                break;
            }
            else
            {
            }
            count++;
            if (count > 400)
                throw (new Exception("Could not reach state: " + new Integer(state)));
        }
    }
    
    public void setVolume(int volume)
    {
        this.volume = volume;
        GainControl gc = null;
        if (null != p)
        {
            gc = (GainControl) p.getControl("javax.media.GainControl");
            if (null != gc)
            {
                System.err.println("Current Level = " + gc.getLevel());
                try
                {
                    gc.setLevel((float)volume / 127.0f);
                }
                catch (Exception e)
                {
                }
            }
        }
    }
    
    public void enableEchoSuppression(boolean enable, float percentSuppression)
    {
        enableEchoSuppression = enable;
        this.percentSuppression = percentSuppression;
        EchoSuppressor.getInstance().setEnabled(this.enableEchoSuppression);
    }
}

