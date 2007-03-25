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

public class CallManagerTest implements FlibbleListener
{
    private CallManager callMgr1;
    private CallManager callMgr2;
    private String proxyAddress;
    private String lineHandle1;
    private String lineHandle2;
    
    public static junit.framework.Test suite()
    {
        return new JUnit4TestAdapter(CallManagerTest.class); 
    }
    
    public CallManagerTest()
    {
    }
    @Before public void setUp()
    {
        Properties props = new Properties();
        try
        {
            props.load(new FileInputStream(System.getProperties().getProperty("user.home") + "/flibble-test.properties"));
            proxyAddress = props.getProperty("proxyAddress");
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
    
    @After public void tearDown()
    {
    }
    
    @Test public void addRemoveProvisionedLines()
    {
        initializeCallManagers();
        addProvisionedLines();
        destroyCallManagers();
    }
    
    @Test public void register()
    {
    }
    
    public boolean onEvent(Event event)
    {
        return false;
    }
    private void initializeCallManagers()
    {
        callMgr1 = new CallManager();
        try
        {
            callMgr1.initialize(InetAddress.getLocalHost().getHostName(),
                    5080,
                    9100,
                    9150,
                    proxyAddress,
                    5060,
                    false,
                    null,
                    false);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        
        callMgr2 = new CallManager();
        try
        {
            callMgr2.initialize(InetAddress.getLocalHost().getHostName(),
                    5090,
                    9200,
                    9250,
                    proxyAddress,
                    5060,
                    false,
                    null,
                    false);
        }
        catch (UnknownHostException e)
        {
            e.printStackTrace();
        }
        callMgr1.addListener(this);
        callMgr2.addListener(this);
        
    }
    
    private void addProvisionedLines()
    {
        lineHandle1 = callMgr1.addLine("sip:foo@127.0.0.1:5080", "Display Name", false, -1, null);
        assertTrue(lineHandle1.equals("1"));

        lineHandle2 = callMgr2.addLine("sip:bar@127.0.0.1:5090", "Other Name", false, -1, null);
        assertTrue(lineHandle2.equals("2"));
    }
    
    private void destroyCallManagers()
    {
        callMgr1.destroyCallManager();
        callMgr2.destroyCallManager();
    }
            
}   
