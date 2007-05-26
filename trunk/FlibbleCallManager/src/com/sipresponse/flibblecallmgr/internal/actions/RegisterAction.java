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


import gov.nist.javax.sip.Utils;
import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ObjectInUseException;
import javax.sip.ResponseEvent;
import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;
import com.sipresponse.flibblecallmgr.internal.util.AuthenticationHelper;
import com.sipresponse.flibblecallmgr.internal.util.MessageDigestAlgorithm;

public class RegisterAction extends ActionThread
{
    protected int timeout = 8000;

    public RegisterAction(CallManager callMgr, Line line)
    {
        super(callMgr, null, line);
    }

    public void run()
    {
        line.setStatus(EventCode.LINE_REGISTERING, EventReason.LINE_NORMAL);

        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
                .getProvider(callMgr);

        Request register = createRegisterRequest();
        if (register != null)
        {

            // create the transaction
            ClientTransaction ct = flibbleProvider.sendRequest(register);

            // wait for a response
            ResponseEvent responseEvent = flibbleProvider
                    .waitForResponseEvent(ct);
            if (null != responseEvent)
            {
                Response response = responseEvent.getResponse();
                int responseCount = 0;
                while (responseCount < 8)
                {
                    if (response.getStatusCode() == 401
                            || response.getStatusCode() == 403)
                    {
                        try
                        {
                            ct.terminate();
                        }
                        catch (ObjectInUseException e)
                        {
                            e.printStackTrace();
                        }
                        Request registerWithAuth = createRegisterRequest();
                        AuthenticationHelper.processResponseAuthorization(callMgr,
                                line,
                                response,
                                registerWithAuth,
                                true);

                        ct = flibbleProvider.sendRequest(registerWithAuth);

                    }
                    else if (response.getStatusCode() == 200)
                    {
                        break;
                    }
                    responseCount++;
                    responseEvent = flibbleProvider.waitForResponseEvent(ct);
                    if (responseEvent != null)
                    {
                        response = responseEvent.getResponse();
                    }
                }
                if (response != null && response.getStatusCode() == 200)
                {
                    line.setLastRegisterTimestamp(System.currentTimeMillis());
                    line.setStatus(EventCode.LINE_REGISTERED, EventReason.LINE_NORMAL);
                }
                else
                {
                    line.setStatus(EventCode.LINE_REGISTER_FAILED, EventReason.LINE_NORMAL);
                }
            }
            else
            {
                line.setStatus(EventCode.LINE_REGISTER_FAILED, EventReason.LINE_NORMAL);
            }
        }
    }

    private Request createRegisterRequest()
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
                .getProvider(callMgr);
        Request register = null;
        register = flibbleProvider.createRequest(Utils
                .generateCallIdentifier(callMgr.getLocalIp()),
                Request.REGISTER, line.getSipUri(), line.getSipUri());
        ExpiresHeader expiresHeader = null;
        try
        {
            expiresHeader = flibbleProvider.headerFactory
                    .createExpiresHeader(line.getRegisterPeriod());
        }
        catch (InvalidArgumentException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        if (null != expiresHeader)
        {
            register.setExpires(expiresHeader);
        }

        return register;
    }
}
