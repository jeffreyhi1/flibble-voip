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
