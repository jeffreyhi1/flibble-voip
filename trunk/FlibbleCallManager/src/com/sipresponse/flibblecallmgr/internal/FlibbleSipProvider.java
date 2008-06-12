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
package com.sipresponse.flibblecallmgr.internal;

import gov.nist.javax.sip.Utils;

import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.ClientTransaction;
import javax.sip.Dialog;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.handlers.ByeHandler;
import com.sipresponse.flibblecallmgr.internal.handlers.CancelHandler;
import com.sipresponse.flibblecallmgr.internal.handlers.InviteHandler;
import com.sipresponse.flibblecallmgr.internal.util.Signal;

public class FlibbleSipProvider implements SipListener
{
    public SipProvider sipProvider;

    public AddressFactory addressFactory;

    public MessageFactory messageFactory;

    public HeaderFactory headerFactory;

    private SipStack sipStack;

    private static final int RESPONSE_TIMEOUT = 4000;

    private ListeningPoint udpListeningPoint;

    private CallManager callMgr;

    private ConcurrentHashMap<ClientTransaction, Signal> signals = new ConcurrentHashMap<ClientTransaction, Signal>();

    public FlibbleSipProvider(CallManager callMgr)
    {
        this.callMgr = callMgr;
    }

    public boolean initialize()
    {
        SipFactory sipFactory = null;
        sipStack = null;
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");

        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "FlibbleSipProvider"
                + new Random().nextInt());
        properties.setProperty("javax.sip.OUTBOUND_PROXY", callMgr
                .getProxyAddress()
                + ":" + callMgr.getProxyPort() + "/" + "udp");
        properties
                .setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG",
                "flibbleDebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "flibble.txt");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "0");
        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS",
                "false");
        properties.setProperty("javax.sip.AUTOMATIC_DIALOG_SUPPORT", "ON");

        try
        {
            // Create SipStack object
            sipStack = sipFactory.createSipStack(properties);
            System.out.println("createSipStack " + sipStack);
        }
        catch (PeerUnavailableException e)
        {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
            return false;
        }
        try
        {
            headerFactory = sipFactory.createHeaderFactory();
            addressFactory = sipFactory.createAddressFactory();
            messageFactory = sipFactory.createMessageFactory();
            int count = 0;
            while (udpListeningPoint == null && count < 10)
            {
                try
                {
                    udpListeningPoint = sipStack.createListeningPoint(callMgr.getLocalIp(), callMgr.getUdpSipPort(), "udp");
                }
                catch (InvalidArgumentException e)
                {
                    callMgr.setUdpSipPort(callMgr.getUdpSipPort()+2);
                }
                count++;
            }
            udpListeningPoint.setSentBy(callMgr.getContactIp() + ":" + callMgr.getUdpSipPort());
            sipProvider = sipStack.createSipProvider(udpListeningPoint);
            sipProvider.addSipListener(this);
        }
        catch (PeerUnavailableException e)
        {
            e.printStackTrace();
            System.err.println(e.getMessage());
            System.exit(0);
            return false;
        }
        catch (Exception e)
        {
            System.out.println("Creating Listener Points");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public ResponseEvent waitForResponseEvent(ClientTransaction ct)
    {
        ResponseEvent responseEvent = null;
        Signal signal = signals.get(ct);
        if (null != signal)
        {
            try
            {
                responseEvent = signal.waitForResponseEvent(RESPONSE_TIMEOUT);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            if (responseEvent != null
                    && responseEvent.getResponse().getStatusCode() >= 200)
            {
                signals.remove(ct);
            }
        }
        return responseEvent;
    }

    public ClientTransaction sendDialogRequest(Dialog dialog, Request request)
    {
        ClientTransaction ct = null;
        try
        {
            ct = getSipProvider().getNewClientTransaction(request);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        if (null != ct)
        {
            Signal signal = new Signal();
            signals.put(ct, signal);
        }
        try
        {
            dialog.sendRequest(ct);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return ct;

    }

    public ClientTransaction sendRequest(Request request)
    {
        //System.err.println("Sending request: " + request.toString());
        ClientTransaction ct = null;
        int tries = 0;
        try
        {
            ct = sipProvider.getNewClientTransaction(request);
        }
        catch (TransactionUnavailableException e)
        {
            try
            {
                Thread.sleep(50);
            }
            catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
            e.printStackTrace();
        }
        tries++;
        if (null != ct)
        {
            Signal signal = new Signal();
            signals.put(ct, signal);
            try
            {
                ct.sendRequest();
            }
            catch (SipException e)
            {
                e.printStackTrace();
            }
        }

        return ct;
    }

    public void ackResponse(ResponseEvent responseEvent)
    {
        long cseq = 1;
        ClientTransaction ct = responseEvent.getClientTransaction();
        Request lastInvite = ct.getRequest();
        CSeqHeader cseqHeader = (CSeqHeader) lastInvite
                .getHeader(CSeqHeader.NAME);
        cseq = cseqHeader.getSeqNumber();
        Request ack = null;
        try
        {
            ack = responseEvent.getDialog().createAck(cseq);
            ViaHeader viaHeader = (ViaHeader)ack.getHeader(ViaHeader.NAME);
            viaHeader.setRPort();
        }
        catch (InvalidArgumentException e)
        {
            e.printStackTrace();
        }
        catch (SipException e)
        {
            e.printStackTrace();
        }
        if (null != ack)
        {
            try
            {
                ct.getDialog().sendAck(ack);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

    }

    public void processDialogTerminated(DialogTerminatedEvent arg0)
    {
        // TODO Auto-generated method stub
    }

    public void processIOException(IOExceptionEvent arg0)
    {
        // TODO Auto-generated method stub
    }

    public void processRequest(RequestEvent requestEvent)
    {
//        System.err.println("Received Request: "
//                + requestEvent.getRequest().toString());
        String method = requestEvent.getRequest().getMethod();

        Request request = requestEvent.getRequest();
        if (request.getHeader(MaxForwardsHeader.NAME) == null)
        {
            MaxForwardsHeader maxForwardsHeader = null;
            try
            {
                maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);
            }
            catch (InvalidArgumentException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            request.addHeader(maxForwardsHeader);
        }
        ServerTransaction st = requestEvent.getServerTransaction();
        if (st == null)
        {
            try
            {
                //System.err.println("FlibbleSipProvider.processRequest.  Creating new server transaction");
                st = sipProvider.getNewServerTransaction(requestEvent
                        .getRequest());
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
        CallIdHeader callIdHeader = (CallIdHeader) requestEvent.getRequest()
                .getHeader(CallIdHeader.NAME);
        String callId = callIdHeader.getCallId();
        Call call = InternalCallManager.getInstance().getCallById(callId);
        if (method.equals(Request.INVITE))
        {
            if (null == call)
            {
                InviteHandler inviteHandler = new InviteHandler(callMgr, requestEvent);
                if (null != st)
                {
                    call = InternalCallManager.getInstance().getCallById(callId);
                    call.setServerTransaction(st);
                }
                inviteHandler.execute();

            }
        }
        if (call == null) { return; }

        if (method.equals(Request.BYE))
        {
            new ByeHandler(callMgr, call, requestEvent).execute();
        }
        else if (method.equals(Request.CANCEL))
        {
            new CancelHandler(callMgr, call, requestEvent).execute();
        }
        else if (method.equals(Request.NOTIFY))
        {
        }
        else if (method.equals(Request.OPTIONS))
        {
        }
        else if (method.equals(Request.REFER))
        {
        }
    }

    public void processResponse(ResponseEvent responseEvent)
    {
        //System.err.println("Received Response: "
        //        + responseEvent.getResponse().toString());
        // find the client transaction signal for this response
        if (signals != null &&
            responseEvent != null &&
            responseEvent.getClientTransaction() != null)
        {
            Signal signal = signals.get(responseEvent.getClientTransaction());
            Dialog dialog = responseEvent.getClientTransaction().getDialog();
    
            if (dialog != null)
            {
                CallIdHeader callIdHeader = (CallIdHeader) responseEvent
                        .getResponse().getHeader(CallIdHeader.NAME);
                String callId = callIdHeader.getCallId();
                Call call = InternalCallManager.getInstance().getCallById(callId);
                if (null != call)
                {
                    call.setDialog(dialog);
                }
            }
    
            if (null != signal)
            {
                signal.setResponseEvent(responseEvent);
                signal.notifyResponseEvent();
            }
            else
            {
                System.err.println("Received response not waited on:\n"
                        + responseEvent.getResponse().toString());
            }
        }
    }

    public void processTimeout(TimeoutEvent timeoutEvent)
    {
        // find the client transaction signal for this response
        Signal signal = signals.get(timeoutEvent.getClientTransaction());
        if (null != signal)
        {
            signal.notifyResponseEvent();
        }
    }

    public void processTransactionTerminated(TransactionTerminatedEvent arg0)
    {
        // TODO Auto-generated method stub
    }

    public SipStack getSipStack()
    {
        return sipStack;
    }

    public Request createRequest(String callId, String requestMethod,
            SipURI fromUri, SipURI toUri)
    {
        SipURI requestURI = null;
        try
        {
            requestURI = toUri;

            Address fromAddress = addressFactory.createAddress(addressFactory
                    .createSipURI(fromUri.getUser(), fromUri.getHost()));
            FromHeader fromHeader = headerFactory.createFromHeader(fromAddress,
                    Utils.generateTag());

            Address toAddress = addressFactory.createAddress(addressFactory
                    .createSipURI(toUri.getUser(), toUri.getHost()));
            ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

            ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
            ViaHeader viaHeader = headerFactory.createViaHeader(callMgr
                    .getLocalIp(), udpListeningPoint.getPort(),
                    udpListeningPoint.getTransport(), null);
            viaHeaders.add(viaHeader);

            MaxForwardsHeader maxForwardsHeader = headerFactory
                    .createMaxForwardsHeader(50);

            CallIdHeader callIdHeader = headerFactory
                    .createCallIdHeader(callId);
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader((long) 1,
                    requestMethod);

            Request request = messageFactory.createRequest(requestURI,
                    requestMethod, callIdHeader, cSeqHeader, fromHeader,
                    toHeader, viaHeaders, maxForwardsHeader);

            SipURI contactURI = addressFactory.createSipURI(fromUri.getUser(),
                    callMgr.getContactIp());

            contactURI.setPort(callMgr.getUdpSipPort());

            ContactHeader contactHeader = headerFactory
                    .createContactHeader(addressFactory
                            .createAddress(contactURI));

            contactURI.setPort(udpListeningPoint.getPort());

            request.addHeader(contactHeader);

            if (requestMethod.equalsIgnoreCase(Request.REGISTER))
            {
                request.setRequestURI(addressFactory.createURI("sip:" + toUri.getHost()));
            }

            
            return request;
        }
        catch (Exception ex)
        {
            return null;
        }
    }

    public SipProvider getSipProvider()
    {
        return sipProvider;
    }

    public void setSipProvider(SipProvider sipProvider)
    {
        this.sipProvider = sipProvider;
    }

}
