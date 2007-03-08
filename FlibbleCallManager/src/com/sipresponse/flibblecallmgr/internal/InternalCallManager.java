package com.sipresponse.flibblecallmgr.internal;

import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;

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

}
