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
import java.net.InetAddress;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.FlibbleListener;
import com.sipresponse.flibblecallmgr.MediaSourceType;


public class UserAgent implements FlibbleListener
{
    private String proxy = "sphone.vopr.vonage.net";
    private static UserAgent instance;
    private CallManager callMgr = new CallManager();
    private String lineHandle = null;
    private String callHandle = null;        
    private boolean inCall;
    
    public static synchronized UserAgent getInstance()
    {
        if (null == instance)
        {
            instance = new UserAgent();
        }
        return instance;
    }
    
    public void init()
    {
        try
        {
            callMgr.initialize(InetAddress.getLocalHost().getHostAddress(),  // address to bind to
                    5060, // port to bind to 
                    9300, // start media port range
                    9400, // end media port range
                    proxy, // proxy address
                    5060, // proxy port
                    null, // stun server
                    true, // use sound card
                    null);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        callMgr.addListener(this);  // this class implements the FlibbleListener interface     
        
        // create a registered line
        lineHandle = callMgr.addLine("sip:17815552814@sphone.vopr.vonage.net", "Foo Bar", true, 20, "pwd");
    }
    
    public void onDialPad(int code)
    {
        if (inCall == false)
        {
            if (code < 10)
            {
                MainForm.getInstance().addToDialString(new String(new Integer(code).toString()));
            }
            else if (code == 10)
            {
                MainForm.getInstance().addToDialString(new String("*"));
            }
            else if (code == 11)
            {
                MainForm.getInstance().addToDialString(new String("*"));
            }
        }
        else
        {
            callMgr.sendDtmf(callHandle, code);
        }
            
    }

    public void answerCall()
    {
        callMgr.answerCall(callHandle, MediaSourceType.MEDIA_SOURCE_MICROPHONE, null, false);
    }
    
    public void onCallButtonPressed(String dialString)
    {
        if (null == callHandle)
        {
            if (dialString.length() == 10)
            {
                dialString = "1" + dialString;
            }
            callHandle = callMgr.createCall(lineHandle,
                    "sip:"+ dialString + "@" + proxy );
            callMgr.placeCall(callHandle,
                    MediaSourceType.MEDIA_SOURCE_MICROPHONE,
                    null,
                    false);
        }
        else
        {
            answerCall();
        }
    }
    public void bye()
    {
        callMgr.endCall(callHandle);
        callHandle = null;
        inCall = false;
    }
    
    public boolean onEvent(Event event)
    {
        if (event.getEventType() == EventType.LINE)
        {
            System.err.println("Line Event:  " +  event.getEventCode() + ", " + event.getEventReason());
            
            // place the call if the line is registered
            if (event.getEventCode() == EventCode.LINE_REGISTERED)
            {
                if (false == inCall)
                {
                }
            }
        }
        else if (event.getEventType() == EventType.CALL)
        {
            if (event.getEventCode() == EventCode.CALL_INCOMING_INVITE)
            {
                callHandle = event.getCallHandle();
                // accept the call with a 180 ringing
                callMgr.acceptCall(callHandle, 180);
                MainForm.getInstance().setIncomingCallerId((String)event.getInfo());
            }
            else if (event.getEventCode() == EventCode.CALL_CONNECTED)
            {
                inCall = true;
            }
            else if (event.getEventCode() == EventCode.CALL_FAILED ||
                event.getEventCode() == EventCode.CALL_DISCONNECTED)
            {
                inCall = false;
                callHandle = null;
            }
        }
        return false;
    }

}
