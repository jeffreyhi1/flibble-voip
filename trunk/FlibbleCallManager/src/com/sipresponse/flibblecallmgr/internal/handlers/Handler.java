package com.sipresponse.flibblecallmgr.internal.handlers;

import javax.sip.RequestEvent;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.Call;
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
}
