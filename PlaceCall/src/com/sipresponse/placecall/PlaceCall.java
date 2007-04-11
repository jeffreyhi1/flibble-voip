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
        callMgr.initialize("192.168.0.203",
                5060,
                9300,
                9400,
                null,
                5060,
                false, null, true, null);
        callMgr.addListener(this);
        
        // create a registered line
        lineHandle = callMgr.addLine("sip:mike4@192.168.0.105", "Foo Bar", true, 300, "bink616");
        
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
            if (count > 300)
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
                        "sip:mike5@192.168.0.105");
                callMgr.placeCall(callHandle,
                        MediaSourceType.MEDIA_SOURCE_MICROPHONE,
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
