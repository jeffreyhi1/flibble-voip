package com.sipresponse.flibblecallmgr;

import javax.sip.address.Address;
import javax.sip.address.SipURI;

import com.sipresponse.flibblecallmgr.internal.Call;

public class CallData
{
    private Call call;
    public CallData(Call call)
    {
        this.call = call;
    }
    public String getCallHandle()
    {
        return call.getHandle();
    }
    public Event getCallStatus()
    {
        return call.getLastCallEvent();
    }
    public boolean isFromThisSide()
    {
        return call.isFromThisSide();
    }
    public String getLineHandle()
    {
        return call.getLineHandle();
    }
    public String getRemoteNumber()
    {
        return ((SipURI)call.getRemoteAddress().getURI()).getUser();
    }
    public String getRemoteName()
    {
        return getRemoteAddress().getDisplayName();
    }
    public Address getRemoteAddress()
    {
        return call.getRemoteAddress();
    }
    
}
