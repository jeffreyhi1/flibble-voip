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
package com.sipresponse.flibblecallmgr.internal;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.FlibbleListener;

public class InternalCallManager
{
    private Object vectorSync = new Object();
    private ConcurrentHashMap<CallManager, Vector<FlibbleListener>> flibbleListenerVectors = 
        new ConcurrentHashMap<CallManager, Vector<FlibbleListener>>();
    private ConcurrentHashMap callMap = new ConcurrentHashMap(); 
    private ConcurrentHashMap<CallManager, FlibbleSipProvider> sipProviders = 
        new ConcurrentHashMap<CallManager, FlibbleSipProvider>();
    private ConcurrentHashMap<CallManager, LineManager> lineManagers = 
        new ConcurrentHashMap<CallManager, LineManager>();
    
    private ConcurrentHashMap<String, Call> handleMap = 
        new ConcurrentHashMap<String, Call>();
    private ConcurrentHashMap<String, Call> callIdMap = 
        new ConcurrentHashMap<String, Call>();
    private int handleCounter = 0;
    
    private static InternalCallManager instance;
    public synchronized static InternalCallManager getInstance()
    {
        if (null == instance)
        {
            instance = new InternalCallManager();
        }
        return instance;
    }
    
    protected InternalCallManager()
    {
    }
    
    public FlibbleSipProvider getProvider(CallManager callManager)
    {
        return sipProviders.get(callManager);
    }

    public void setProvider(CallManager callManager, FlibbleSipProvider provider)
    {
        sipProviders.put(callManager, provider);
    }
    
    public LineManager getLineManager(CallManager callManager)
    {
        return lineManagers.get(callManager);
    }

    public void setLineManager(CallManager callManager, LineManager lineManager)
    {
        lineManagers.put(callManager, lineManager);
    }
    
    public void fireEvent(CallManager callManager, Event event)
    {
        synchronized (vectorSync)
        {
            Vector<FlibbleListener> listeners = flibbleListenerVectors.get(callManager);
            for (FlibbleListener listener : listeners)
            {
                listener.onEvent(event);
            }
        }
        return; 
    }
    
    public void addListener(CallManager callManager, FlibbleListener listener)
    {
        // try to get the vector
        Vector<FlibbleListener> listeners = flibbleListenerVectors.get(callManager);
        if (null == listeners)
        {
            // add the vector
            listeners = new Vector<FlibbleListener>();
            flibbleListenerVectors.put(callManager, listeners);
        }
        listeners.add(listener);
    }
    
    public void removeListener(CallManager callManager, FlibbleListener listener)
    {
        // try to get the vector
        Vector<FlibbleListener> listeners = flibbleListenerVectors.get(callManager);
        if (null != listeners)
        {
            // remove the listener
            listeners.remove(listener);
        }
    }    
    
    public void addCall(String handle, Call call)
    {
        handleMap.put(handle, call);
        callIdMap.put(call.getCallId(), call);
    }
    public Call getCallByHandle(String callHandle)
    {
        return handleMap.get(callHandle);
    }
    public void removeCallByHandle(String callHandle)
    {
        handleMap.remove(callHandle);
        return;
    }
    public synchronized String getNewHandle()
    {
        handleCounter++;
        return new Integer(handleCounter).toString();
    }
    

}
