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
package com.sipresponse.flibblecallmgr.internal.actions;

import java.text.ParseException;

import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ResponseEvent;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Request;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.MediaSourceType;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;
import com.sipresponse.flibblecallmgr.internal.LineManager;

public class HoldAction extends ActionThread
{
    private boolean hold;

    public HoldAction(CallManager callMgr, Call call, boolean hold)
    {
        super(callMgr, call, null);
        this.callMgr = callMgr;
        this.call = call;
        this.hold = hold;
    }

    public void run()
    {
        doHold();
    }

    public void doHold()
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
                .getProvider(callMgr);
        Dialog dialog = call.getDialog();
        Request reinvite = null;
        try
        {
            reinvite = dialog.createRequest(Request.INVITE);
            LineManager lineMgr = InternalCallManager.getInstance().getLineManager(
                    callMgr);            Line fromLine = lineMgr.getLine(call.getLineHandle());
            String fromUser = fromLine.getUser();
            SipURI contactUri = flibbleProvider.addressFactory.createSipURI(
                    fromUser, callMgr.getContactIp());
            Address contactAddress = flibbleProvider.addressFactory
                    .createAddress(contactUri);
            ((SipURI) contactAddress.getURI()).setPort(callMgr.getUdpSipPort());
            ContactHeader contactHeader = flibbleProvider.headerFactory
                    .createContactHeader(contactAddress);            
            reinvite.setHeader(contactHeader);
            // create a _copy_ of the sdp
            SessionDescription localSdp =(SessionDescription) SdpFactory.getInstance().createSessionDescription(call.getLocalSdp().toString());
            if (true == hold)
            {
                //localSdp.getOrigin().setAddress("0.0.0.0");
                localSdp.getConnection().setAddress("0.0.0.0");
            }
            ContentTypeHeader contentTypeHeader = null;
            ContentLengthHeader contentLengthHeader = null;
            try
            {
                contentTypeHeader = flibbleProvider.headerFactory
                        .createContentTypeHeader("application", "sdp");
                contentLengthHeader = flibbleProvider.headerFactory
                        .createContentLengthHeader(localSdp.toString().length());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            reinvite.setHeader(contentLengthHeader);
            try
            {
                System.err.println("doHold - setting content:");
                System.err.println(localSdp.toString());
                reinvite.setContent(localSdp.toString(), contentTypeHeader);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null != reinvite)
        {
            ClientTransaction ct = null;
            try
            {
                ct = flibbleProvider.sendDialogRequest(dialog, reinvite);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            ResponseEvent responseEvent = flibbleProvider
                    .waitForResponseEvent(ct);
            while (responseEvent.getResponse().getStatusCode() < 200)
            {
                responseEvent = flibbleProvider
                    .waitForResponseEvent(ct);
            }
            // response should be 200
            if (responseEvent != null &&
                    responseEvent.getResponse() != null &&
                    (responseEvent.getResponse().getStatusCode()<400 ))
            {
                System.err.println("Acking hold/unhold response");
                flibbleProvider.ackResponse(responseEvent);
                
                if (hold == true)
                {
                    String remoteSdpIp = call.getRemoteSdpAddress();
                    int remoteSdpPort = call.getRemoteSdpPort();
                    call.getMediaProvider().stopRtpSend(remoteSdpIp, remoteSdpPort);
                    InternalCallManager.getInstance().fireEvent(
                            this.callMgr,
                            new Event(EventType.CALL,
                                    EventCode.CALL_HOLDING_REMOTE_PARTY,
                                    EventReason.CALL_NORMAL,
                                    line.getHandle(),
                                    call.getHandle()));
                }
                else
                {
                    SessionDescription remoteSdp = null;
                    if (null != responseEvent.getResponse().getRawContent())
                    {
                        try
                        {
                            remoteSdp = SdpFactory.getInstance().createSessionDescription(new String(responseEvent.getResponse().getRawContent()));
                        }
                        catch (SdpParseException e)
                        {
                            e.printStackTrace();
                        }
                        if (null != remoteSdp)
                        {
                            call.setRemoteSdp(remoteSdp);
                        }
                    }
                    String remoteSdpIp = null;
                    remoteSdpIp = call.getRemoteSdpAddress();
                    int remoteSdpPort = call.getRemoteSdpPort();
                    call.getMediaProvider().startRtpSend(remoteSdpIp, remoteSdpPort);
                    InternalCallManager.getInstance().fireEvent(
                            this.callMgr,
                            new Event(EventType.CALL,
                                    EventCode.CALL_CONNECTED,
                                    EventReason.CALL_UNHOLD,
                                    line.getHandle(),
                                    call.getHandle()));
                }
            }
            else
            {
                System.err.println("Acking hold/unhold response");
                flibbleProvider.ackResponse(responseEvent);
                InternalCallManager.getInstance().fireEvent(this.callMgr, 
                        new Event(EventType.CALL,
                                  EventCode.CALL_HOLD_FAILED,
                                  EventReason.CALL_FAILURE_REJECTED,
                                  line.getHandle(),
                                  call.getHandle()));
            }
        }
    }
}
