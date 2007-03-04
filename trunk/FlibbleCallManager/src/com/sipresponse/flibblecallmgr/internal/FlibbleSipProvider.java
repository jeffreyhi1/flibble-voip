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

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.ClientTransaction;
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
import javax.sip.address.AddressFactory;
import javax.sip.header.CSeqHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.util.Signal;

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
        properties.setProperty("javax.sip.STACK_NAME", "FlibbleSipProvider");
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

    public ResponseEvent sendRequest(Request request)
    {
        ResponseEvent responseEvent = null;
        ClientTransaction ct = null;
        boolean gotNewTx = false;
        int tries = 0;
        while (tries < 80 && gotNewTx == false)
        {
            try
            {
                ct = sipProvider.getNewClientTransaction(request);
                gotNewTx = true;
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
        }
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
            try
            {
                responseEvent = signal.waitForResponseEvent(RESPONSE_TIMEOUT);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            signals.remove(ct);
        }

        return responseEvent;
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
        // find the client transaction signal for this response
        Signal signal = signals.get(responseEvent.getClientTransaction());
        if (null != signal)
        {
            signal.setResponseEvent(responseEvent);
            signal.notifyResponseEvent();
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
}
