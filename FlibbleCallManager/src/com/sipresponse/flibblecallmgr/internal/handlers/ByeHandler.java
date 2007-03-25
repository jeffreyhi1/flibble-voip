package com.sipresponse.flibblecallmgr.internal.handlers;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.InvalidArgumentException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;

public class ByeHandler extends Handler
{
    public ByeHandler(CallManager callMgr,
            Call call,
            RequestEvent requestEvent)
    {
        super(callMgr, call, null, requestEvent);
    }
    
    public void execute()
    {
        // fire a disconnected event
        InternalCallManager.getInstance().fireEvent(this.callMgr, new Event(EventType.CALL,
                EventCode.CALL_DISCONNECTED,
                EventReason.CALL_DISCONNECT_REMOTE,
                line.getHandle(),
                call.getHandle()));
        
        // stop media here?
        
        // send a 200 OK
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
            .getProvider(callMgr);
        Response response = null;
        try
        {
            flibbleProvider.messageFactory.createResponse(200, requestEvent.getRequest());
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
        // remove call from internal call manager
        InternalCallManager.getInstance().removeCallByHandle(call.getHandle());
        
    }
}
