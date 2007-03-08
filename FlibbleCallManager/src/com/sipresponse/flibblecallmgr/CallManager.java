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

import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleListener;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.LineManager;
import com.sipresponse.flibblecallmgr.internal.actions.PlaceCallAction;
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;

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
    private String localIp;
    private int udpSipPort;
    private int mediaPortStart;
    private int mediaPortEnd;
    private String proxyAddress;
    private int proxyPort;
    boolean enableStun;    
    private boolean useSoundCard;
    private FlibbleMediaProvider mediaProvider;

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
        this.localIp = localIp;
        this.udpSipPort = udpSipPort;
        this.mediaPortStart = mediaPortStart;
        this.mediaPortEnd = mediaPortEnd;
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
        this.enableStun = enableStun;
        this.useSoundCard = useSoundCard;
        InternalCallManager.getInstance().setProvider(this, new FlibbleSipProvider(this));
        InternalCallManager.getInstance().getProvider(this).initialize();
        InternalCallManager.getInstance().setLineManager(this, new LineManager(this));
    }

    public String addLine(String sipUriString,
                                 String displayName,
                                 boolean register)
    {
        String lineHandle = null;
        try
        {
            lineHandle = InternalCallManager.getInstance().getLineManager(this)
                .addLine(sipUriString, displayName, register);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return lineHandle;
    }
    
    public String createCall(String lineHandle, String sipUriString)
    {
        String callId = InternalCallManager.getInstance().getProvider(this)
            .sipProvider.getNewCallId().getCallId();
        Call call = new Call(lineHandle, sipUriString, callId);
        String callHandle = call.getHandle(); 
        return callHandle;
    }
    
    public FlibbleResult placeCall(String callHandle)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;
        
        Call call = Call.getCallByHandle(callHandle);
        PlaceCallAction placeCall = new PlaceCallAction(this, call);
        placeCall.start();
        
        return result;
    }
    
    /**
     * Registers a object to receive Flibble Events.
     * @param listener Listener to add.
     */
    public void addListener(FlibbleListener listener)
    {
        InternalCallManager.getInstance().addListener(this, listener);
    }
    
    /**
     * Removes an object from the list of objects to receive Flibble Events.
     * @param listener Listener to remove.
     */
    public void removeListener(FlibbleListener listener)
    {
        InternalCallManager.getInstance().removeListener(this, listener);
    }


    public boolean isEnableStun()
    {
        return enableStun;
    }

    public String getLocalIp()
    {
        return localIp;
    }

    public int getMediaPortEnd()
    {
        return mediaPortEnd;
    }

    public int getMediaPortStart()
    {
        return mediaPortStart;
    }

    public String getProxyAddress()
    {
        return proxyAddress;
    }

    public int getProxyPort()
    {
        return proxyPort;
    }

    public int getUdpSipPort()
    {
        return udpSipPort;
    }

    public boolean getUseSoundCard()
    {
        return useSoundCard;
    }   
    
}
