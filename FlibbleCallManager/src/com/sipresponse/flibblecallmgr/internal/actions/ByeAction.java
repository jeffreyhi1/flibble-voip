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

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.TransactionDoesNotExistException;
import javax.sip.TransactionUnavailableException;
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
import com.sipresponse.flibblecallmgr.internal.util.Signal;

public class ByeAction extends ActionThread
{
    private int timeout = 4000;
    
    public ByeAction(CallManager callMgr, Call call)
    {
        super(callMgr, call, null);
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
        Dialog dialog = call.getDialog();
        Request bye = null;
        try
        {
            bye = dialog.createRequest(Request.BYE);
        }
        catch (SipException e)
        {
            e.printStackTrace();
        }
        if (null != bye)
        {
            ClientTransaction ct = null;
            try
            {
                ct = flibbleProvider.sendDialogRequest(dialog, bye);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            ResponseEvent responseEvent = flibbleProvider.waitForResponseEvent(ct);
            // response should be 200 ok...
            
            InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL,
                    EventCode.CALL_DISCONNECTED,
                    EventReason.CALL_DISCONNECT_LOCAL,
                    line.getHandle(),
                    call.getHandle()));
            
        }
        
    }
}

