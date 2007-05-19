/*******************************************************************************
 *   Copyright 2007 SIP Response
 *   Copyright 2007 Michael D. Cohen
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

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.MediaSourceType;

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
                                              String callHandle,
                                              String address,
                                              int port);
    public abstract void startRtpReceive(String address, int port);
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
}
