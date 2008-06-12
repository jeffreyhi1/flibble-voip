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

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
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
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;
import com.sipresponse.flibblecallmgr.internal.util.AuthenticationHelper;

public class CancelAction extends ActionThread
{

    public CancelAction(CallManager callMgr, Call call)
    {
        super(callMgr, call, null);
    }

    @Override
    public void run()
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
            .getProvider(callMgr);
        FlibbleMediaProvider mediaProvider = call.getMediaProvider();
        if (null != mediaProvider)
        {
            mediaProvider.stopRtpReceive(call.getLocalSdpAddress(), call.getLocalSdpPort());
        }
        
        Dialog dialog = call.getDialog();
        Request cancel = null;
        ClientTransaction originalInviteTransaction = call.getClientTransaction();
        ClientTransaction ct = null;
        try
        {
            cancel = originalInviteTransaction.createCancel();
            ViaHeader viaHeader = (ViaHeader)cancel.getHeader(ViaHeader.NAME);
            viaHeader.setRPort();            
            
            originalInviteTransaction.terminate();
            ct = flibbleProvider.getSipProvider().getNewClientTransaction(cancel);
            ct.sendRequest();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        ResponseEvent responseEvent = flibbleProvider.waitForResponseEvent(ct);
        // response should be 200 ok...
        
    }
    

}
