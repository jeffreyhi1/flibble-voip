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

import java.util.concurrent.ConcurrentHashMap;

import com.sipresponse.flibblecallmgr.media.FlibbleMediaProvider;

/**
 * Object is central to flibble-voip.
 * Allows for call control and media control.
 * Provides a simple to use interface for controlling voip calls. 
 * 
 * @author Mike Cohen
 *
 */
public class CallManager
{
    private boolean useSoundCard;
    private FlibbleMediaProvider mediaProvider;
    private FlibbleUiProvider uiProvider;
    private ConcurrentHashMap callMap = new ConcurrentHashMap(); 

    /**
     * Constructor.
     *
     */
    public CallManager()
    {
    }

    /**
     * Initializes the CallManager.  The object must not be used
     * before initialization.
     *
     * @param localIp
     * @param udpSipPort
     * @param mediaPortStart
     * @param mediaPortEnd
     * @param enableStun
     * @param uiProvider The UI implementation.  Supply a null for an application 
     *          with no user interface.
     *          for an application with no media control.
     * @param useSoundCard True if the application wishes to utilize audio hardware.
     *          Otherwise, false.
     * 
     */
    public void initialize(String localIp,
                           int udpSipPort,
                           int mediaPortStart,
                           int mediaPortEnd,
                           boolean enableStun,
                           FlibbleUiProvider uiProvider,
                           boolean useSoundCard)
    {
        this.uiProvider = uiProvider;
        this.useSoundCard = useSoundCard;
    }
                           
}
