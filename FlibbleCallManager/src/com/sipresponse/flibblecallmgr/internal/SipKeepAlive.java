package com.sipresponse.flibblecallmgr.internal;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.actions.OptionsAction;

public class SipKeepAlive extends Thread
{
    private boolean stop = false;
    private CallManager callMgr;
    private Line line;
    public SipKeepAlive(CallManager callMgr,
                        Line line)
    {
        this.callMgr = callMgr;
        this.line = line;
        start();
    }
    public void shutdown()
    {
        stop = true;
        interrupt();
    }
    public void run()
    {
        while (!stop)
        {
            try
            {
                Thread.sleep(20000);
            }
            catch (InterruptedException e)
            {
                stop = true;
                break;
            }
            OptionsAction optionsAction = new OptionsAction(callMgr, line);
            optionsAction.start();
        }
    }
}
