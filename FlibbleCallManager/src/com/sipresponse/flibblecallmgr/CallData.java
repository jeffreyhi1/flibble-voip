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
        Address remoteAddress = call.getRemoteAddress();
        SipURI remoteUri = (SipURI) remoteAddress.getURI();
        String remoteNumber = remoteUri.getUser();
        return remoteNumber;
    }
    public String getRemoteName()
    {
        Address remoteAddress = call.getRemoteAddress();
        String displayName = remoteAddress.getDisplayName();
        
        System.err.println("Remote Name: " + displayName);
        return displayName;
    }
    public Address getRemoteAddress()
    {
        return call.getRemoteAddress();
    }
    
}
