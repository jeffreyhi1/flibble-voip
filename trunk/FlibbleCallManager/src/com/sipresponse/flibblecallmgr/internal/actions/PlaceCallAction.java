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

import gov.nist.javax.sip.Utils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Vector;

import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.sip.ClientTransaction;
import javax.sip.ResponseEvent;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.UserAgentHeader;
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
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;
import com.sipresponse.flibblecallmgr.internal.util.AuthenticationHelper;

public class PlaceCallAction extends ActionThread
{
    private int timeout = 60000;
    private MediaSourceType mediaSourceType;
    private String mediaFilename;
    private FlibbleSipProvider flibbleProvider;
    private int receivePort;
    private FlibbleMediaProvider mediaProvider;
    private String destIp;
    private int destPort;
    private boolean loop;
    private int initialVolume;
    private int initialGain;
    
    public PlaceCallAction(CallManager callMgr,
            Call call,
            MediaSourceType mediaSourceType,
            String mediaFilename,
            boolean loop,
            int initialVolume,
            int initialGain)
    {
        super(callMgr, call, null);
        this.mediaSourceType = mediaSourceType;
        this.mediaFilename = mediaFilename;
        this.loop = loop;
        this.initialVolume = initialVolume;
        this.initialGain = initialGain;
        flibbleProvider = InternalCallManager.getInstance()
                .getProvider(callMgr);
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public void run()
    {
        try
        {
            receivePort = InternalCallManager.getInstance().getMediaSocketManager(callMgr).getNextAvailablePort();
            
            Request request = createRequest();
            setContent(request);
            ClientTransaction ct = flibbleProvider.sendRequest(request);
            call.setClientTransaction(ct);
            ResponseEvent responseEvent = flibbleProvider
                    .waitForResponseEvent(ct);
            if (null == responseEvent)
            {

            }
            else
            {
                startMediaReceive();
                int statusCode = responseEvent.getResponse().getStatusCode();
                while (true)
                {
                    if (100 == statusCode)
                    {
                        System.err.println("Firing Trying");
                        InternalCallManager.getInstance().fireEvent(
                                this.callMgr,
                                new Event(EventType.CALL,
                                        EventCode.CALL_TRYING,
                                        EventReason.CALL_NORMAL,
                                        line.getHandle(),
                                        call.getHandle()));   
                        responseEvent = flibbleProvider
                            .waitForResponseEvent(ct);                        
                        statusCode = responseEvent.getResponse().getStatusCode();
                        continue;
                    }
                    else if (statusCode >= 500)
                    {
                        EventReason eventReason = EventReason.CALL_FAILURE_NETWORK;
                        InternalCallManager.getInstance().fireEvent(
                                this.callMgr,
                                new Event(EventType.CALL,
                                        EventCode.CALL_FAILED, eventReason,
                                        line.getHandle(), call.getHandle()));
                        break;
                    }
                    else if (statusCode == 401 || statusCode == 403 || statusCode == 407)
                    {
                        ct.terminate();
                        Request inviteWithAuth = createRequest();
                        AuthenticationHelper.processResponseAuthorization(callMgr,
                                line,
                                responseEvent.getResponse(),
                                inviteWithAuth,
                                true);
                        setContent(inviteWithAuth);
                        ct = flibbleProvider.sendRequest(inviteWithAuth);
                        call.setClientTransaction(ct);
                        responseEvent = flibbleProvider.waitForResponseEvent(ct);
                        statusCode = responseEvent.getResponse().getStatusCode();
                        continue;
                    }
                    else if (statusCode == 487)
                    {
                        InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL,
                                EventCode.CALL_DISCONNECTED,
                                EventReason.CALL_CANCELLED,
                                line.getHandle(),
                                call.getHandle()));
                        break;
                    }
                    else if (statusCode > 400)
                    {
                        EventReason eventReason = EventReason.CALL_FAILED_REQUEST;
                        if (486 == statusCode)
                        {
                            eventReason = EventReason.CALL_BUSY;
                        }
                        else if (482 == statusCode)
                        {
                            eventReason = EventReason.CALL_FAILED_LOOP_DETECTED;
                        }
                        InternalCallManager.getInstance().fireEvent(
                                this.callMgr,
                                new Event(EventType.CALL,
                                        EventCode.CALL_FAILED, eventReason,
                                        line.getHandle(), call.getHandle()));

                        break;
                    }
                    else if (statusCode == 183 || statusCode == 180)
                    {
                        InternalCallManager.getInstance().fireEvent(
                                this.callMgr,
                                new Event(EventType.CALL,
                                        EventCode.CALL_REMOTE_RINGING,
                                        EventReason.CALL_NORMAL,
                                        line.getHandle(),
                                        call.getHandle()));
                        responseEvent = flibbleProvider
                                .waitForResponseEvent(ct);
                        statusCode = responseEvent.getResponse().getStatusCode();
                    }
                    else if (statusCode < 200)
                    {
                        responseEvent = flibbleProvider
                                .waitForResponseEvent(ct);
                    }
                    else if (statusCode >= 200 && statusCode < 400)
                    {
                        if (200 == statusCode)
                        {
                            SessionDescription remoteSdp = 
                                SdpFactory.getInstance().createSessionDescription(
                                        new String(responseEvent.getResponse().getRawContent()));
                            call.setRemoteSdp(remoteSdp);
                            startMediaSend();
                        }
                        flibbleProvider.ackResponse(responseEvent);
                        InternalCallManager.getInstance()
                                .fireEvent(
                                        this.callMgr,
                                        new Event(EventType.CALL,
                                                EventCode.CALL_CONNECTED,
                                                EventReason.CALL_NORMAL, line
                                                        .getHandle(), call
                                                        .getHandle()));
                        break;
                    }
                    statusCode = responseEvent.getResponse().getStatusCode();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void setContent(Request request)
    {
        // Create ContentTypeHeader
        ContentTypeHeader contentTypeHeader = null;
        ContentLengthHeader contentLengthHeader = null;
        SessionDescription localSdp = null;

        if (mediaSourceType != MediaSourceType.MEDIA_SOURCE_NONE)
        {
            try
            {
                call.createLocalSdp(null, null, receivePort);
                localSdp = call.getLocalSdp();
                contentTypeHeader = flibbleProvider.headerFactory
                        .createContentTypeHeader("application", "sdp");
                contentLengthHeader = flibbleProvider.headerFactory
                        .createContentLengthHeader(localSdp.toString().length());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            request.setHeader(contentLengthHeader);
            try
            {
                request.setContent(localSdp.toString(), contentTypeHeader);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Request createRequest()
    {
        SipProvider sipProvider = flibbleProvider.sipProvider;
        LineManager lineMgr = InternalCallManager.getInstance().getLineManager(
                callMgr);
        Line fromLine = lineMgr.getLine(call.getLineHandle());
        String fromUser = fromLine.getUser();
        String fromHost = callMgr.getDomain();
        String fromDisplayName = fromLine.getDisplayName();
        Request request = null;

        try
        {
            String toUriString = call.getSipUriString();
            SipURI toUri = (SipURI) flibbleProvider.addressFactory
                    .createURI(toUriString);

            // create >From Header
            SipURI fromAddress = flibbleProvider.addressFactory.createSipURI(
                    fromUser, fromHost);

            Address fromNameAddress = flibbleProvider.addressFactory
                    .createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = flibbleProvider.headerFactory
                    .createFromHeader(fromNameAddress, Utils.generateTag());

            // create To Header
            Address toNameAddress = flibbleProvider.addressFactory
                    .createAddress(toUri);
            ToHeader toHeader = flibbleProvider.headerFactory.createToHeader(
                    toNameAddress, null);

            // create Contact Header
            SipURI contactUri = flibbleProvider.addressFactory.createSipURI(
                    fromUser, callMgr.getContactIp());
            Address contactAddress = flibbleProvider.addressFactory
                    .createAddress(contactUri);
            ((SipURI) contactAddress.getURI()).setPort(callMgr.getUdpSipPort());
            ContactHeader contactHeader = flibbleProvider.headerFactory
                    .createContactHeader(contactAddress);

            // Create ViaHeaders
            ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
            // add via headers
            if (callMgr.getPublicIp() != null)
            {
                ViaHeader publicViaHeader = flibbleProvider.headerFactory
                        .createViaHeader(callMgr.getPublicIp(), sipProvider
                                .getListeningPoint("udp").getPort(), "udp", null);
                publicViaHeader.setRPort();
                viaHeaders.add(publicViaHeader);
            }
            else
            {
                ViaHeader viaHeader = flibbleProvider.headerFactory
                        .createViaHeader(callMgr.getLocalIp(), sipProvider
                                .getListeningPoint("udp").getPort(), "udp", null);
                viaHeader.setRPort();
                viaHeaders.add(viaHeader);
            }

            // Create a new CallId header
            CallIdHeader callIdHeader = flibbleProvider.headerFactory
                    .createCallIdHeader(call.getCallId());

            // Create a new Cseq header
            CSeqHeader cSeqHeader = flibbleProvider.headerFactory
                    .createCSeqHeader((long) 1, Request.INVITE);

            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = flibbleProvider.headerFactory
                    .createMaxForwardsHeader(70);

            // Create the request.
            request = flibbleProvider.messageFactory.createRequest(toUri,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            request.setHeader(contactHeader);
            
            UserAgentHeader uaHeader = null;
            Vector<String> uaList = new Vector<String>();
            uaList.add(callMgr.getUserAgent());
            try
            {
                uaHeader = flibbleProvider.headerFactory.createUserAgentHeader(uaList);
            }
            catch (ParseException e1)
            {
                e1.printStackTrace();
            }
            if (null != uaHeader)
            {
                request.addHeader(uaHeader);
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return request;
    }

    private void startMediaReceive()
    {

        mediaProvider = call.getMediaProvider();
        if (null != mediaProvider)
        {
            call.setLocalSdpAddress(callMgr.getContactIp());
            call.setLocalSdpPort(receivePort);
            mediaProvider.initializeRtpReceive(callMgr,
                    this.line.getHandle(),
                    this.call.getHandle(),
                    callMgr.getLocalIp(),
                    receivePort);
            mediaProvider.setVolume(initialVolume);
            call.setVolume(initialVolume);
        }
    }
    
    private void startMediaSend()
    {
        destPort = call.getRemoteSdpPort();
        destIp = call.getRemoteSdpAddress();
        
        mediaProvider = call.getMediaProvider();
        if (null != mediaProvider)
        {
            mediaProvider.initializeRtpSend(callMgr,
                    this.call.getHandle(),
                    destIp, destPort,
                    call.getLocalSdpPort(),
                    mediaSourceType,
                    mediaFilename,
                    loop);
            mediaProvider.setMicrophoneGain(initialGain);
        };
    }
    
}
