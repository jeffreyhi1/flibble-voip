package com.sipresponse.flibblecallmgr.internal.actions;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.Line;

public class RegisterAction extends Thread
{
    private int timeout = 8000;
    private CallManager callMgr;
    private Line line;
    
    public RegisterAction(CallManager callMgr, Line line)
    {
        this.callMgr = callMgr;
        this.line = line;
    }
    public void run()
    {
        
    }

}
