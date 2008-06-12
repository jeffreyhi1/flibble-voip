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
package com.sipresponse.flibblecallmgr.internal.handlers;

import java.util.Random;

import javax.sip.RequestEvent;
import javax.sip.ServerTransaction;
import javax.sip.header.ToHeader;
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;
import com.sipresponse.flibblecallmgr.internal.SipMessageProcessor;

public abstract class Handler extends SipMessageProcessor
{
    protected RequestEvent requestEvent;
    
    public Handler(CallManager callMgr,
                   Call call,
                   Line line,
                   RequestEvent requestEvent)
    {
        this.callMgr = callMgr;
        this.call = call;
        if (call != null && line == null)
        {
            try
            {
                this.line = InternalCallManager.getInstance().getLineManager(callMgr).getLine(call.getLineHandle());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (call == null && line != null)
        {
            this.line = line;
        }
        if (call != null)
        {
            call.setLastRequestEvent(requestEvent);
        }
        this.requestEvent = requestEvent;
    }
    
    public abstract void execute();
    
    public void sendResponse(int statusCode)
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
            .getProvider(callMgr);
        Response response = null;
        try
        {
            response = flibbleProvider.messageFactory.createResponse(
                    statusCode,
                    requestEvent.getRequest());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null != response)
        {
            ServerTransaction st = requestEvent.getServerTransaction();
            if (null == st)
            {
                st = call.getServerTransaction();
            }
            int count = 0;
            while (null == st && count < 40)
            {
                System.err.println("No server transaction for request: " + requestEvent.getRequest().toString());
                count++;
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                st = requestEvent.getServerTransaction();                
            }
            if (null == st)
            {
                System.err.println("COULD NOT GET server transaction for request: " + requestEvent.getRequest().toString());
                return;
            }
            try
            {
                if (200 > statusCode)
                {
                    Random rand = new Random();
                    rand.setSeed(System.currentTimeMillis());
                    Long tag = new Long(Math.abs(rand.nextLong()));
                    ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                    if (toHeader.getTag() == null)
                    {
                        call.setToTag(tag.toString());
                        toHeader.setTag(call.getToTag());   
                    }
                }
                
                System.out.println("Handler.sendResponse: " + response.toString());
                st.sendResponse(response);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
