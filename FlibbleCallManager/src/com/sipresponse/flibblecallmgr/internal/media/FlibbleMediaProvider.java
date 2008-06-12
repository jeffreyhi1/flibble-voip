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

package com.sipresponse.flibblecallmgr.internal.media;

import java.net.URL;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.MediaSourceType;
import com.sipresponse.flibblecallmgr.internal.Call;

/**
 * Abstract class defining the media control interface. 
 * @author Mike Cohen
 */
public abstract class FlibbleMediaProvider
{
    protected boolean useMic;
    protected boolean audioRender;
    protected String mediaUrl;
    
    public void setUseMicrophone(boolean useMic)
    {
        this.useMic = useMic;
    }
    public void setAudioRender(boolean render)
    {
        this.audioRender = render;
    }
    public void setMediaStreamSource(String mediaUrl)
    {
        this.mediaUrl = mediaUrl;
    }
    
    public abstract void initializeRtpReceive(CallManager callMgr,
                                              String lineHandle,
                                              String callHandle,
                                              String address,
                                              int port);
    public abstract void stopRtpReceive(String address, int port);
    public abstract void initializeRtpSend(CallManager callMgr,
                                           String callHandle,
                                           String destIp,
                                           int destPort,
                                           int srcPort,
                                           MediaSourceType mediaSourceType,
                                           String mediaFilename,
                                           boolean loop);
    public abstract void startRtpSend(String destIp, int destPort);
    public abstract void stopRtpSend(String destIp, int destPort);
    public abstract void changeMediaSource(MediaSourceType mediaSourceType,
                                           String mediaFilename,
                                           boolean loop);
    public abstract void playFileLocally(URL url, boolean loop, int volume);
    public abstract void stopFileLocally(URL url);
    public abstract void stopLocalPlayoutAll();
    public abstract void sendDtmf(int dtmfCode);
    public abstract void joinOtherCallsWithDataSource(Call call, Call[] otherCalls);
    public abstract void setVolume(int volume);
    public abstract void setMicrophoneGain(int gain);
    public abstract void enableEchoSuppression(boolean enable, float percentSuppression);
}
