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

import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.FromHeader;
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;
import com.sipresponse.flibblecallmgr.internal.LineManager;

public class InviteHandler extends Handler
{
   private String callerId;
   
   public InviteHandler(CallManager callMgr,
            RequestEvent requestEvent)
    {
        super(callMgr, null, null, requestEvent);
        CallIdHeader callIdHeader = (CallIdHeader) requestEvent.getRequest().getHeader(CallIdHeader.NAME);
        String callId = callIdHeader.getCallId();
        
        LineManager lineMgr = InternalCallManager.getInstance().getLineManager(callMgr);
        
        SipURI uri = (SipURI) requestEvent.getRequest().getRequestURI();
        String lineHandle = lineMgr.findLineHandle(uri);
        line = lineMgr.getLine(lineHandle);
        
        FromHeader fromHeader = (FromHeader)requestEvent.getRequest().getHeader(FromHeader.NAME);
        Address fromAddress = fromHeader.getAddress();
        callerId = fromAddress.getDisplayName();
        
        call = new Call(callMgr, lineHandle, uri.toString(), callId, false, fromAddress);
        call.setLastRequestEvent(requestEvent);
    }

    @Override
    public void execute()
    {
        // create the call object
       // set the call's remote SDP
       String content = new String(requestEvent.getRequest().getRawContent());
       SessionDescription remoteSdp = null;
       try
       {
           remoteSdp = SdpFactory.getInstance().createSessionDescription(content);
       }
       catch (SdpParseException e)
       {
           e.printStackTrace();
       }
       call.setRemoteSdp(remoteSdp);
        
        // send out a 100 trying
        //sendResponse(100);
        
        // fire an INCOMING INVITE event
        InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL,
                EventCode.CALL_INCOMING_INVITE,
                EventReason.CALL_NORMAL,
                line.getHandle(),
                call.getHandle(),
                new String(callerId)));
        
        // if the call is accepted, the
        // AcceptCallAction sends a 180 ringing
        
        // if the call is answered,
        // the AnswerCallAction sends a 200 Ok, and
        // starts sending audio
        
    }
}
