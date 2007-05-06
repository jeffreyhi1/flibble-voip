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
package com.sipresponse.receivecall;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.FlibbleListener;
import com.sipresponse.flibblecallmgr.MediaSourceType;

public class ReceiveCall implements FlibbleListener
{
    private CallManager callMgr = new CallManager();
    private String lineHandle = null;
    private String callHandle = null;

    private void go()
    {
        callMgr.initialize("192.168.0.203",
                5060,
                8000,
                8020,
                "sphone.vopr.vonage.net",
                5061,
                false,
                null,
                true,
                null);
        callMgr.addListener(this);
        
        // create a registered line
        lineHandle = callMgr.addLine("sip:17815552814@sphone.vopr.vonage.net", "Foo Bar", true, 40, "P@ssw0rd");
        
        while (true)
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
    
    public synchronized boolean onEvent(Event event)
    {
        if (event.getEventType() == EventType.LINE)
        {
            System.err.println("Line Event:  " + event.getLineHandle() + ": " + event.getEventCode() + ", " + event.getEventReason());
        }
        else if (event.getEventType() == EventType.CALL)
        {
            System.err.println("Call Event:  " + event.getCallHandle() + ": " + event.getEventCode() + ", " + event.getEventReason());
            if (event.getEventCode() == EventCode.CALL_INCOMING_INVITE)
            {
                callHandle = event.getCallHandle();
                // accept the call with a 180 ringing
                callMgr.acceptCall(callHandle, 180);
                
                // answer the call
                callMgr.answerCall(callHandle, MediaSourceType.MEDIA_SOURCE_MICROPHONE, null);
            }
        }
        return false;
    }

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ReceiveCall placeCall = new ReceiveCall();
        placeCall.go();
        System.exit(0);

    }
}
