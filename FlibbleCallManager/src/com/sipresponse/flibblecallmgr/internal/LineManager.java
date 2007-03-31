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

import java.text.ParseException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.sip.address.SipURI;

import com.sipresponse.flibblecallmgr.CallManager;

public class LineManager
{
    private Object syncObj = new Object();
    private static int lineHandle = 0;
    private ConcurrentHashMap<String, Line> lines = 
        new ConcurrentHashMap<String, Line>();
    private Vector<Line> linesVector = new Vector<Line>();
    private CallManager callMgr;
    private RegistrationManager regMgr;
    
    public LineManager(CallManager callMgr)
    {
        regMgr = new RegistrationManager(callMgr);
        regMgr.start();
        this.callMgr = callMgr;
    }
    public String addLine(String sipUriString, String displayName, boolean register, int registerPeriod, String password)
    {
        String sLineHandle = null;
        synchronized (syncObj)
        {
            sLineHandle = new Integer(++lineHandle).toString();
        }
        Line line = new Line(callMgr);
        line.setRegisterPeriod(registerPeriod);
        line.setHandle(sLineHandle);
        line.setPassword(password);
        SipURI sipUri = null;
        try
        {
            sipUri = (SipURI)InternalCallManager.getInstance()
                .getProvider(callMgr).addressFactory.createURI(sipUriString);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        if (null != sipUri)
        {
            line.setSipUri(sipUri);
            line.setRegisterEnabled(register);
            lines.put(sLineHandle, line);
            linesVector.add(line);
        }
        else
        {
            sLineHandle = "-1";
        }
        return sLineHandle;
    }
    
    public Line getLine(String sLineHandle)
    {
        return lines.get(sLineHandle);
    }
    
    public Vector<Line> getLines()
    {
        return linesVector;
    }
    
    public void stopRegistration()
    {
        regMgr.shutdown();
        regMgr.interrupt();
    }

}
