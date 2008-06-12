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
import java.util.Vector;

import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ResponseEvent;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.Request;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;

public class OptionsAction extends ActionThread
{
    private int timeout = 4000;
    
    public OptionsAction(CallManager callMgr, Line line)
    {
        super(callMgr, null, line);
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
        Request options = null;
        options = flibbleProvider.createRequest(Utils
                .generateCallIdentifier(callMgr.getLocalIp()),
                Request.OPTIONS, line.getSipUri(), line.getSipUri());
        ExpiresHeader expiresHeader = null;
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
            options.addHeader(uaHeader);
        }
        try
        {
            if (line.getRegisterPeriod() > 0)
            {
                expiresHeader = flibbleProvider.headerFactory
                        .createExpiresHeader(line.getRegisterPeriod());
            }
        }
        catch (InvalidArgumentException e)
        {
            e.printStackTrace();
        }

        if (null != expiresHeader)
        {
            options.setExpires(expiresHeader);
        }
        // add via headers
        try
        {
            ViaHeader viaHeader = null;
            if (callMgr.getPublicIp() != null)
            {
                 viaHeader = flibbleProvider.headerFactory
                .createViaHeader(callMgr.getPublicIp(), flibbleProvider.sipProvider
                        .getListeningPoint("udp").getPort(), "udp", null);
            }
             else
             {
             
                viaHeader = flibbleProvider.headerFactory
                        .createViaHeader(callMgr.getLocalIp(), flibbleProvider.sipProvider
                                .getListeningPoint("udp").getPort(), "udp", null);
             }
            viaHeader.setRPort();
            options.setHeader(viaHeader);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        
        if (options != null)
        {

            // create the transaction
            ClientTransaction ct = flibbleProvider.sendRequest(options);

            if (ct == null)
            {
                return;            
            }
            
            // wait for a response
            ResponseEvent responseEvent = flibbleProvider
                    .waitForResponseEvent(ct);
            if (null != responseEvent)
            {
            }
            
        }
        
    }
}


