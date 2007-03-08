package com.sipresponse.placecall;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.FlibbleListener;

public class PlaceCall implements FlibbleListener
{
    private boolean shouldExit = false;

    public void go()
    {
        CallManager callMgr = new CallManager();
        callMgr.initialize("192.168.0.101",
                5060,
                9300,
                9400,
                null,
                5060,
                false, true);
        callMgr.addListener(this);
        String lineHandle = callMgr.addLine("sip:foo@192.168.0.101", "Foo Bar", false);
        String callHandle = callMgr.createCall(lineHandle, "sip:reliagility@192.168.0.103");
        callMgr.placeCall(callHandle);
        
        while (shouldExit == false)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        callMgr.destroy();
        callMgr = null;

    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        PlaceCall placeCall = new PlaceCall();
        placeCall.go();

    }
    public boolean onEvent(Event event)
    {
        
        if (event.getEventType() == EventType.CALL)
        {
            if (event.getEventCode() == EventCode.CALL_FAILED)
            {
                shouldExit = true;
            }
        }
        return false;
    }

}
