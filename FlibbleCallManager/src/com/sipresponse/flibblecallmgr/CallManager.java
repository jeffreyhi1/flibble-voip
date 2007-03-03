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

import java.text.ParseException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.SipProvider;
import javax.sip.address.SipURI;

import com.sipresponse.flibblecallmgr.media.FlibbleMediaProvider;
import com.sipresponse.flibblecallmgr.actions.PlaceCallAction;

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
    private Vector<FlibbleListener> flibbleListeners = new Vector<FlibbleListener>();
    private Object vectorSync = new Object();
    private ConcurrentHashMap callMap = new ConcurrentHashMap(); 
    private FlibbleSipProvider provider = new FlibbleSipProvider(this);
    private LineManager lineManager = new LineManager(this);

    /**
     * Constructor.
     *
     */
    public CallManager()
    {
    }

    /**
     * Initializes the CallManager.  The object must not be used
     * before initialization (with the exception of addListener).
     *
     * @param localIp
     * @param udpSipPort
     * @param mediaPortStart
     * @param mediaPortEnd
     * @param proxyAddress SIP proxy address or host name.
     * @param proxyPort Port value for the SIP proxy.
     * @param enableStun
     * @param useSoundCard True if the application wishes to utilize audio hardware.
     *          Otherwise, false.
     * 
     */
    public void initialize(String localIp,
                           int udpSipPort,
                           int mediaPortStart,
                           int mediaPortEnd,
                           String proxyAddress,
                           int proxyPort,
                           boolean enableStun,
                           boolean useSoundCard)
    {
        this.useSoundCard = useSoundCard;
        provider.initialize();
    }

    public String addLine(String sipUrlString,
                                 String displayName,
                                 boolean register)
    {
        String lineHandle = null;
        SipURI sipUrl = null;
        try
        {
            sipUrl = (SipURI)provider.addressFactory.createURI(sipUrlString);
            lineHandle = lineManager.addLine(sipUrlString, displayName, register);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        return lineHandle;
    }
    
    public FlibbleResult placeCall(String lineHandle,
            String sipUri)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;
        
        PlaceCallAction placeCall = new PlaceCallAction(this);
        placeCall.setLineHandle(lineHandle);
        placeCall.setSipUri(sipUri);
        
        placeCall.start();
        
        return result;
    }
    
    /**
     * Registers a object to receive Flibble Events.
     * @param listener Listener to add.
     */
    public void addListener(FlibbleListener listener)
    {
        synchronized (vectorSync)
        {
            flibbleListeners.add(listener);
        }
    }
    /**
     * Removes an object from the list of objects to receive Flibble Events.
     * @param listener Listener to remove.
     */
    public void removeListener(FlibbleListener listener)
    {
        synchronized (vectorSync)
        {
            flibbleListeners.remove(listener);
        }
    }

    public FlibbleSipProvider getProvider()
    {
        return provider;
    }

    public LineManager getLineManager()
    {
        return lineManager;
    }

    public void setLineManager(LineManager lineManager)
    {
        this.lineManager = lineManager;
    }    
}
