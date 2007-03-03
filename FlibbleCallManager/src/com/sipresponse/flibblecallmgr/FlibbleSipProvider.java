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

import java.util.Properties;

import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

public class FlibbleSipProvider implements SipListener
{
    private SipProvider sipProvider;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private HeaderFactory headerFactory;
    private SipStack sipStack;
    private ListeningPoint udpListeningPoint;
    

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
            udpListeningPoint = sipStack.createListeningPoint("127.0.0.1",5060, "udp");
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


    public void processResponse(ResponseEvent arg0)
    {
        // TODO Auto-generated method stub
    }


    public void processTimeout(TimeoutEvent arg0)
    {
        // TODO Auto-generated method stub
    }

    public void processTransactionTerminated(TransactionTerminatedEvent arg0)
    {
        // TODO Auto-generated method stub
    }
}
