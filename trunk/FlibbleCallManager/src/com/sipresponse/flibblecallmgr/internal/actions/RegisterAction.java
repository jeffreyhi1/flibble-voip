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
                        processResponseAuthorization(response, registerWithAuth, true);

                        ct = flibbleProvider.sendRequest(registerWithAuth);

                    }
                    else if (response.getStatusCode() == 200)
                    {
                        break;
                    }
                    responseCount++;
                    responseEvent = flibbleProvider.waitForResponseEvent(ct);
                    response = responseEvent.getResponse();
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

    public void processResponseAuthorization(Response response,
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

            if (qop != null)
            {
                // Integer randInt = new Integer(rand.nextInt());
                // cnonce = randInt.toString();
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
