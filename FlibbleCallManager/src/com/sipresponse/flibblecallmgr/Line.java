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
package com.sipresponse.flibblecallmgr;

import javax.sip.address.SipURI;

public class Line
{
    private SipURI sipUri;
    private String displayName;
    private boolean register;
    private String handle;
    public String getHandle()
    {
        return handle;
    }
    public void setHandle(String handle)
    {
        this.handle = handle;
    }
    public boolean isRegister()
    {
        return register;
    }
    public void setRegister(boolean register)
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
    
}
