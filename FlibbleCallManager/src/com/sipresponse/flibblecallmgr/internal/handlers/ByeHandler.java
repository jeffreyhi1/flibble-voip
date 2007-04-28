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
package com.sipresponse.flibblecallmgr.internal.handlers;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;

public class ByeHandler extends Handler
{
    public ByeHandler(CallManager callMgr,
            Call call,
            RequestEvent requestEvent)
    {
        super(callMgr, call, null, requestEvent);
    }
    
    public void execute()
    {
        // fire a disconnected event
        InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL,
                EventCode.CALL_DISCONNECTED,
                EventReason.CALL_DISCONNECT_REMOTE,
                line.getHandle(),
                call.getHandle()));
        
        // stop media here?
        FlibbleMediaProvider mediaProvider = call.getMediaProvider();
        
        if (null != mediaProvider)
        {
            mediaProvider.stopRtpReceive(call.getLocalSdpAddress(), call.getLocalSdpPort());
            mediaProvider.stopRtpSend(call.getRemoteSdpAddress(), call.getRemoteSdpPort());
        }
        // send a 200 OK
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
            .getProvider(callMgr);
        Response response = null;
        try
        {
            response = flibbleProvider.messageFactory.createResponse(200, requestEvent.getRequest());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null != response)
        {
            ServerTransaction st = requestEvent.getServerTransaction();
            try
            {
                st.sendResponse(response);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        // remove call from internal call manager
        InternalCallManager.getInstance().removeCallByHandle(call.getHandle());
        
    }
}
