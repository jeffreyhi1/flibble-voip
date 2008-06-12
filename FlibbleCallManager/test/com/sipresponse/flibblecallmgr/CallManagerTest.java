package com.sipresponse.flibblecallmgr;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import junit.framework.JUnit4TestAdapter;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sipresponse.flibblecallmgr.internal.util.Signal;

public class CallManagerTest implements FlibbleListener
{
    private static Properties props;
    private static CallManager callMgr1;
    private static CallManager callMgr2;
    private static String proxyAddress;
    private static String localIp;
    private static int proxyPort;
    private static String uriA;
    private static String uriB;
    private Object eventSync = new Object();
    
    private String lineHandle1;
    private String lineHandle2;
    
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(CallManagerTest.class); 
    }

   
    
    public CallManagerTest()
    {
        if (props == null)
        {
            props = new Properties();
            try
            {
                props.load(new FileInputStream(System.getProperties().getProperty("user.home") + "/flibble-test.properties"));
                proxyAddress = props.getProperty("proxyAddress");
                proxyPort = new Integer(props.getProperty("proxyPort")).intValue();
                uriA = props.getProperty("uriA");
                uriB = props.getProperty("uriB");
                localIp = props.getProperty("localIp");
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        if (callMgr1 == null)
        {
            initializeCallManagers();
        }
    }
    @Before public void setUp()
    {
    }
    
    @After public void tearDown()
    {
//        if (callMgr1 != null && callMgr2 != null)
//        {
//            destroyCallManagers();
//        }
    }
    
    @Test public void addRemoveProvisionedLines()
    {
        addProvisionedLines();
    }
    
    @Test public void register()
    {
    }
    
    private static Signal callSignal;
    private static String waitingForCallHandle;
    private static EventCode waitingForCallCode;
    private static EventReason waitingForCallReason;
    
    @Test public void blindTransfer()
    {
        // initialize call managers if need be
        addProvisionedLines();
        
        String callHandle = callMgr1.createCall(lineHandle1, uriA);
        assertTrue(callHandle != null);
        callMgr1.placeCall(callHandle, MediaSourceType.MEDIA_SOURCE_DUMMY, null, false, 100, 100);
        boolean ret = waitForCallEvent(callHandle, 
                                       EventCode.CALL_CONNECTED,
                                       EventReason.CALL_NORMAL,
                                       15000);
        assertTrue(ret);
        
        callMgr1.blindTransfer(callHandle, uriB);
         ret = waitForCallEvent(callHandle, 
                EventCode.CALL_TRANSFER,
                EventReason.CALL_TRANSFER_AS_CONTROLLER,
                15000);
         assertTrue(ret);
         
         ret = waitForCallEvent(callHandle, 
                 EventCode.CALL_DISCONNECTED,
                 EventReason.CALL_DISCONNECT_LOCAL,
                 15000);
          assertTrue(ret);
         
    }
    
    private void initializeCallManagers()
    {
        if (callMgr1 == null)
        {
            callMgr1 = new CallManager();
            try
            {
            	
                FlibbleResult res = callMgr1.initialize(CallManager.AUTO_DISCOVER,
                        5080,// port to bind to 
                        9100,// start media port range
                        9150,// end media port range
                        proxyAddress,// domain
                        proxyAddress,// proxy address
                        5061,// proxy port
                        null,// stun server
                        "Flibble UA",
                        false,
                        null);        	
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        if (callMgr2 == null)
        {
            callMgr2 = new CallManager();
            try
            {
                FlibbleResult res = callMgr2.initialize(CallManager.AUTO_DISCOVER,
                        5090,// port to bind to 
                        9200,// start media port range
                        9250,// end media port range
                        proxyAddress,// domain
                        proxyAddress,// proxy address
                        5060,// proxy port
                        null,// stun server
                        "Flibble UA",
                        false,
                        null);        	
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            callMgr1.addListener(this);
            callMgr2.addListener(this);
        }
        
    }
    
    private void addProvisionedLines()
    {
        if (lineHandle1 == null)
        {
            lineHandle1 = callMgr1.addLine("sip:foo@" + localIp + ":5080", "Display Name", false, -1, null);
            assertTrue(lineHandle1 != null);
        }

        if (lineHandle2 == null)
        {
            lineHandle2 = callMgr2.addLine("sip:bar@" + localIp + ":5090", "Other Name", false, -1, null);
            assertTrue(lineHandle2 != null);
        }
    }
    
    private void destroyCallManagers()
    {
        
        callMgr1.destroyCallManager();
        lineHandle1 = null;
        callMgr1 = null;
        callMgr2.destroyCallManager();
        lineHandle2 = null;
        callMgr2 = null;
    }
    
    private boolean waitForCallEvent(String callHandle,
            EventCode code,
            EventReason reason,
            long timeout)
    {
        callSignal = new Signal();
        boolean ret = false;
        
        waitingForCallHandle = callHandle;
        waitingForCallCode = code;
        waitingForCallReason = reason;
        ret = callSignal.waitForSignal(timeout);
        return ret;
        
    }
    
    public boolean onEvent(Event event)
    {
        System.out.println(event.toString());
        if (event.getEventType() == EventType.CALL &&
            event.getCallHandle().equals(waitingForCallHandle) &&
            event.getEventCode() == waitingForCallCode &&
            event.getEventReason() == waitingForCallReason)
        {
            callSignal.notifyResponseEvent();
        }
        return false;
    }
    
    
            
}   
