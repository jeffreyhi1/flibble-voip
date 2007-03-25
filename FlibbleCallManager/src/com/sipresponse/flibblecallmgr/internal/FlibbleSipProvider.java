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
    private ConcurrentHashMap<ClientTransaction,Signal> signals = 
        new ConcurrentHashMap<ClientTransaction,Signal>();
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
        properties.setProperty("javax.sip.STACK_NAME", "FlibbleSipProvider" + new Random().nextInt());
        properties.setProperty("gov.nist.javax.sip.MAX_MESSAGE_SIZE", "1048576");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG","shootistAuthdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG","shootistAuthlog.txt");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "16");
        // Drop the client connection after we are done with the transaction.
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS","false");

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
            udpListeningPoint = sipStack.createListeningPoint(callMgr.getLocalIp(),callMgr.getUdpSipPort(), "udp");
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
            if (responseEvent != null && responseEvent.getResponse().getStatusCode() >= 200)
            {
                signals.remove(ct);
            }
        }
        return responseEvent;
    }
    public ClientTransaction sendRequest(Request request)
    {
        System.err.println("Sending request: " + request.toString());
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
        CSeqHeader cseqHeader = (CSeqHeader) lastInvite.getHeader(CSeqHeader.NAME);
        cseq = cseqHeader.getSeqNumber();
        Request ack = null;
        try
        {
            ack = responseEvent.getDialog().createAck(cseq);
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


    public void processRequest(RequestEvent arg0)
    {
        // TODO Auto-generated method stub
    }


    public void processResponse(ResponseEvent responseEvent)
    {
        System.err.println("Received Response: " + responseEvent.getResponse().toString());
        // find the client transaction signal for this response
        Signal signal = signals.get(responseEvent.getClientTransaction());
        Dialog dialog = responseEvent.getClientTransaction().getDialog();
        
        if (dialog != null)
        {
            CallIdHeader callIdHeader = (CallIdHeader) responseEvent.getResponse().getHeader(CallIdHeader.NAME);
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
            System.err.println("Received transactionless response.");
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
    
    public Request createRequest(String callId,
            String requestMethod,
            SipURI fromUri,
            SipURI toUri)
    {
        SipURI requestURI = null;
        try
        {
            requestURI = toUri;
            
            Address fromAddress = addressFactory.createAddress(addressFactory.createSipURI(
                    fromUri.getUser(), fromUri.getHost()));
            FromHeader fromHeader = headerFactory
                    .createFromHeader(fromAddress, Utils.generateTag());
            
            Address toAddress = addressFactory.createAddress(addressFactory.createSipURI(
                    toUri.getUser(), toUri.getHost()));
            ToHeader toHeader = headerFactory.createToHeader(
                    toAddress, null);

            ArrayList<ViaHeader> viaHeaders = new ArrayList<ViaHeader>();
            ViaHeader viaHeader = headerFactory
                    .createViaHeader(
                            callMgr.getLocalIp(),
                            udpListeningPoint.getPort(), udpListeningPoint.getTransport(), null);
            viaHeaders.add(viaHeader);
            
            MaxForwardsHeader maxForwardsHeader = headerFactory
                    .createMaxForwardsHeader(50);
            
            CallIdHeader callIdHeader = headerFactory.createCallIdHeader(callId);
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader((long)1, requestMethod);
            
            Request request = messageFactory.createRequest(
                    requestURI, requestMethod, callIdHeader, cSeqHeader,
                    fromHeader, toHeader, viaHeaders, maxForwardsHeader);
            
            SipURI contactURI = addressFactory.createSipURI(
                    fromUri.getUser(),
                    callMgr.getLocalIp());

            ContactHeader contactHeader = headerFactory
                    .createContactHeader(addressFactory
                            .createAddress(contactURI));
            
            contactURI.setPort(udpListeningPoint.getPort());
            
            request.addHeader(contactHeader);


            return request;
        }
        catch (Exception ex)
        {
            return null;
        }
    }
    
}
