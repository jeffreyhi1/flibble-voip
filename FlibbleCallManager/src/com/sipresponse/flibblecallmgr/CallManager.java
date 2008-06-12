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
package com.sipresponse.flibblecallmgr;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.sip.ListeningPoint;
import javax.sip.ObjectInUseException;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.address.Address;

import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.LineManager;
import com.sipresponse.flibblecallmgr.internal.actions.AcceptCallAction;
import com.sipresponse.flibblecallmgr.internal.actions.AnswerCallAction;
import com.sipresponse.flibblecallmgr.internal.actions.ByeAction;
import com.sipresponse.flibblecallmgr.internal.actions.CancelAction;
import com.sipresponse.flibblecallmgr.internal.actions.ChangeMediaAction;
import com.sipresponse.flibblecallmgr.internal.actions.HoldAction;
import com.sipresponse.flibblecallmgr.internal.actions.JoinCallsAction;
import com.sipresponse.flibblecallmgr.internal.actions.PlaceCallAction;
import com.sipresponse.flibblecallmgr.internal.actions.ReferAction;
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;
import com.sipresponse.flibblecallmgr.internal.media.MediaSocketManager;
import com.sipresponse.flibblecallmgr.internal.net.ProxyDiscoverer;
import com.sipresponse.flibblecallmgr.internal.net.StunDiscovery;
import com.sipresponse.flibblecallmgr.internal.util.HostPort;
import com.sipresponse.flibblecallmgr.internal.util.Signal;

/**
 * Object is central to flibble-voip. Allows for call control and media control.
 * Provides a simple to use interface for controlling SIP based calls.
 * 
 * @author Mike Cohen
 * 
 */
public class CallManager
{
    public static final String AUTO_DISCOVER = "auto";
    private String localIp;
    private int udpSipPort;
    private int mediaPortStart;
    private int mediaPortEnd;
    private String domain;
    private String proxyAddress;
    private int proxyPort;
    private String stunServer;
    private boolean useSoundCard;
    private String publicIp;
    private String userAgent;
    private FlibbleMediaProvider mediaProvider;
    /**
     * Constructor.
     * 
     */
    public CallManager()
    {
    }

