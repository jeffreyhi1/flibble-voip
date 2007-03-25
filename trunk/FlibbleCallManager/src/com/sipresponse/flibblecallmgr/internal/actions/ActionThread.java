package com.sipresponse.flibblecallmgr.internal.actions;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;

public class ActionThread extends Thread
{
    protected int timeout = 4000;
    protected CallManager callMgr;
    protected Call call;
    protected Line line;
    
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
    
}
