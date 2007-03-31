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

import gov.nist.javax.sdp.fields.AttributeField;

import java.util.Vector;

import javax.sdp.Connection;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.sdp.SessionName;
import javax.sdp.Time;
import javax.sdp.Version;
import javax.sip.Dialog;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventType;

public class Call
{
    private CallManager callMgr;
    private String callId;
    private String handle;
    private String lineHandle;
    private String sipUriString;
    private Dialog dialog;
    private SessionDescription localSdp;
    private SessionDescription remoteSdp;
    private Line line;
    private Event lastCallEvent;
    private boolean connected = false;
    
    public Event getLastCallEvent()
    {
        return lastCallEvent;
    }
    public void setLastCallEvent(Event lastCallEvent)
    {
        this.lastCallEvent = lastCallEvent;
        if (lastCallEvent.getEventType() == EventType.CALL &&
            lastCallEvent.getEventCode() ==  EventCode.CALL_CONNECTED)
        {
            connected = true;
        }
        else if (lastCallEvent.getEventType() == EventType.CALL &&
                lastCallEvent.getEventCode() ==  EventCode.CALL_DISCONNECTED)
        {
            connected = false;
        }
    }
    public boolean isConnected()
    {
        return connected;
    }
    public SessionDescription getLocalSdp()
    {
        return localSdp;
    }
    public void setLocalSdp(SessionDescription localSdp)
    {
        this.localSdp = localSdp;
    }
    public SessionDescription getRemoteSdp()
    {
        return remoteSdp;
    }
    public void setRemoteSdp(SessionDescription remoteSdp)
    {
        this.remoteSdp = remoteSdp;
    }
    public Call(CallManager callMgr,
            String lineHandle,
            String sipUriString,
            String callId)
    {
        this.callMgr = callMgr; 
        this.lineHandle = lineHandle;
        this.sipUriString = sipUriString;
        this.callId = callId;
        handle = InternalCallManager.getInstance().getNewHandle();
        InternalCallManager.getInstance().addCall(handle, this);
        line = InternalCallManager.getInstance().getLineManager(callMgr).getLine(lineHandle);
    }
    public String getCallId()
    {
        return callId;
    }
    public void setCallId(String callId)
    {
        this.callId = callId;
    }
    public String getHandle()
    {
        return handle;
    }
    public void setHandle(String handle)
    {
        this.handle = handle;
    }
    public String getLineHandle()
    {
        return lineHandle;
    }
    public void setLineHandle(String lineHandle)
    {
        this.lineHandle = lineHandle;
    }
    public String getSipUriString()
    {
        return sipUriString;
    }
    public void setSipUriString(String sipUriString)
    {
        this.sipUriString = sipUriString;
    }
    public Dialog getDialog()
    {
        return dialog;
    }
    public void setDialog(Dialog dialog)
    {
        this.dialog = dialog;
    }
    
    public void createLocalSdp(String[] codecNames, SessionDescription remoteSdp)
    {
        String ipToShare = callMgr.getLocalIp();
        try
        {
            localSdp = SdpFactory.getInstance().createSessionDescription();
            Version version = SdpFactory.getInstance().createVersion(0);
            localSdp.setVersion(version);
            
            long session = (long) ( 1000000 * Math.random());
            Origin origin = SdpFactory.getInstance().createOrigin(
                    line.getUser(),
                    session,
                    session + 1,
                    "IN",
                    "IP4",
                    ipToShare);
            localSdp.setOrigin(origin);
            // Session Name
            SessionName sessionName = SdpFactory.getInstance().createSessionName("Flibble Session");
            localSdp.setSessionName(sessionName);
            // Connection
            Connection connection = SdpFactory.getInstance().createConnection(ipToShare);
            localSdp.setConnection(connection);

            Time time = SdpFactory.getInstance().createTime();
            Vector<Time> timeDescriptions = new Vector<Time>();
            timeDescriptions.add(time);
            localSdp.setTimeDescriptions(timeDescriptions);
            
            
            // Media Description
            MediaDescription mediaDescription = SdpFactory.getInstance()
                    .createMediaDescription("audio",
                            callMgr.getMediaPortStart(),
                            1,
                            "RTP/AVP",
                            new int[] { 0, 101 });            
            
            Vector<AttributeField> attributes = new Vector<AttributeField>();
            AttributeField media = new AttributeField();
            media.setName("rtpmap");
            media.setValue("0" +
                           " " +
                           "PCMU" + 
                           "/" + 
                           "8000");
            attributes.add(media);
            
            media = new AttributeField();
            media.setName("rtpmap");
            media.setValue("101 telephone-event/8000" );
            attributes.add(media);
            
            media = new AttributeField();
            media.setName("fmtp");
            media.setValue("101 0-15");
            attributes.add(media);
            
            mediaDescription.setAttributes(attributes);
            Vector<MediaDescription> mediaDescriptions = new Vector<MediaDescription>();
            mediaDescriptions.add(mediaDescription);
            localSdp.setMediaDescriptions(mediaDescriptions);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }
    public CallManager getCallMgr()
    {
        return callMgr;
    }
}