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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.media.CannotRealizeException;
import javax.media.Control;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.GainControl;
import javax.media.Manager;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.Time;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.MediaSourceType;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;
import javax.media.protocol.DataSource;

public class JmfPlugin extends FlibbleMediaProvider
{
    private Receiver receiver;
    private static Vector<Receiver> receivers = new Vector<Receiver>();
    private Transmitter transmitter;

    private CallManager callMgr;

    private String callHandle;

    private String destIp;

    private int destPort;

    private int srcPort;
    private int playoutVolume;

    private MediaSourceType mediaSourceType;

    private String mediaFilename;
    private static boolean echoSuppression;
    private static float percentSuppress;
    private ConcurrentHashMap<String, Player> playerMap = new ConcurrentHashMap<String, Player>();
    private Vector<String> playerVector = new Vector<String>();
    private boolean loop;

    @Override
    public void initializeRtpReceive(CallManager callMgr, String lineHandle,
            String callHandle, String address, int port)
    {
        receiver = new Receiver(callMgr, lineHandle, callHandle, address, port);
        receiver.enableEchoSuppression(echoSuppression, percentSuppress);
        receivers.add(receiver);
    }
    

    @Override
    public void stopRtpReceive(String address, int port)
    {
        receiver.stop();
        receivers.remove(receiver);
    }

    @Override
    public void initializeRtpSend(CallManager callMgr, String callHandle,
            String destIp, int destPort, int srcPort,
            MediaSourceType mediaSourceType, String mediaFilename, boolean loop)
    {
        this.callMgr = callMgr;
        this.callHandle = callHandle;
        this.destIp = destIp;
        this.destPort = destPort;
        this.srcPort = srcPort;
        this.mediaSourceType = mediaSourceType;
        this.mediaFilename = mediaFilename;
        this.loop = loop;
        transmitter = new Transmitter(this, callMgr, callHandle, destIp, destPort,
                srcPort, mediaSourceType, mediaFilename, loop, null);
    }

    @Override
    public void startRtpSend(String destIp, int destPort)
    {
        if (transmitter == null)
        {
            transmitter = new Transmitter(this, callMgr, callHandle, destIp,
                    destPort, srcPort, mediaSourceType, mediaFilename, loop, null);
        }
    }

    @Override
    public void stopRtpSend(String destIp, int destPort)
    {
        if (transmitter != null)
        {
            transmitter.stop();
            transmitter = null;
        }
    }

    @Override
    public void changeMediaSource(MediaSourceType mediaSourceType,
            String mediaFilename, boolean loop)
    {
        if (null != transmitter)
        {
            this.mediaSourceType = mediaSourceType;
            this.mediaFilename = mediaFilename;
            this.loop = loop;
            transmitter.stop();
            transmitter = new Transmitter(this, callMgr, callHandle, destIp,
                    destPort, srcPort, mediaSourceType, mediaFilename, loop, null);

        }
    }

    public void sendDtmf(int dtmfCode)
    {
        if (null != transmitter)
        {
            transmitter.sendDtmf(dtmfCode);
        }
    }

    public void playFileLocally(URL url, boolean loop, int volume)
    {
        try
        {
            System.out.println("Playing file locally: " + url.toString());
            playAudio(url.toString(), loop, volume);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void playAudio(final String filename, final boolean loop, final int volume)
    {
        try
        {
            this.playoutVolume = volume;
            this.loop = loop;
            final Player p = getPlayer(filename);
            p.stop();
            p.setMediaTime(new Time(0));
            GainControl gc = (GainControl)p.getControl("javax.media.GainControl");
            try
            {
                gc.setLevel((float)volume / 127.0f);
            }
            catch (Exception e)
            {
                
            }
            p.start();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private Player getPlayer(final String filename)
    {
        Player p = playerMap.get(filename);
        if (null == p)
        {
            try
            {
                p = Manager.createRealizedPlayer(new MediaLocator(filename));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            playerMap.put(filename, p);
            playerVector.add(filename.toString());
            p.addControllerListener(
                    new ControllerListener()
                    {
                        public void controllerUpdate(ControllerEvent evt)
                        {
                            System.out.println("playAudio:  ControllerEvent: " + evt);
                            if (evt instanceof EndOfMediaEvent && 
                                    loop)
                            {
                                EndOfMediaEvent endEvent = (EndOfMediaEvent)evt;
                                Player p = (Player) evt.getSource();
                                if (p != null)
                                {
                                    // System.err.pringln("ABOUT TO STOP")
                                    p.stop();
                                    System.err.println("ABOUT TO SET MEDIA TIME");
                                    p.setMediaTime(new Time(0));
                                    System.err.println("processing = false");
                                    System.err.println("ABOUT TO PLAY AUDIO");
                                    playAudio(filename, loop, playoutVolume);
                                }
                            }
                            else if (evt instanceof EndOfMediaEvent && 
                                    !loop)
                            {
                            }
                        }
                        
                    }
                );
        }      
        return p;
    }

    @Override
    public void stopFileLocally(URL filename)
    {
        System.err.println("Stopping file playout: " + filename.toString());
        Player p = playerMap.get(filename.toString());
        if (null == p)
        {
            return;
        }
        System.out.println("Stop file locally ");
        if (null != p)
        {
            try
            {
                loop = false;
                p.stop();
                p.close();
            }
            catch (Exception e)
            {
                e.printStackTrace();   
            }
            playerMap.remove(filename.toString());
            playerVector.remove(filename.toString());
        }
    }

    @Override
    public void stopLocalPlayoutAll()
    {
        for (int i = 0; i < playerVector.size(); i++)
        {
            try
            {
                stopFileLocally( new URL(playerVector.get(i)) );
            }
            catch (MalformedURLException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void joinOtherCallsWithDataSource(Call call, Call[] otherCalls)
    {
        if (transmitter != null)
        {
            transmitter.stop();
            transmitter = null;
        }
        transmitter = new Transmitter(this, callMgr, callHandle, destIp, destPort,
                srcPort, mediaSourceType, mediaFilename, loop, otherCalls);
    }
    
    public DataSource getIncomingDataSource()
    {
        if (null == receiver)
            return null;
        return receiver.getDataSource();
    }


    @Override
    public void setVolume(int volume)
    {
        this.playoutVolume = volume;
        if (receiver != null)
        {
            receiver.setVolume(volume);
        }
        Player p = null;
        Enumeration<Player> enumer = playerMap.elements();
        if (null != enumer)
        {
            while (enumer.hasMoreElements())
            {
                p = enumer.nextElement();
                GainControl gc = (GainControl)p.getControl(GainControl.class.getName());
                if (gc != null)
                {
                    gc.setLevel((float) ((float)volume / 127.0f));
                }

            }
            
        }
        
    }

    @Override
    public void setMicrophoneGain(int gain)
    {
        MicrophoneThread.getInstance().setGain(gain);        
    }


    @Override
    public void enableEchoSuppression(boolean enable, float percentSuppression)
    {
        echoSuppression = enable;
        percentSuppress = percentSuppression;
        for (Receiver r : receivers)
        {
            r.enableEchoSuppression(enable, percentSuppression);
        }
    }
    
    
}