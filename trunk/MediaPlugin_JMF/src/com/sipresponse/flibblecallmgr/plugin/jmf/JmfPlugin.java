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
package com.sipresponse.flibblecallmgr.plugin.jmf;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.MediaSourceType;
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;

public class JmfPlugin extends FlibbleMediaProvider
{
    private Receiver receiver;
    private Transmitter transmitter;
    private CallManager callMgr;
    private String callHandle;
    private String destIp;
    private int destPort;
    private int srcPort;
    
    @Override
    public void initializeRtpReceive(CallManager callMgr,
            String lineHandle,
            String callHandle,
            String address,
            int port)
    {
        receiver = new Receiver(callMgr, lineHandle, callHandle, address, port);
    }

    @Override
    public void startRtpReceive(String address, int port)
    {
    }

    @Override
    public void stopRtpReceive(String address, int port)
    {
        receiver.stop();
    }

    @Override
    public void initializeRtpSend(CallManager callMgr,
            String callHandle,
            String destIp,
            int destPort,
            int srcPort,
            MediaSourceType mediaSourceType,
            String mediaFilename,
            boolean loop)
    {
        this.callMgr = callMgr;
        this.callHandle = callHandle;
        this.destIp = destIp;
        this.destPort = destPort;
        this.srcPort = srcPort;
        transmitter = new Transmitter(callMgr,
                callHandle,
                destIp,
                destPort,
                srcPort,
                mediaSourceType,
                mediaFilename,
                loop);
    }

    @Override
    public void startRtpSend(String destIp, int destPort)
    {
    }

    @Override
    public void stopRtpSend(String destIp, int destPort)
    {
        if (transmitter != null)
        {
            transmitter.stop();
        }
    }

    @Override
    public void changeMediaSource(MediaSourceType mediaSourceType,
            String mediaFilename, boolean loop)
    {
        if (null != transmitter)
        {
            transmitter.stop();
            transmitter = new Transmitter(callMgr,
                    callHandle,
                    destIp,
                    destPort,
                    srcPort,
                    mediaSourceType, 
                    mediaFilename,
                    loop);

        }
    }

    public void sendDtmf(int dtmfCode)
    {
        if (null != transmitter)
        {
            transmitter.sendDtmf(dtmfCode);
        }
    }

}
