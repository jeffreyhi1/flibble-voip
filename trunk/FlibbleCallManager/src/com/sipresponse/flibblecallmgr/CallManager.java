/*
 * @(#) CallManager
 *
 * Copyright (c) 2007 Michael D. Cohen.  All rights reserved.
 * 
 * @author mike@sipresponse.com
 */
package com.sipresponse.flibblecallmgr;

import com.sipresponse.flibblecallmgr.media.FlibbleMediaProvider;

/**
 * Singleton object is central to flibble-voip.
 * Allows for call control and media control.
 * Provides a simple to use 
 * 
 * @author Mike Cohen
 *
 */
public class CallManager
{
    private FlibbleMediaProvider mediaProvider;
    private FlibbleUiProvider uiProvider;
    private static CallManager instance;
    
    /**
     * Obtains the one and only instance of the CallManager.
     * @return The CallManager instance
     */
    public static synchronized CallManager getInstance()
    {
        if (null == instance)
        {
            instance = new CallManager();
        }
        return instance;
    }
    private CallManager()
    {
    }
    /**
     * 
     * @param uiProvider The UI implementation.  Supply a null for an application 
     * with no user interface.
     * @param mediaProvider The media implementation.  Supply a null
     * for an application with no media control.
     */
    public void initialize(FlibbleUiProvider uiProvider,
                           FlibbleMediaProvider mediaProvider)
    {
        this.uiProvider = uiProvider;
        this.mediaProvider = mediaProvider;
    }
                           
}
