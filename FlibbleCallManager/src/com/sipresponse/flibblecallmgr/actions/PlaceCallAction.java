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
package com.sipresponse.flibblecallmgr.actions;

public class PlaceCallAction extends Thread
{
    private String sipUri;
    private int timeout = 60000;
    private String lineHandle;
    public PlaceCallAction()
    {
        
    }
    
    public String getLineHandle()
    {
        return lineHandle;
    }
    
    public void setLineHandle(String lineHandle)
    {
        this.lineHandle = lineHandle;
    }
    
    public String getSipUri()
    {
        return sipUri;
    }
    
    public void setSipUri(String sipUri)
    {
        this.sipUri = sipUri;
    }
    
    public int getTimeout()
    {
        return timeout;
    }
    
    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }
    
    public void run()
    {
    }
}