    /**
     * Initializes the CallManager by loading the default property file, "flibble.properties",
     * 
     * @return Result of initialization
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public FlibbleResult initialize() throws IOException, IllegalArgumentException
    {
        return initialize(System.getProperty("user.home") + "/" + "flibble.properties");
    }

    /**
     * Initializes the CallManager by loading the default property file specified by the 
     * filename
     * 
     * @param filename The filename of property file to be used for initialization. 
     * @return Result of initialization
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public FlibbleResult initialize(String filename) throws IOException,
            IllegalArgumentException
    {
        Properties props = new Properties();
        try
        {
            props.load(new FileInputStream(filename));
        }
        catch (IOException e)
        {
            throw e;
//          return FlibbleResult.RESULT_IO_FAILURE;
        }
        String localIp = null;
        int udpSipPort = -1;
        int mediaPortStart = 9000;
        int mediaPortEnd = 9020;
        String proxyAddress = null;
        int proxyPort = -1;
        boolean enableStun = false;
        boolean useSoundCard = false;
        String mediaPluginClass = null;
        String domain;

        localIp = props.getProperty("localIp");
        if (null != props.getProperty("udpSipPort"))
        {
            udpSipPort = new Integer(props.getProperty("udpSipPort"))
                    .intValue();
        }
        if (null != props.getProperty("mediaPortStart"))
        {
            mediaPortStart = new Integer(props.getProperty("mediaPortStart"))
                    .intValue();
        }
        if (null != props.getProperty("mediaPortEnd"))
        {
            mediaPortEnd = new Integer(props.getProperty("mediaPortEnd"))
                    .intValue();
        }
        proxyAddress = props.getProperty("proxyAddress");
        domain = props.getProperty("domain");
        if (null != props.getProperty("proxyPort"))
        {
            proxyPort = new Integer(props.getProperty("proxyPort")).intValue();
        }
        if (null != props.getProperty("enableStun"))
        {
            enableStun = Boolean.parseBoolean(props.getProperty("enableStun"));
        }
        stunServer = props.getProperty("stunServer");
        userAgent = props.getProperty("userAgent");
        if (null != props.getProperty("useSoundCard"))
        {
            useSoundCard = Boolean.parseBoolean(props
                    .getProperty("useSoundCard"));
        }
        mediaPluginClass = props.getProperty("mediaPluginClass");

        return initialize(localIp,
                udpSipPort,
                mediaPortStart,
                mediaPortEnd,
                domain,
                proxyAddress,
                proxyPort,
                stunServer,
                userAgent,
                useSoundCard,
                mediaPluginClass);
    }

    /**
     * Initializes the CallManager. The object must not be used before
     * initialization (with the exception of addListener).
     * 
     * @param localIp -
     *            The IP address to be bound to for receiving SIP messages. This
     *            address (or the associated public address, if STUN is enabled)
     *            will appear in the SIP contact and via headers, and in the
     *            SDP's origin and destination headers.
     * @param udpSipPort
     *            The udp port for receiving and sending SIP messages.
     * @param mediaPortStart
     *            The start of the range of allowable ports for use with RTP.
     * @param mediaPortEnd
     *            The end of the range of allowable ports for use with RTP.
     * @param proxyAddress
     *            SIP proxy address or host name.
     * @param proxyPort
     *            Port value for the SIP proxy.
     * @param stunServer
     *            The stun server name or address to be used for STUN discovery.
     * @param useSoundCard
     *            True if the application wishes to use an audio hardware
     *            device. Otherwise, false.
     * @param mediaPluginClass 
     *            Full classpath and name of the the media plugin class.  Can
     *            be set to null to indicate usage of the default media plugin.
     */
    public FlibbleResult initialize(String localIp,
            int udpSipPort,
            int mediaPortStart,
            int mediaPortEnd,
            String domain,
            String proxyAddress,
            int proxyPort,
            String stunServer,
            String userAgent,
            boolean useSoundCard,
            String mediaPluginClass) throws IllegalArgumentException
    {
        this.localIp = localIp;
        this.udpSipPort = udpSipPort;
        this.mediaPortStart = mediaPortStart;
        this.mediaPortEnd = mediaPortEnd;
        this.domain = domain;
        this.proxyAddress = proxyAddress;
        this.proxyPort = proxyPort;
        this.stunServer = stunServer;
        this.useSoundCard = useSoundCard;
        this.userAgent = userAgent;
        
        if (localIp.equals(AUTO_DISCOVER))
        {
            ProxyDiscoverer proxyDiscoverer = new ProxyDiscoverer();
            boolean bStun = false;
            if (getStunServer() != null && getStunServer().length() > 0)
            {
                StunDiscovery.getInstance().setStunServer(getStunServer());
                StunDiscovery.getInstance().setStunServerPort(3478);
                bStun = true;   
            }
            HostPort localHostPort = proxyDiscoverer.selectBestIpAddress(proxyAddress,
                    domain,
                    proxyPort,
                    udpSipPort,
                    bStun);
            if (null == localHostPort)
            {
                try
                {
                    this.localIp = InetAddress.getLocalHost().getHostAddress();
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                    return FlibbleResult.RESULT_NETWORK_FAILURE;
                }                
            }
            else
            {
                this.localIp = localHostPort.getHost();
                this.udpSipPort = localHostPort.getPort();
            }
        }
        if (stunServer != null && stunServer.length() > 0)
        {
            HostPort publicHostPort = null;
            try
            {
                StunDiscovery.getInstance().setStunServer(stunServer);
                StunDiscovery.getInstance().setStunServerPort(3478);
                publicHostPort = StunDiscovery.getInstance().discoverPublicIp(this.localIp, 0, null);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (null != publicHostPort)
            {
                setPublicIp(publicHostPort.getHost());
                System.err.println("Found external ip: " + publicHostPort.getHost());
            }
        }

        // if the application needs to use a sound card, and no
        // plugin class is given, use JMF
        if (true == useSoundCard && null == mediaPluginClass)
        {
            mediaPluginClass = "com.sipresponse.flibblecallmgr.plugin.jmf.JmfPlugin";
        }

        if (mediaPortStart % 2 != 0 || mediaPortEnd % 2 != 0) { throw new IllegalArgumentException(
                "Media end and start ports must be even numbers."); }
        if (mediaPortStart > mediaPortEnd) { throw new IllegalArgumentException(
                "Media end port must be greater than start port."); }
        InternalCallManager.getInstance().setProvider(this,
                new FlibbleSipProvider(this));
        if (false == InternalCallManager.getInstance().getProvider(this).initialize())
        {
            return FlibbleResult.RESULT_UNKNOWN_FAILURE;
        }
        InternalCallManager.getInstance().setLineManager(this,
                new LineManager(this));
        InternalCallManager.getInstance().setMediaSocketManager(this,
                new MediaSocketManager(this, mediaPortStart, mediaPortEnd));
        InternalCallManager.getInstance().setMediaPluginClass(mediaPluginClass);
        return FlibbleResult.RESULT_SUCCESS;
    }

    /**
     * Creates a line entity associated with a SIP URL (display name + uri). The
     * line can be registered with a proxy, or provisioned.
     * 
     * @param sipUriString -
     *            The SIP uri associated with this line. eg
     *            "sip:foo@example.com"
     * @param displayName -
     *            Display name portion of the SIP URL for this line. Useful for
     *            caller ID.
     * @param register -
     *            Whether or not to perform SIP registration with the proxy.
     * @param registerPeriod -
     *            The requested period (in seconds) of the registration.
     * @param password -
     *            A password used for registration authentication.
     * @return Line handle associated with the line entity.
     */
    public String addLine(String sipUriString, String displayName,
            boolean register, int registerPeriod, String password)
    {
        String lineHandle = null;
        try
        {
            lineHandle = InternalCallManager.getInstance().getLineManager(this)
                    .addLine(sipUriString, displayName, register,
                            registerPeriod, password);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return lineHandle;
    }

    /**
     * Creates a call entity on the requested line
     * 
     * @param lineHandle
     *            The line handle to be used for the call.
     * @param sipUriString
     *            The callee's SIP uri.
     * @return Call handle of the newly created call entity.
     */
    public String createCall(String lineHandle, String sipUriString)
    {
        String callId = InternalCallManager.getInstance().getProvider(this).sipProvider
                .getNewCallId().getCallId();
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance().getProvider(this);
        Address sipAddress = null;
        try
        {
            sipAddress = flibbleProvider.addressFactory.createAddress(sipUriString);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        Call call = new Call(this, lineHandle, sipUriString, callId, true, sipAddress);
        String callHandle = call.getHandle();
        return callHandle;
    }

    /**
     * Sends an INVITE to the remote party. The response to the invite will come
     * in the form of an event. See FlibbleListener.onEvent.
     * 
     * @param callHandle
     *            The call handle obtained by invoking createCall.
     * @param mediaSourceType
     *        Type of media source to send to the remote endpoint of the call.
     * @param filename
     *        For file media types, the filename of the .wav file to be
     *        used as a media source.
     * @param loop
     *        For file media types, and indication of the desired looping behavior
     * @param initialVolume
     *        Initial volume for audio playout.  Valid values are between 0 and 100, inclusive
     * @param initialGain
     *        Initial microphone gain.  Valid values are between 0 and 100, inclusive
     * @return A result indicating the validity of the parameters. Actual
     *         results of the INVITE will come in the form of an event See
     *         FlibbleListener.onEvent.
     */
    public FlibbleResult placeCall(String callHandle,
            MediaSourceType mediaSourceType,
            String filename,
            boolean loop,
            int initialVolume,
            int initialGain)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;

        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            PlaceCallAction placeCall = new PlaceCallAction(this,
                    call,
                    mediaSourceType,
                    filename,
                    loop,
                    initialVolume,
                    initialGain);
            placeCall.start();
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;
    }

    /**
     * Joins currently connected calls into a conference
     * 
     * (CURRENTLY UNIMPLEMENTED)
     *  
     * @param calls array of calls to be joined into a conference.
     * 
     * @return The result of the join.
     */
    public FlibbleResult joinCalls(CallData[] calls)
    {
        Call[] callArray = new Call[calls.length];
        FlibbleResult result = FlibbleResult.RESULT_SUCCESS;
        int i = 0;
        for (CallData cd : calls)
        {
            callArray[i] = cd.getCall();
            i++;
        }
        JoinCallsAction joinCalls = new JoinCallsAction(this,
                callArray);
        joinCalls.start();
        return result;
    }
    
    /**
     * Joins currently connected calls into a conference
     *
     * (CURRENTLY UNIMPLEMENTED)
     * 
     * @param calls array of calls to be joined into a conference.
     * 
     * @return The result of the join.
     */
    public FlibbleResult joinCalls(Vector<CallData> calls)
    {
        Call[] callArray = new Call[calls.size()];
        FlibbleResult result = FlibbleResult.RESULT_SUCCESS;
        int i = 0;
        for (CallData cd : calls)
        {
            callArray[i] = cd.getCall();
            i++;
        }
        JoinCallsAction joinCalls = new JoinCallsAction(this,
                callArray);
        joinCalls.start();
        return result;
    }
    
    /**
     * Ends a currently connected call by sending a BYE message to the remote
     * party.
     * 
     * @param callHandle
     *            Handle of the call to end.
     * @return A result indicating the validity of the parameters. Actual
     *         results of the BYE will come in the form of an event See
     *         FlibbleListener.onEvent.
     */
    public FlibbleResult endCall(String callHandle)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;

        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            if (call.isConnected())
            {
                ByeAction bye = new ByeAction(this, call);
                bye.start();
            }
            else if (call.isFromThisSide())
            {
                CancelAction cancel = new CancelAction(this, call);
                cancel.start();
            }
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;
    }

    /**
     * Accepts an invite from a remote party, sending a non final response
     * 
     * @param callHandle - Handle of the call to be accepted
     * @param statusCode - The non final response code to send back to the remote party.
     *                     For example, a 180 Ringing response.
     */
    public FlibbleResult acceptCall(String callHandle,
                             int statusCode)
    {
        FlibbleResult result = FlibbleResult.RESULT_SUCCESS;
        if (100 > statusCode)
        {
            statusCode = 180;
        }
        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            AcceptCallAction acceptAction = new AcceptCallAction(this, call);
            acceptAction.setAcceptStatusCode(statusCode);
            acceptAction.start();
        }
        return result;
    }
    
