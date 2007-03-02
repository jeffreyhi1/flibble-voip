/*
 * @(#) FlibbleUIProvider
 *
 * Copyright (c) 2007 Michael D. Cohen.  All rights reserved.
 * 
 * @author mike@sipresponse.com
 */
package com.sipresponse.flibblecallmgr;

/**
 * Interface which defines a UserAgent UI entity. 
 * An object implementing this interface can be supplied to
 * the CallManager's initalize function.
 * If the UserAgent is headless (provides no UI),
 * the CallManager can be initialized without a
 * FlibbleUIProvider.
 * 
 * @author Mike Cohen
 */
public interface FlibbleUiProvider
{

}
