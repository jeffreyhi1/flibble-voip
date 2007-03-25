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
import javax.sip.address.Address;
import javax.sip.header.ReferToHeader;
import javax.sip.message.Request;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;

public class ReferAction extends ActionThread
{
    private ReferActionType referType;
    private String targetUri;
    
    public enum ReferActionType
    {
        CONSULTATIVE,
        BLIND,
    }
    public ReferAction(CallManager callMgr,
                       Call call,
                       String targetUri,
                       ReferActionType referType)
    {
        super(callMgr, call, null);
        this.callMgr = callMgr;
        this.call = call;
        this.targetUri = targetUri;
        this.referType = referType;
    }
    
    public void run()
    {
        if (referType == ReferActionType.BLIND)
        {
            blindTransfer();
        }
    }
    
    public void blindTransfer()
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
            .getProvider(callMgr);
        Dialog dialog = call.getDialog();
        Request refer = null;
        Address referToAddress = null;
        ReferToHeader referToHeader = null;
        try
        {
            refer = dialog.createRequest(Request.REFER);
            referToAddress = flibbleProvider.addressFactory.createAddress(targetUri);
            referToHeader = flibbleProvider.headerFactory.createReferToHeader(referToAddress);
            refer.setHeader(referToHeader);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        if (null != refer)
        {
            ClientTransaction ct = flibbleProvider.sendRequest(refer);
            //ResponseEvent responseEvent = flibbleProvider.waitForResponseEvent(ct);
            // response should be 200 or 202..
//            if (responseEvent != null &&
//                responseEvent.getResponse() != null /*&&
//                (responseEvent.getResponse().getStatusCode() % 100) == 2*/) 
//                // dont care about repsonse code for blind transfers
            {
                InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL,
                        EventCode.CALL_TRANSFER,
                        EventReason.CALL_TRANSFER_AS_CONTROLLER,
                        line.getHandle(),
                        call.getHandle()));                
                // we should now send a BYE to the Transferee
                ByeAction bye = new ByeAction(callMgr, call);
                bye.run();
            }
            /*
            else
            {
                InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL,
                        EventCode.CALL_TRANSFER_FAILED,
                        EventReason.CALL_TRANSFER_AS_CONTROLLER,
                        line.getHandle(),
                        call.getHandle()));                
                
            }
            */
        }
    }
}

