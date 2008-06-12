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
package com.sipresponse.flibblecallmgr.internal.util;

import gov.nist.javax.sip.header.SIPHeader;


import javax.sip.header.CSeqHeader;
import javax.sip.header.Header;
import javax.sip.header.ProxyAuthenticateHeader;
import javax.sip.header.WWWAuthenticateHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScheme;
import org.apache.commons.httpclient.auth.AuthenticationException;
import org.apache.commons.httpclient.auth.DigestScheme;
import org.apache.commons.httpclient.auth.MalformedChallengeException;

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
        String headerName = "Authorization";
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
                .getProvider(callMgr);
                WWWAuthenticateHeader wwwAuthenticateHeader = (WWWAuthenticateHeader) response
                        .getHeader(WWWAuthenticateHeader.NAME);
        SIPHeader sipHeader = (SIPHeader) wwwAuthenticateHeader;
        
        if (sipHeader == null)
        {
            headerName = "Proxy-Authorization";
            ProxyAuthenticateHeader proxyAuthenticateHeader = (ProxyAuthenticateHeader)
                response.getHeader(ProxyAuthenticateHeader.NAME);
            sipHeader = (SIPHeader) proxyAuthenticateHeader;
        }
        CSeqHeader cseqHeader = (CSeqHeader) response.getHeader(CSeqHeader.NAME);
        String method = cseqHeader.getMethod();

        System.out.println("processResponseAuthorization()");
        // Proxy-Authorization header:
        
        
         UsernamePasswordCredentials cred =
             new UsernamePasswordCredentials(line.getUser(), line.getPassword());

         AuthScheme authscheme = new DigestScheme();
         try
        {
            authscheme.processChallenge(sipHeader.getHeaderValue());
        }
        catch (MalformedChallengeException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
         
        String uriString = null;
//        if (forRegister == true)
//        {
//            uriString = "sip:" + line.getHost(); 
//        }
//        else
        {
            uriString = newRequest.getRequestURI().toString();
        }
         String responseString = null;
        try
        {
            responseString = authscheme.authenticate(cred, method, uriString);
        }
        catch (AuthenticationException e)
        {
            e.printStackTrace();
        }

        try
        {
                Header header = flibbleProvider.headerFactory.createHeader(headerName, responseString);
                newRequest.addHeader(header);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }        
        
    }

}
