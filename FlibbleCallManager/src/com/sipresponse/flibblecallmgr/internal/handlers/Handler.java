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
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;

public abstract class Handler
{
    protected Call call;
    protected Line line;
    protected CallManager callMgr;
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
            response = flibbleProvider.messageFactory.createResponse(100, requestEvent.getRequest());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null != response)
        {
            ServerTransaction st = requestEvent.getServerTransaction();
            try
            {
                st.sendResponse(response);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
