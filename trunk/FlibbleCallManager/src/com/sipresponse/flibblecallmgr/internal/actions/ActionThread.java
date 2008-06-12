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

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;
import com.sipresponse.flibblecallmgr.internal.SipMessageProcessor;
import com.sipresponse.flibblecallmgr.internal.util.Signal;

public class ActionThread extends SipMessageProcessor
{
    protected int timeout = 4000;
    protected Signal signal;
    protected ActionThread(CallManager callMgr, Call call, Line line)
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
    }
    public int getTimeout()
    {
        return timeout;
    }
    
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }
    public void setNotifier(Signal signal)
    {
       this.signal = signal;
    }
    
}
