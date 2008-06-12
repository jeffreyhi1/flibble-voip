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
import javax.sip.header.ViaHeader;
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
import com.sipresponse.flibblecallmgr.internal.media.MediaSocketManager;
import com.sipresponse.flibblecallmgr.internal.util.AuthenticationHelper;

public class HoldAction extends ActionThread
{
    private boolean hold;
    private int volume;
    public HoldAction(CallManager callMgr, Call call, boolean hold,
            int volume)
    {
        super(callMgr, call, null);
        this.callMgr = callMgr;
        this.call = call;
        this.hold = hold;
        this.volume = volume;
    }

    public void run()
    {
        doHold();
        if (signal != null)
        {
            signal.notifyResponseEvent();
        }
    }

    public void doHold()
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
                .getProvider(callMgr);
        Dialog dialog = call.getDialog();
        Request reinvite = null;
        SessionDescription localSdp = null;
        ContentTypeHeader contentTypeHeader = null;
        ContentLengthHeader contentLengthHeader = null;
        
        try
        {
            reinvite = dialog.createRequest(Request.INVITE);
            ViaHeader viaHeader = (ViaHeader) reinvite
                    .getHeader(ViaHeader.NAME);
            viaHeader.setRPort();

            LineManager lineMgr = InternalCallManager.getInstance()
                    .getLineManager(callMgr);
            Line fromLine = lineMgr.getLine(call.getLineHandle());
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
            localSdp = (SessionDescription) SdpFactory.getInstance()
                    .createSessionDescription(call.getLocalSdp().toString());
            if (true == hold)
            {
                // localSdp.getOrigin().setAddress("0.0.0.0");
                localSdp.getConnection().setAddress("0.0.0.0");
            }
            else
            {
                localSdp.getConnection().setAddress(callMgr.getContactIp());
            }
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
            int statusCode = responseEvent.getResponse().getStatusCode();
            ;
            System.err.println("ReinviteStatus code = " + statusCode);
            int count = 0;
            while (statusCode != 200 && count < 10)
            {

                count++;
                if (statusCode == 401 || statusCode == 403 || statusCode == 407)
                {
                    // ack 1st trans
                    flibbleProvider.ackResponse(responseEvent);
                    Request reinviteWithAuth = null;
                    try
                    {
                        reinviteWithAuth = dialog.createRequest(Request.INVITE);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    AuthenticationHelper.processResponseAuthorization(callMgr,
                            line, responseEvent.getResponse(),
                            reinviteWithAuth, true);

                    try
                    {
                        reinviteWithAuth.setContent(localSdp.toString(),
                                contentTypeHeader);
                    }
                    catch (ParseException e)
                    {
                        e.printStackTrace();
                    }
                    ct = flibbleProvider.sendDialogRequest(dialog,
                            reinviteWithAuth);
                }

                responseEvent = flibbleProvider.waitForResponseEvent(ct);
                statusCode = responseEvent.getResponse().getStatusCode();
                ;
                System.err.println("ReinviteStatus code = " + statusCode);
            }

            // response should be 200
            if (responseEvent != null && responseEvent.getResponse() != null)
            {
                System.err.println("Acking hold/unhold response");
                if (signal != null)
                {
                    signal.notifyResponseEvent();
                }
                flibbleProvider.ackResponse(responseEvent);
                if (hold == false)
                {

                    call.createMediaProvider();
                    int receivePort = InternalCallManager.getInstance().getMediaSocketManager(callMgr).getNextAvailablePort();
                    call.setLocalSdpPort(receivePort);
                    call.getMediaProvider().initializeRtpReceive(callMgr,
                            call.getLineHandle(),
                            call.getHandle(), callMgr.getLocalIp(),
                            receivePort);
                    call.getMediaProvider().setVolume(volume);
                    SessionDescription remoteSdp = null;
                    if (null != responseEvent.getResponse().getRawContent())
                    {
                        try
                        {
                            remoteSdp = SdpFactory.getInstance()
                                    .createSessionDescription(
                                            new String(responseEvent
                                                    .getResponse()
                                                    .getRawContent()));
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
                    
                    call.getMediaProvider().initializeRtpSend(callMgr,
                            call.getHandle(),
                            call.getRemoteSdpAddress(),
                            call.getRemoteSdpPort(),
                            receivePort,
                            MediaSourceType.MEDIA_SOURCE_MICROPHONE,
                            null,
                            false);

                    call.getMediaProvider().startRtpSend(remoteSdpIp,
                            remoteSdpPort);

                    InternalCallManager.getInstance().fireEvent(
                            this.callMgr,
                            new Event(EventType.CALL, EventCode.CALL_CONNECTED,
                                    EventReason.CALL_UNHOLD, line.getHandle(),
                                    call.getHandle()));
                }
                else if (hold == true)
                {
                    String remoteSdpIp = call.getRemoteSdpAddress();
                    int remoteSdpPort = call.getRemoteSdpPort();

                    call.getMediaProvider().stopRtpReceive(call.getLocalSdpAddress(), call.getLocalSdpPort());
                    call.getMediaProvider().stopRtpSend(remoteSdpIp, remoteSdpPort);
        
                    InternalCallManager.getInstance().fireEvent(
                            this.callMgr,
                            new Event(EventType.CALL,
                                    EventCode.CALL_HOLDING_REMOTE_PARTY,
                                    EventReason.CALL_NORMAL, line.getHandle(), call
                                            .getHandle()));
                }
            }
        }

    }

    private String now()
    {
        return new Long(System.nanoTime() / 1000000).toString() + ": ";
    }
}