    /**
     * Answers an invite from a remote party, sending a final 200 OK response
     * 
     * @param callHandle Handle of the call to be answered.
     * @param mediaSourceType
     *        Type of media source to send to the remote endpoint of the call.
     * @param filename
     *        For file media types, the filename of the .wav file to be
     *        used as a media source.
     * @param loop
     *        For file media types, and indication of the desired looping behavior
     * @param initialVolume
     *        Initial volume for audio playout.  Valid values are between 0 and 100, inclusive
     * @param initialGain
     *        Initial microphone gain.  Valid values are between 0 and 100, inclusive
     */
    public FlibbleResult answerCall(String callHandle,
            MediaSourceType mediaSourceType,
            String mediaFilename,
            boolean loop,
            int initialVolume,
            int initialGain)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;
        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            AnswerCallAction answerAction = new AnswerCallAction(this,
                                                                 call,
                                                                 mediaSourceType,
                                                                 mediaFilename, 
                                                                 loop,
                                                                 initialVolume,
                                                                 initialGain);
            answerAction.start();
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;
    }
    
    /**
     * Changes the media source for a call in progress.
     * 
     * @param callHandle Handle of the call.
     * @param mediaSourceType
     *        Type of media source to send to the remote endpoint of the call.
     * @param filename
     *        For file media types, the filename of the .wav file to be
     *        used as a media source.
     * @param loop
     *        For file media types, and indication of the desired looping behavior
     * @param initialVolume
     *        Initial volume for audio playout.  Valid values are between 0 and 100, inclusive
     * @param initialGain
     *        Initial microphone gain.  Valid values are between 0 and 100, inclusive
     */
    public FlibbleResult changeMediaSource(String callHandle,
            MediaSourceType mediaSourceType,
            String mediaFilename,
            boolean loop)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;
        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            ChangeMediaAction changeMediaAction = new ChangeMediaAction(this,
                                                                 call,
                                                                 mediaSourceType,
                                                                 mediaFilename, 
                                                                 loop);
            changeMediaAction.start();
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;
    }

    /**
     * Enables local echo suppression.  The effect of echo suppression will 
     * be that, during local audio playout of remote audio, the microphone capture
     * will be suppressed, or partially suppressed.
     * 
     * @param enable Enables or disables echo suppression.
     * @param percentSuppression The percent of echo suppression to use. 
     * (100% will mute the microphone during above-threshold local audio playout)
     * @return
     */
    public FlibbleResult enableEchoSuppression(boolean enable, float percentSuppression)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;
        if (null == mediaProvider)
        {
            String mediaPluginClassName = InternalCallManager.getInstance()
                .getMediaPluginClass();
            try
            {
                mediaProvider = (FlibbleMediaProvider) Class.forName(
                        mediaPluginClassName).newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return FlibbleResult.RESULT_UNKNOWN_FAILURE;
            }
        }
        mediaProvider.enableEchoSuppression(enable, percentSuppression);
        result = FlibbleResult.RESULT_SUCCESS;
        return result;
    }
    
    /**
     * Sets the microphone gain level.
     * @param gain Gain level (valid values are 0 - 100)
     * @return
     */
    public FlibbleResult setGain(int gain)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;
        if (null == mediaProvider)
        {
            String mediaPluginClassName = InternalCallManager.getInstance()
                .getMediaPluginClass();
            try
            {
                mediaProvider = (FlibbleMediaProvider) Class.forName(
                        mediaPluginClassName).newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return FlibbleResult.RESULT_UNKNOWN_FAILURE;
            }
        }
        mediaProvider.setMicrophoneGain(gain);
        result = FlibbleResult.RESULT_SUCCESS;
        return result;
    }
    /**
     * Sets the call playout volume for a specific call.
     * @param callHandle
     * @param vol
     * @return
     */
    public FlibbleResult setCallVolume(String callHandle, int vol)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;
        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            call.setVolume(vol);
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;
    }
    
    /**
     * Sets the audio playout volume for non-call related scenarios, 
     * such as local media file playout.
     * @param vol
     */
    public void setVolume(int vol)
    {
        if (null == mediaProvider)
        {
            String mediaPluginClassName = InternalCallManager.getInstance()
                .getMediaPluginClass();
            try
            {
                mediaProvider = (FlibbleMediaProvider) Class.forName(
                        mediaPluginClassName).newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        mediaProvider.setVolume(vol);
    }
    
    /**
     * Plays a media file to the system's audio playout device.
     * @param url - Url of the media file.
     * @param loop - Looping preference.
     * @param volume Volume (valid values are 0 - 100)
     * @return
     */
    public FlibbleResult playFileLocally(URL url, boolean loop, int volume)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;

        if (null == mediaProvider)
        {
            String mediaPluginClassName = InternalCallManager.getInstance()
                .getMediaPluginClass();
            try
            {
                mediaProvider = (FlibbleMediaProvider) Class.forName(
                        mediaPluginClassName).newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return FlibbleResult.RESULT_UNKNOWN_FAILURE;
            }
        }
        mediaProvider.playFileLocally(url, loop, volume);
        result = FlibbleResult.RESULT_SUCCESS;
        return result;
    }
    
    /**
     * Stops local playout of a media file.
     * @param url Url of the media file to be stopped.
     * @return
     */
    public FlibbleResult stopFileLocally(URL url)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;

        if (null != mediaProvider)
        {
            mediaProvider.stopFileLocally(url);
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;
    }   
    
    /**
     * Stops local playout of all media files.
     * @return
     */
    public FlibbleResult stopLocalPlayoutAll()
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;

        if (null != mediaProvider)
        {
            mediaProvider.stopLocalPlayoutAll();
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;        
    }
    

    /**
     * Puts a call on hold, or takes a call off hold, in a 
     * synchronous manner.
     * @param callHandle The call to put on or off hold.
     * @param hold On-hold or off-hold preference
     * @param volume For off-hold operations, the local playout volume
     *  at which to resume the call.
     * @param timeout Maximum time to wait for a hold operation to complete
     * @return
     */
    public FlibbleResult holdCallSynchronous(String callHandle,
            boolean hold,
            int volume,
            int timeout)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;
        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            HoldAction holdAction = new HoldAction(this,
                                                   call,
                                                   hold,
                                                   volume);
            Signal signal = null;
            if (timeout > -1)
            {
                signal = new Signal();
            }
            holdAction.setNotifier(signal);
            holdAction.start();
            if (null != signal)
            {
                try
                {
                    signal.waitForResponseEvent(timeout);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            result = FlibbleResult.RESULT_SUCCESS;
        }        
        return result;
        
    }
    
    /**
     * Puts a call on hold, or takes a call off hold, in a 
     * asynchronous manner.
     * @param callHandle The call to put on or off hold.
     * @param hold On-hold or off-hold preference
     * @param volume For off-hold operations, the local playout volume
     *  at which to resume the call.
     * @return
     */
    public FlibbleResult holdCall(String callHandle,
            boolean hold,
            int volume
            )
    {
        return holdCallSynchronous(callHandle, hold, volume, -1);
    }
    /**
     * Transfers a currently connected call by sending a REFER message to the
     * remote party.
     * 
     * @param callHandle
     *            Handle of the call to transfer.
     * @param targetUri
     *            URI of the transfer target.
     * @return A result indicating the validity of the parameters. Actual
     *         results of the transfer will come in the form of an event See
     *         FlibbleListener.onEvent.
     */
    public FlibbleResult blindTransfer(String callHandle, String targetUri)
    {
        FlibbleResult result = FlibbleResult.RESULT_UNKNOWN_FAILURE;
        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);

        if (null != call && call.isConnected() == false)
        {
            result = FlibbleResult.RESULT_INVALID_STATE;
            return result;
        }
        if (null != call)
        {
            ReferAction refer = new ReferAction(this, call, targetUri,
                    ReferAction.ReferActionType.BLIND);
            refer.start();
            result = FlibbleResult.RESULT_SUCCESS;
        }
        return result;
    }

    /**
     * Sends a dtmf event to the remote party of the call.
     * 
     * @param callHandle - Handle of the call for the sending of dtmf.
     * @param dtmfCode - 0-9 for digits 0 to 9, * is 10, # is 11.
     */
    public void sendDtmf(String callHandle,
            int dtmfCode)
    {
        Call call = InternalCallManager.getInstance().getCallByHandle(
                callHandle);
        if (null != call)
        {
            FlibbleMediaProvider mediaProvider = call.getMediaProvider();
            
            if (null != mediaProvider)
            {
                mediaProvider.sendDtmf(dtmfCode);
            }
        }
    }
    
    /**
     * Sets an object to receive Flibble Events.
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

    /**
     * Removes aLL objectS from the list of objects to receive Flibble Events.
     */
    public void removeAllListeners()
    {
        InternalCallManager.getInstance().removeAllListeners(this);
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

    public String getDomain()
    {
        return domain;
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

    public void setUdpSipPort(int udpSipPort)
    {
        this.udpSipPort = udpSipPort;   
    }
    
    public boolean getUseSoundCard()
    {
        return useSoundCard;
    }
    
    public CallData getCallData(String callHandle)
    {
        CallData callData = InternalCallManager.getInstance().getCallData(callHandle);
        return callData;
    }
    public String getPublicIp()
    {
        return publicIp;
    }
    
    public void setPublicIp(String publicIp)
    {
        this.publicIp = publicIp;
    }
    
    public String getContactIp()
    {
        if (publicIp != null)
            return publicIp;
        return localIp;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public String getStunServer()
    {
        return stunServer;
    }

    /**
     * Tears down this call manager.
     * 
     */
    public void destroyCallManager()
    {
        removeAllListeners();
        LineManager lineManager = InternalCallManager.getInstance().getLineManager(this);
        if (null != lineManager)
        {
            lineManager.stopRegistration();
        }
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance().getProvider(this);
        SipStack sipStack = null;
        if (flibbleProvider != null)
        {
            sipStack = flibbleProvider.getSipStack();
        }

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
                Thread.sleep(200);
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
        System.err.println("CallManager destroyed.");
    }

}
