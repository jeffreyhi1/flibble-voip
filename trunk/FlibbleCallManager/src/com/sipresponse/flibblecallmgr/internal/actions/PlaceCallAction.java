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

import gov.nist.javax.sip.Utils;

import java.text.ParseException;
import java.util.ArrayList;

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

public class PlaceCallAction extends ActionThread
{
    private int timeout = 60000;
    private MediaSourceType mediaSourceType;
    private String mediaFilename;
    private FlibbleSipProvider flibbleProvider;
    
    public PlaceCallAction(CallManager callMgr,
            Call call,
            MediaSourceType mediaSourceType,
            String mediaFilename)
    {
        super(callMgr, call, null);
        this.mediaSourceType = mediaSourceType;
        this.mediaFilename = mediaFilename;
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
            Request request = createRequest();
            setContent(request);
            ClientTransaction ct = flibbleProvider.sendRequest(request);
            ResponseEvent responseEvent = flibbleProvider.waitForResponseEvent(ct);
            if (null == responseEvent)
            {
                
            }
            else
            {
                int statusCode = responseEvent.getResponse().getStatusCode();
                while (true)
                {
                    if (statusCode >= 500)
                    {
                        EventReason eventReason = EventReason.CALL_FAILURE_NETWORK;
                        InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL, 
                                                                                            EventCode.CALL_FAILED,
                                                                                            eventReason,
                                                                                            line.getHandle(),
                                                                                            call.getHandle()));
                        break;
                    }
                    else if (statusCode == 401 || statusCode == 403)
                    {
                        // todo - reinvite with authentication
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
                        InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL,
                                                                                            EventCode.CALL_FAILED,
                                                                                            eventReason,
                                                                                            line.getHandle(),
                                                                                            call.getHandle()));
                        
                        break;
                    }
                    else if (statusCode == 183 || statusCode == 180)
                    {
                        // todo - fire a remote ringing event
                        responseEvent = flibbleProvider.waitForResponseEvent(ct);
                    }
                    else if (statusCode < 200)
                    {
                        responseEvent = flibbleProvider.waitForResponseEvent(ct);
                    }
                    else if (statusCode >= 200 && statusCode < 400)
                    {
                        flibbleProvider.ackResponse(responseEvent);
                        InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL,
                                EventCode.CALL_CONNECTED,
                                EventReason.CALL_NORMAL,
                                line.getHandle(),
                                call.getHandle()));
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
                call.createLocalSdp(null, null);
                localSdp = call.getLocalSdp(); 
                contentTypeHeader = flibbleProvider.headerFactory.createContentTypeHeader("application", "sdp");
                contentLengthHeader = flibbleProvider.headerFactory.createContentLengthHeader(localSdp.toString().length());
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
        LineManager lineMgr = InternalCallManager.getInstance().getLineManager(callMgr);
        Line fromLine = lineMgr.getLine(call.getLineHandle());
        String fromUser = fromLine.getUser();
        String fromHost = fromLine.getHost();
        String fromDisplayName = fromLine.getDisplayName();
        Request request = null;

        try
        {
            String toUriString = call.getSipUriString();
            SipURI toUri = (SipURI)flibbleProvider.addressFactory.createURI(toUriString);
    
            // create >From Header
            SipURI fromAddress = flibbleProvider.addressFactory.createSipURI(fromUser,fromHost);
    
            Address fromNameAddress = flibbleProvider.addressFactory.createAddress(fromAddress);
            fromNameAddress.setDisplayName(fromDisplayName);
            FromHeader fromHeader = flibbleProvider.headerFactory.createFromHeader(fromNameAddress,
                    Utils.generateTag());
            
            // create To Header
            Address toNameAddress = flibbleProvider.addressFactory.createAddress(toUri);
            ToHeader toHeader = flibbleProvider.headerFactory.createToHeader(toNameAddress,null);
    
            // create Contact Header
            SipURI contactUri = flibbleProvider.addressFactory.createSipURI(fromUser, callMgr.getLocalIp());
            Address contactAddress = flibbleProvider.addressFactory.createAddress(contactUri);
            ((SipURI)contactAddress.getURI()).setPort(callMgr.getUdpSipPort());
            ContactHeader contactHeader = flibbleProvider.headerFactory.createContactHeader(contactAddress);
            
            // Create ViaHeaders
            ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
            ViaHeader viaHeader = flibbleProvider.headerFactory.createViaHeader(callMgr.getLocalIp(), sipProvider.getListeningPoint("udp").getPort(),"udp", null);
            // add via headers
            viaHeaders.add(viaHeader);
    
    
            // Create a new CallId header
            CallIdHeader callIdHeader = flibbleProvider.headerFactory.createCallIdHeader(call.getCallId());
    
            // Create a new Cseq header
            CSeqHeader cSeqHeader = flibbleProvider.headerFactory.createCSeqHeader((long)1,Request.INVITE);
    
            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = flibbleProvider.headerFactory.createMaxForwardsHeader(70);
    
            // Create the request.
            request = flibbleProvider.messageFactory.createRequest(toUri,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            request.setHeader(contactHeader);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return request;
    }
}
