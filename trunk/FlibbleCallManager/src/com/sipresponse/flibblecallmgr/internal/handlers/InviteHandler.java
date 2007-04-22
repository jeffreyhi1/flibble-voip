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

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.SipURI;
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;
import com.sipresponse.flibblecallmgr.internal.LineManager;

public class InviteHandler extends Handler
{
   public InviteHandler(CallManager callMgr,
            Call call,
            RequestEvent requestEvent)
    {
        super(callMgr, call, null, requestEvent);
    }

    @Override
    public void execute()
    {

        // create the call object
        String callId = InternalCallManager.getInstance().getProvider(callMgr).sipProvider
            .getNewCallId().getCallId();
        
       LineManager lineMgr = InternalCallManager.getInstance().getLineManager(callMgr);
        
       SipURI uri = (SipURI) requestEvent.getRequest().getRequestURI();
       String lineHandle = lineMgr.findLine(uri);
       Call call = new Call(callMgr, lineHandle, uri.toString(), callId);
        
        // send out a 100 trying
        sendResponse(100);
        
        // no media is created yet,
        // that is the job of the Answer action
        
    }
}
