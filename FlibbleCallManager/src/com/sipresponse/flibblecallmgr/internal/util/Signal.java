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
package com.sipresponse.flibblecallmgr.internal.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sip.ResponseEvent;

public class Signal
{
    private Semaphore block = null;
    private Object data = null;
    private ResponseEvent responseEvent;
    public Signal()
    {
        block = new Semaphore(1, true);
        try
        {
            block.acquire();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public ResponseEvent waitForResponseEvent(long timeout) throws Exception
    {
        block.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        return responseEvent;
    }
    
    public boolean waitForSignal(long timeout)
    {
        boolean ret = false;
        try
        {
            ret = block.tryAcquire(timeout, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        return ret;
    }
    
    public boolean waitForSignal()
    {
        boolean ret = false;
        try
        {
            block.acquire();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
            return ret;
        }
        ret = true;
        return ret;
    }
    
    
    public void notifyResponseEvent()
    {
        block.release();
    }


    public void setResponseEvent(ResponseEvent responseEvent)
    {
        this.responseEvent = responseEvent;
    }

    public Object getData()
    {
        return data;
    }

    public void setData(Object data)
    {
        this.data = data;
    }
}
