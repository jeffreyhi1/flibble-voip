package com.sipresponse.flibblecallmgr;

import java.text.ParseException;
import java.util.Date;

import javax.sip.address.Address;
import javax.sip.address.SipURI;

import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;

public class CallData
{
    private Call call;
    private Date startTime = new Date();
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
        String displayName = null;
        
        if (call.isFromThisSide())
        {
            FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance().getProvider(call.getCallMgr());
            SipURI sipUri = null;
            try
            {
                sipUri = (SipURI)flibbleProvider.addressFactory.createURI(call.getSipUriString());
            }
            catch (ParseException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            displayName = sipUri.getUser();
            //call.getSipUriString()
        }
        else
        {
            Address remoteAddress = call.getRemoteAddress();
            displayName = remoteAddress.getDisplayName();
        }
        return displayName;
    }
    public Address getRemoteAddress()
    {
        return call.getRemoteAddress();
    }
    public Date getStartTime()
    {
        return startTime;
    }
    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }
    
}
