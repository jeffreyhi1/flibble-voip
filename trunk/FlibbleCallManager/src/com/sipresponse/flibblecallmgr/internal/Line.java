/*******************************************************************************
 *   Copyright 2007-2008 SIP Response
 *   Copyright 2007-2008 Michael D. Cohen
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

import javax.sip.address.SipURI;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.EventType;

public class Line
{
    private SipURI sipUri;
    private String displayName;
    private boolean register;
    private String handle;
    private long lastRegisterTimestamp;
    private EventCode status;
    private int registerPeriod = -1;
    private CallManager callMgr;
    private String password;
    
    public Line(CallManager callMgr)
    {
        this.callMgr = callMgr;
    }
    public String getHandle()
    {
        return handle;
    }
    public void setHandle(String handle)
    {
        this.handle = handle;
    }
    public boolean getRegisterEnabled()
    {
        return register;
    }
    public void setRegisterEnabled(boolean register)
    {
        this.register = register;
    }
    public SipURI getSipUri()
    {
        return sipUri;
    }
    public void setSipUri(SipURI sipUrl)
    {
        this.sipUri = sipUrl;
    }
    public String getHost()
    {
        return sipUri.getHost();
    }
    public String getUser()
    {
        return sipUri.getUser();
    }
    public String getDisplayName()
    {
        return displayName;
    }
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }
    public long getLastRegisterTimestamp()
    {
        return lastRegisterTimestamp;
    }
    public void setLastRegisterTimestamp(long lastRegisterTimestamp)
    {
        this.lastRegisterTimestamp = lastRegisterTimestamp;
    }
    public EventCode getStatus()
    {
        return status;
    }
    public void setStatus(EventCode status, EventReason reason)
    {
        this.status = status;
        
        InternalCallManager.getInstance().fireEvent(
                callMgr,
                new Event(EventType.LINE, status, reason, handle, null));        
    }
    public int getRegisterPeriod()
    {
        return registerPeriod;
    }
    public void setRegisterPeriod(int registerPeriod)
    {
        this.registerPeriod = registerPeriod;
    }
    public String getPassword()
    {
        return password;
    }
    public void setPassword(String password)
    {
        this.password = password;
    }    
    
}
