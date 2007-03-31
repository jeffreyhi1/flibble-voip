package com.sipresponse.flibblecallmgr.internal;

import java.util.Vector;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.internal.actions.RegisterAction;

public class RegistrationManager extends Thread
{
    private CallManager callMgr;
    private boolean shutdown = false;
    public RegistrationManager(CallManager callMgr)
    {
        this.callMgr = callMgr;
    }
    
    public void shutdown()
    {
        shutdown = true;
    }
    
    public void run()
    {
        while (!shutdown)
        {
            try
            {
                Thread.sleep(5000);
            }
            catch (InterruptedException e)
            {
                return;
            }
            LineManager lineMgr = InternalCallManager.getInstance().getLineManager(callMgr);
            Vector<Line> linesVector = lineMgr.getLines();
            long now = java.lang.System.currentTimeMillis();
            for (Line line : linesVector)
            {
                
                if (line.getRegisterEnabled() == true &&
                        line.getStatus() != EventCode.LINE_UNREGISTERED &&
                        line.getStatus() != EventCode.LINE_UNREGISTERING &&
                        line.getStatus() != EventCode.LINE_REGISTERING &&
                            (line.getLastRegisterTimestamp() == 0 ||
                                    ((now - line.getLastRegisterTimestamp()) / 1000) > (0.50 * line.getRegisterPeriod() )
                            )
                   )
                {
                    new RegisterAction(callMgr, line).start();
                }
            }
        }
    }

}
