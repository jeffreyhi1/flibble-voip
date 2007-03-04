package com.sipresponse.flibblecallmgr.util;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import javax.sip.ResponseEvent;
import javax.sip.message.Response;

public class Signal
{
    private Semaphore block = null;
    private ResponseEvent responseEvent;
    public Signal()
    {
        super();
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
    
    public void notifyResponseEvent()
    {
        block.release();
    }


    public void setResponseEvent(ResponseEvent responseEvent)
    {
        this.responseEvent = responseEvent;
    }
}
