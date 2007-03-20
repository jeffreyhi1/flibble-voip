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

import java.util.Iterator;

import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.LineManager;
import com.sipresponse.flibblecallmgr.internal.actions.ByeAction;
import com.sipresponse.flibblecallmgr.internal.actions.PlaceCallAction;
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;

/**
 * Object is central to flibble-voip. Allows for call control and media control.
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
    private String stunServer;
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
     * Initializes the CallManager. The object must not be used before
     * initialization (with the exception of addListener).
     * 
     * @param localIp - The IP address to be bound to for receiving SIP messages.
     *   This address (or the associated public address, if STUN is enabled)
     *   will appear in the SIP contact and via headers, and in the SDP's origin
     *   and destination headers.
     * @param udpSipPort The udp port for receiving and sending SIP messages.
     * @param mediaPortStart  The start of the range of allowable ports for use with RTP.
     * @param mediaPortEnd The end of the range of allowable ports for use with RTP.
     * @param proxyAddress SIP proxy address or host name.
     * @param proxyPort Port value for the SIP proxy.
     * @param enableStun Enables discovery of public the IP address.
     * @param stunServer The stun server name or address to be used for STUN discovery.
     * @param useSoundCard True if the application wishes to utilize audio hardware.
     *            Otherwise, false.
     */
    public void initialize(String localIp,
            int udpSipPort,
            int mediaPortStart,
            int mediaPortEnd,
            String proxyAddress,
            int proxyPort,
            boolean enableStun,
            String stunServer,
            boolean useSoundCard)
    {
        this.localIp = localIp;
        this.udpSipPort = udpSipPort;
        this.mediaPortStart = mediaPortStart;
        this.mediaPortEnd = mediaPortEnd;
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
        this.enableStun = enableStun;
        this.stunServer = stunServer;
        this.useSoundCard = useSoundCard;
        InternalCallManager.getInstance().setProvider(this,
                new FlibbleSipProvider(this));
        InternalCallManager.getInstance().getProvider(this).initialize();
        InternalCallManager.getInstance().setLineManager(this,
                new LineManager(this));
    }
    
    /**
     * Creates a line entity associated with a SIP URL (display name + uri).
     * The line can be registered with a proxy, or provisioned. 
     * @param sipUriString - The SIP uri associated with this line. eg "sip:foo@example.com"
     * @param displayName - Display name portion of the SIP URL for this line.  Useful for caller ID.
     * @param register - Whether or not to perform SIP registration with the proxy.
     * @param registerPeriod - The requested period (in seconds) of the registration.
     * @param password - A password used for registration authentication.
     * @return Line handle associated with the line entity.
     */
    public String addLine(String sipUriString, String displayName,
            boolean register, int registerPeriod, String password)
    {
        String lineHandle = null;
        try
        {
            lineHandle = InternalCallManager.getInstance().getLineManager(this)
                    .addLine(sipUriString, displayName, register, registerPeriod, password);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return lineHandle;
    }

    /**
     * Creates a call entity on the requested line
     * @param lineHandle The line handle to be used for the call.
     * @param sipUriString The callee's SIP uri.
     * @return Call handle of the newly created call entity.
     */
    public String createCall(String lineHandle, String sipUriString)
    {
        String callId = InternalCallManager.getInstance().getProvider(this).sipProvider
                .getNewCallId().getCallId();
        Call call = new Call(this, lineHandle, sipUriString, callId);
        String callHandle = call.getHandle();
        return callHandle;
    }

    /**
     * Sends an INVITE to the remote party.  The response to the invite will
     * come in the form of an event.  See FlibbleListener.onEvent.
     * @param callHandle The call handle obtained by invoking createCall.
     * @return A result indicating the validity of the parameters.  Actual
     * results of the INVITE will come in the form of an event See FlibbleListener.onEvent. 
     */
    public FlibbleResult placeCall(String callHandle)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;

        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            PlaceCallAction placeCall = new PlaceCallAction(this, call);
            placeCall.start();
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;
    }

    /**
     * Ends a currently connected call by sending a BYE message to the remote party.
     * @param callHandle Handle of the call to end.
     * @return A result indicating the validity of the parameters.  Actual
     * results of the BYE will come in the form of an event See FlibbleListener.onEvent. 
     */
    public FlibbleResult endCall(String callHandle)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;

        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            ByeAction bye = new ByeAction(this, call);
            bye.start();
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;
    }

    /**
     * Registers a object to receive Flibble Events.
     * 
     * @param listener
     *            Listener to add.
     */
    public void addListener(FlibbleListener listener)
    {
        InternalCallManager.getInstance().addListener(this, listener);
    }

    /**
     * Removes an object from the list of objects to receive Flibble Events.
     * 
     * @param listener
     *            Listener to remove.
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

    /**
     * Tears down this call manager.
     *
     */
    public void destroyCallManager()
    {
        SipStack sipStack = InternalCallManager.getInstance().getProvider(this)
                .getSipStack();

        try
        {
            if (sipStack == null)
                return;
            Iterator listeningPoints = sipStack.getListeningPoints();
            if (listeningPoints != null)
            {
                while (listeningPoints.hasNext())
                {
                    ListeningPoint lp = (ListeningPoint) listeningPoints.next();
                    try
                    {
                        sipStack.deleteListeningPoint(lp);
                        lp = null;
                    }
                    catch (ObjectInUseException oiue)
                    {
                        oiue.printStackTrace();
                    }
                    listeningPoints = sipStack.getListeningPoints();
                }
            }
            else
            {

            }

            try
            {
                Thread.currentThread().sleep(200);
            }
            catch (InterruptedException ie)
            {
                ie.printStackTrace();
            }

            Iterator sipProviders = sipStack.getSipProviders();
            if (sipProviders != null)
            {
                while (sipProviders.hasNext())
                {
                    SipProvider sipProvider = (SipProvider) sipProviders.next();
                    sipProvider.removeSipListener(InternalCallManager
                            .getInstance().getProvider(this));
                    try
                    {
                        sipStack.deleteSipProvider(sipProvider);
                        sipProvider = null;
                    }
                    catch (ObjectInUseException oiue)
                    {

                    }
                    sipProviders = sipStack.getSipProviders();
                }
            }
            else
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
