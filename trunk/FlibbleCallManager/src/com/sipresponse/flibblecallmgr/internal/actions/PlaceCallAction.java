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

import java.util.ArrayList;

import javax.sip.ClientTransaction;
import javax.sip.ResponseEvent;
import javax.sip.SipProvider;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
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
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;
import com.sipresponse.flibblecallmgr.internal.LineManager;

public class PlaceCallAction extends Thread
{
    private int timeout = 60000;
    private CallManager callMgr;
    private Call call;
    
    public PlaceCallAction(CallManager callMgr, Call call)
    {
        this.callMgr = callMgr;
        this.call = call;
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
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
            .getProvider(callMgr);
        SipProvider sipProvider = flibbleProvider.sipProvider;
        LineManager lineMgr = InternalCallManager.getInstance().getLineManager(callMgr);
        Line fromLine = lineMgr.getLine(call.getLineHandle());
        
        try
        {
            String fromUser = fromLine.getUser();
            String fromHost = fromLine.getHost();
            String fromDisplayName = fromLine.getDisplayName();
    
            SipURI toUri = (SipURI)flibbleProvider.addressFactory.createURI(call.getSipUriString());
    
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
            ContactHeader contactHeader = flibbleProvider.headerFactory.createContactHeader(contactAddress);
            
            // Create ViaHeaders
            ArrayList viaHeaders = new ArrayList();
            ViaHeader viaHeader = flibbleProvider.headerFactory.createViaHeader(callMgr.getLocalIp(), sipProvider.getListeningPoint("udp").getPort(),"udp", null);
            // add via headers
            viaHeaders.add(viaHeader);
    
            // Create ContentTypeHeader
            ContentTypeHeader contentTypeHeader = flibbleProvider.headerFactory.createContentTypeHeader("application", "sdp");
    
            // Create a new CallId header
            CallIdHeader callIdHeader;
            callIdHeader = sipProvider.getNewCallId();
    
            // Create a new Cseq header
            CSeqHeader cSeqHeader = flibbleProvider.headerFactory.createCSeqHeader(1,Request.INVITE);
    
            // Create a new MaxForwardsHeader
            MaxForwardsHeader maxForwards = flibbleProvider.headerFactory.createMaxForwardsHeader(70);
    
            // Create the request.
            Request request = flibbleProvider.messageFactory.createRequest(toUri,
                    Request.INVITE, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwards);
            request.setHeader(contactHeader);
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
                        // todo - fire a failure event
                        break;
                    }
                    else if (statusCode == 401 || statusCode == 403)
                    {
                        // todo - reinvite with authentication
                    }
                    else if (statusCode > 400)
                    {
                        EventReason eventReason = EventReason.CALL_FAILURE_NETWORK;
                        InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL, EventCode.CALL_FAILED, eventReason));
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
                        // todo - fire a connected event
                        flibbleProvider.ackResponse(responseEvent);
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
}
