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
package com.sipresponse.flibblecallmgr.internal.actions;


import java.text.ParseException;

import javax.sdp.SessionDescription;
import javax.sip.ServerTransaction;
import javax.sip.address.Address;
import javax.sip.address.SipURI;
import javax.sip.header.ContactHeader;
import javax.sip.header.ContentLengthHeader;
import javax.sip.header.ContentTypeHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.MediaSourceType;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;

public class AnswerCallAction extends ActionThread
{
    private int timeout = 60000;

    private MediaSourceType mediaSourceType;

    private String mediaFilename;
    private String destIp;
    private int destPort;
    private int receivePort;
    private FlibbleSipProvider flibbleProvider;
    private FlibbleMediaProvider mediaProvider;
    
    public AnswerCallAction(CallManager callMgr, Call call,
            MediaSourceType mediaSourceType, String mediaFilename)
    {
        super(callMgr, call, null);
        this.mediaSourceType = mediaSourceType;
        this.mediaFilename = mediaFilename;
        flibbleProvider = InternalCallManager.getInstance()
                .getProvider(callMgr);
    }

    public int getTimeout()
    {
        return timeout;
    }

    public void setTimeout(int timeout)
    {
        this.timeout = timeout;
    }

    public void run()
    {
        try
        {
            receivePort = InternalCallManager.getInstance().getMediaSocketManager(callMgr).getNextAvailablePort();
            call.setLocalSdpPort(receivePort);
            
            Response response = createResponse();
            setContent(response);
            send(response);
            startMediaSend();
            startMediaReceive();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void send(Response response)
    {
        if (null != response)
        {
            ServerTransaction st = call.getLastRequestEvent().getServerTransaction();
            if (null == st)
            {
                st = call.getServerTransaction();
            }
            try
            {
                st.sendResponse(response);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    private void setContent(Response response)
    {
        // Create ContentTypeHeader
        ContentTypeHeader contentTypeHeader = null;
        ContentLengthHeader contentLengthHeader = null;
        SessionDescription localSdp = null;

        if (mediaSourceType != MediaSourceType.MEDIA_SOURCE_NONE)
        {
            try
            {
                call.createLocalSdp(null, null, receivePort);
                localSdp = call.getLocalSdp();
                contentTypeHeader = flibbleProvider.headerFactory
                        .createContentTypeHeader("application", "sdp");
                contentLengthHeader = flibbleProvider.headerFactory
                        .createContentLengthHeader(localSdp.toString().length());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            response.setHeader(contentLengthHeader);
            try
            {
                response.setContent(localSdp.toString(), contentTypeHeader);
            }
            catch (ParseException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Response createResponse()
    {
        Response response = null;

        try
        {
            Request request = call.getLastRequestEvent().getRequest();
            // Create the response.
            FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance().getProvider(callMgr);
            try
            {
                response = flibbleProvider.messageFactory.createResponse(200, request);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            // create Contact Header
            SipURI contactUri = flibbleProvider.addressFactory.createSipURI(
                    line.getUser(), callMgr.getContactIp());
            Address contactAddress = flibbleProvider.addressFactory
                    .createAddress(contactUri);
            ((SipURI) contactAddress.getURI()).setPort(callMgr.getUdpSipPort());
            ContactHeader contactHeader = flibbleProvider.headerFactory
                    .createContactHeader(contactAddress);
            response.setHeader(contactHeader);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return response;
    }

    private void startMediaReceive()
    {
        mediaProvider = call.getMediaProvider();
        if (null != mediaProvider)
        {
            call.setLocalSdpAddress(callMgr.getContactIp());
            call.setLocalSdpPort(receivePort);
            mediaProvider.initializeRtpReceive(callMgr,
                    this.call.getHandle(),
                    callMgr.getLocalIp(),
                    receivePort);
            mediaProvider.startRtpReceive(callMgr.getLocalIp(), receivePort);
        }
    }
    
    private void startMediaSend()
    {
        destPort = call.getRemoteSdpPort();
        destIp = call.getRemoteSdpAddress();
        
        mediaProvider = call.getMediaProvider();
        if (null != mediaProvider)
        {
            mediaProvider.initializeRtpSend(callMgr,
                    this.call.getHandle(),
                    destIp, destPort, call.getLocalSdpPort(),
                    mediaSourceType,
                    mediaFilename);
            mediaProvider.startRtpReceive(callMgr.getLocalIp(), receivePort);
        }
    }
    
}
