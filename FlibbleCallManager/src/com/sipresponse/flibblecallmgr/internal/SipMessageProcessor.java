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
package com.sipresponse.flibblecallmgr.internal;

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

public abstract class SipMessageProcessor extends Thread
{
    protected Call call;
    protected Line line;
    protected CallManager callMgr;

    public SipMessageProcessor()
    {
    }

    public void sendResponse(int statusCode)
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
                .getProvider(callMgr);
        Response response = null;
        try
        {
            if (call.getServerTransaction() != null)
            {
                response = flibbleProvider.messageFactory.createResponse(statusCode,
                        call.getServerTransaction().getRequest());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null != response)
        {
            ServerTransaction st = call.getLastRequestEvent().getServerTransaction();
            if (null == st)
            {
                st = call.getServerTransaction();
            }
            if (null == st)
            {
                return;
            }
            try
            {
                //if (200 >= statusCode)
                {
                    ToHeader toHeader = (ToHeader) response.getHeader(ToHeader.NAME);
                    if (toHeader.getTag() == null)
                    {
                        toHeader.setTag(call.getToTag());   
                    }
                }
               
                st.sendResponse(response);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
