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
package com.sipresponse.flibblecallmgr.internal;

import java.util.concurrent.ConcurrentHashMap;

public class Call
{
    private static ConcurrentHashMap<String, Call> handleMap = 
        new ConcurrentHashMap<String, Call>();
    private static ConcurrentHashMap<String, Call> callIdMap = 
        new ConcurrentHashMap<String, Call>();
    private static int handleCounter = 0;
    
    private String callId;
    private String handle;
    private String lineHandle;
    private String sipUriString;
    
    public Call(String lineHandle,
            String sipUriString,
            String callId)
    {
        this.lineHandle = lineHandle;
        this.sipUriString = sipUriString;
        this.callId = callId;
        handle = Call.getNewHandle();
        handleMap.put(handle, this);
        callIdMap.put(callId, this);
    }
    public String getCallId()
    {
        return callId;
    }
    public void setCallId(String callId)
    {
        this.callId = callId;
    }
    public String getHandle()
    {
        return handle;
    }
    public void setHandle(String handle)
    {
        this.handle = handle;
    }
    public String getLineHandle()
    {
        return lineHandle;
    }
    public void setLineHandle(String lineHandle)
    {
        this.lineHandle = lineHandle;
    }
    public String getSipUriString()
    {
        return sipUriString;
    }
    public void setSipUriString(String sipUriString)
    {
        this.sipUriString = sipUriString;
    }
    public static Call getCallByHandle(String callHandle)
    {
        return handleMap.get(callHandle);
    }
    public static void removeCallByHandle(String callHandle)
    {
        handleMap.remove(callHandle);
        return;
    }
    public static synchronized String getNewHandle()
    {
        Call.handleCounter++;
        return new Integer(Call.handleCounter).toString();
    }
}
