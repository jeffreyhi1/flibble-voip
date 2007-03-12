package com.sipresponse.flibblecallmgr.internal;

import java.util.Vector;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.internal.actions.RegisterAction;

public class RegistrationManager extends Thread
{
    private CallManager callMgr;
    public RegistrationManager(CallManager callMgr)
    {
        this.callMgr = callMgr;
    }
    
    public void run()
    {
        try
        {
            Thread.sleep(5000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return;
        }
        LineManager lineMgr = InternalCallManager.getInstance().getLineManager(callMgr);
        Vector<Line> linesVector = lineMgr.getLines();
        long now = java.lang.System.currentTimeMillis();
        for (Line line : linesVector)
        {
            
            if (line.getRegisterEnabled() == true &&
                    line.getStatus() != RegisterStatus.UNREGISTERED &&
                    line.getStatus() != RegisterStatus.UNREGISTERING &&
                    line.getStatus() != RegisterStatus.REGISTERING &&
                        (line.getLastRegisterTimestamp() == 0 ||
                                (now - line.getLastRegisterTimestamp()) > (0.50 * line.getRegisterPeriod() )
                        )
               )
            {
                line.setStatus(RegisterStatus.REGISTERING, EventReason.LINE_NORMAL);
                new RegisterAction(callMgr, line).start();
            }
        }
    }

}
