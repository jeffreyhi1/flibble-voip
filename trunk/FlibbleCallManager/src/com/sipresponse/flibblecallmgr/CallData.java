package com.sipresponse.flibblecallmgr;

import java.text.ParseException;
import java.util.Date;

import javax.sip.address.Address;
import javax.sip.address.SipURI;

import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.util.StringUtil;

/**
 * Contains information related to a single call.
 * @author Michael Cohen
 *
 */
public class CallData
{
    private Call call;
    private Date startTime = new Date();
    
    /**
     * Constructor (INTERNAL USE ONLY)
     * @param The Call object that this data pertains to.
     */
    public CallData(Call call)
    {
        this.call = call;
    }
    
    /**
     * Gets an opaque string handle for the call.
     * @return the call handle
     */
    public String getCallHandle()
    {
        return call.getHandle();
    }
    /**
     * Gets the last known status of the call, in the form of an Event object.
     * @return The last event object associated with this call.
     */
    public Event getCallStatus()
    {
        return call.getLastCallEvent();
    }
    
    /**
     * Returns true if the call originated locally, otherwise returns false.
     * @return Local origination indicator.
     */
    public boolean isFromThisSide()
    {
        return call.isFromThisSide();
    }
    /**
     * Returns an opaque string handle for this call's line.
     * @return the line handle
     */
    public String getLineHandle()
    {
        return call.getLineHandle();
    }
    
    /**
     * Returns the user portion of the sip address of the remote endpoint. 
     * @return The user portion of the sip address of the remote endopoint.
     */
    public String getRemoteNumber()
    {
        Address remoteAddress = call.getRemoteAddress();
        SipURI remoteUri = (SipURI) remoteAddress.getURI();
        String remoteNumber = remoteUri.getUser();
        return remoteNumber;
    }
    
    /**
     * Returns the display name of the remote endpoint. 
     * @return The display name of the remote endopoint.
     */
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
            SipURI fromURI = (SipURI) remoteAddress.getURI();            
            if (!StringUtil.hasDigits(displayName))
            {
                displayName += " " + StringUtil.stripAllButNumbers(fromURI.getUser(), true);
            }            
        }
        return displayName;
    }
    /**
     * Returns the sip address of the remote endpoint. 
     * @return The sip address of the remote endopoint.
     */
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
    
    public int getVolume()
    {
        return call.getVolume();
    }
    
    /*
     * Internal use only
     */
    Call getCall()
    {
        return call;
    }
    
    
}
