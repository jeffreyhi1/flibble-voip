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
package com.sipresponse.flibblecallmgr.internal.util;

import javax.sip.header.AuthorizationHeader;
import javax.sip.header.CSeqHeader;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;

public class AuthenticationHelper
{

    public static void processResponseAuthorization(CallManager callMgr,
            Line line,
            Response response,
            Request newRequest, boolean forRegister)
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
                .getProvider(callMgr);

        System.out.println("processResponseAuthorization()");
        // Proxy-Authorization header:
        ProxyAuthenticateHeader authenticateHeader = (ProxyAuthenticateHeader) response
                .getHeader(ProxyAuthenticateHeader.NAME);

        WWWAuthenticateHeader wwwAuthenticateHeader = null;
        CSeqHeader cseqHeader = (CSeqHeader) response
                .getHeader(CSeqHeader.NAME);

        String cnonce = null;
        String uri = null;
        
        if (forRegister == true)
        {
            uri = "sip:" + line.getHost(); 
        }
        else
        {
            uri = line.getSipUri().toString();
        }
        String method = cseqHeader.getMethod();
        String nonce = null;
        String realm = null;
        String qop = null;
        String nonceCount = "00000001";
        String opaque = null;

        try
        {
            if (authenticateHeader == null)
            {
                wwwAuthenticateHeader = (WWWAuthenticateHeader) response
                        .getHeader(WWWAuthenticateHeader.NAME);

                nonce = wwwAuthenticateHeader.getNonce();
                realm = wwwAuthenticateHeader.getRealm();
                if (realm == null)
                {
                    System.out
                            .println("AuthenticationProcess, getProxyAuthorizationHeader(),"
                                    + " ERROR: the realm is not part of the 401 response!");
                    return;
                }
                cnonce = wwwAuthenticateHeader.getParameter("cnonce");
                qop = wwwAuthenticateHeader.getParameter("qop");
                opaque = wwwAuthenticateHeader.getParameter("opaque");
            }
            else
            {
                nonce = authenticateHeader.getNonce();
                realm = authenticateHeader.getRealm();
                if (realm == null)
                {
                    System.out
                            .println("AuthenticationProcess, getProxyAuthorizationHeader(),"
                                    + " ERROR: the realm is not part of the 407 response!");
                    return;
                }
                cnonce = authenticateHeader.getParameter("cnonce");
                qop = authenticateHeader.getParameter("qop");
            }


            String digestResponse = MessageDigestAlgorithm.calculateResponse(
                    "MD5", line.getUser(), realm, line.getPassword(), nonce,
                    nonceCount, cnonce, method, uri, null, qop);

            if (authenticateHeader == null)
            {
                AuthorizationHeader header = flibbleProvider.headerFactory
                        .createAuthorizationHeader("Digest");
                header.setParameter("username", line.getUser());
                header.setParameter("realm", realm);
                if (null != opaque)
                {
                    header.setParameter("opaque", opaque);
                }
                if (qop != null)
                {
                    header.setParameter("qop", qop);
                    if (null != cnonce)
                    {
                        header.setParameter("cnonce", cnonce);
                        header.setParameter("nc", nonceCount);
                    }
                }
                header.setParameter("algorithm", "MD5");
                header.setParameter("uri", uri);
                // header.setParameter("opaque","");
                header.setParameter("nonce", nonce);
                header.setParameter("response", digestResponse);

                newRequest.setHeader(header);
            }
            else
            {
                ProxyAuthorizationHeader header = flibbleProvider.headerFactory
                        .createProxyAuthorizationHeader("Digest");
                header.setParameter("username", line.getUser());
                header.setParameter("realm", realm);
                if (qop != null)
                {
                    header.setParameter("qop", qop);
                    header.setParameter("cnonce", cnonce);
                    header.setParameter("nc", "00000001");
                }
                header.setParameter("algorithm", "MD5");
                header.setParameter("uri", uri);
                header.setParameter("nonce", nonce);
                header.setParameter("response", digestResponse);

                newRequest.setHeader(header);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
