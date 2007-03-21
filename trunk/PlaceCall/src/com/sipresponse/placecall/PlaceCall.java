package com.sipresponse.placecall;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.FlibbleListener;
import com.sipresponse.flibblecallmgr.MediaSourceType;

public class PlaceCall implements FlibbleListener
{
    private boolean shouldExit = false;
    private CallManager callMgr = new CallManager();
    private String lineHandle = null;
    private String callHandle = null;

    private void go()
    {
        callMgr.initialize("192.168.0.103",
                5060,
                9300,
                9400,
                null,
                5060,
                false, null, true);
        callMgr.addListener(this);
        
        // create a registered line
        lineHandle = callMgr.addLine("sip:mike4@192.168.0.105", "Foo Bar", true, 300, "x616yzzy");
        
        // or, instead,
        // create a provisioned line, and place the call:
        // 
        // lineHandle = callMgr.addLine("sip:mike4@192.168.0.105", "Foo Bar", false, 0, null);
        //callHandle = callMgr.createCall(lineHandle, "sip:reliagility@192.168.0.103");
        //callMgr.placeCall(callHandle);
 
        
        // wait for 30 seconds for the line to register,
        // and for the call to go through
        int count = 0;
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
            count++;
            if (count > 3000)
            {
                shouldExit = true;
            }
        }
        callMgr.destroyCallManager();
        callMgr = null;
    }
    
    public boolean onEvent(Event event)
    {
        if (event.getEventType() == EventType.LINE)
        {
            System.err.println("Line Event:  " +  event.getEventCode() + ", " + event.getEventReason());
            
            // place the call if the line is registered
            if (event.getEventCode() == EventCode.LINE_REGISTERED)
            {
                callHandle = callMgr.createCall(lineHandle,
                        "sip:reliagility@192.168.0.103");
                callMgr.placeCall(callHandle,
                        MediaSourceType.MEDIA_SOURCE_NONE,
                        null);
            }
        }
        else if (event.getEventType() == EventType.CALL)
        {
            if (event.getEventCode() == EventCode.CALL_FAILED)
            {
                shouldExit = true;
            }
        }
        return false;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        PlaceCall placeCall = new PlaceCall();
        placeCall.go();

    }
}
