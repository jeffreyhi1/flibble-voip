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
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
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
        FlibbleSipProvider flibbleProvider = callMgr.getProvider();
        SipProvider sipProvider = flibbleProvider.sipProvider;
        LineManager lineMgr = callMgr.getLineManager();
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
            ResponseEvent response = flibbleProvider.sendRequest(request);
            flibbleProvider.ackResponse(response);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
