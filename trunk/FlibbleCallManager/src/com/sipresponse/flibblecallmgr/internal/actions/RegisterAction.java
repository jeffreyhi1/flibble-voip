package com.sipresponse.flibblecallmgr.internal.actions;

import java.text.ParseException;

import gov.nist.javax.sip.Utils;
import gov.nist.javax.sip.header.ProxyAuthenticate;
import gov.nist.javax.sip.header.SIPHeaderNames;

import javax.sip.ClientTransaction;
import javax.sip.InvalidArgumentException;
import javax.sip.ObjectInUseException;
import javax.sip.ResponseEvent;
import javax.sip.SipProvider;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ExpiresHeader;
import javax.sip.header.ProxyAuthorizationHeader;
import javax.sip.message.Request;
import javax.sip.message.Response;

import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.Event;
import com.sipresponse.flibblecallmgr.EventCode;
import com.sipresponse.flibblecallmgr.EventReason;
import com.sipresponse.flibblecallmgr.EventType;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.FlibbleSipProvider;
import com.sipresponse.flibblecallmgr.internal.InternalCallManager;
import com.sipresponse.flibblecallmgr.internal.Line;
import com.sipresponse.flibblecallmgr.internal.LineManager;
import com.sipresponse.flibblecallmgr.internal.util.DigestClientAuthenticationMethod;

public class RegisterAction extends Thread
{
    private int timeout = 8000;
    private CallManager callMgr;
    private Line line;
    
    public RegisterAction(CallManager callMgr, Line line)
    {
        this.callMgr = callMgr;
        this.line = line;
    }
    public void run()
    {
        InternalCallManager.getInstance().fireEvent(callMgr, new Event(EventType.LINE, EventCode.LINE_REGISTERING, EventReason.LINE_NORMAL));

        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance()
            .getProvider(callMgr);
        
        Request register = createRegisterRequest();
        if (register != null)
        {
            flibbleProvider.sendRequest(register);
            
            // create the transaction
            ClientTransaction ct = flibbleProvider.sendRequest(register);
            
            // wait for a response
            ResponseEvent responseEvent = flibbleProvider.waitForResponseEvent(ct);
            if (null != responseEvent)
            {
                Response response = responseEvent.getResponse();
                int responseCount = 0;
                boolean bRegistered = false;
                while (response.getStatusCode() != 200 &&
                       responseCount < 8)
                {
                    if (response.getStatusCode() == 401 ||
                        response.getStatusCode() == 403)
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
                        processResponseAuthorization(response, registerWithAuth);
                        
                        ct = flibbleProvider.sendRequest(registerWithAuth);
                        
                    }
                    else if (response.getStatusCode() == 200)
                    {
                        bRegistered = true;
                        break;
                    }
                    responseCount++;
                    responseEvent = flibbleProvider.waitForResponseEvent(ct);
                    response = responseEvent.getResponse();
                }
                if (false == bRegistered)
                {
                    InternalCallManager.getInstance().fireEvent(callMgr, new Event(EventType.LINE, EventCode.LINE_REGISTER_FAILED, EventReason.LINE_NORMAL));
                }
                else
                {
                    InternalCallManager.getInstance().fireEvent(callMgr, new Event(EventType.LINE, EventCode.LINE_REGISTERED, EventReason.LINE_NORMAL));
                }
            }
            else
            {
                InternalCallManager.getInstance().fireEvent(callMgr, new Event(EventType.LINE, EventCode.LINE_REGISTER_FAILED, EventReason.LINE_NORMAL));
            }
        }
    }
    
    private Request createRegisterRequest()
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance().getProvider(callMgr);
        Request register = null;
        register = flibbleProvider.createRequest(Utils.generateCallIdentifier(callMgr.getLocalIp()),
                                                 Request.REGISTER,
                                                 line.getSipUri(),
                                                 line.getSipUri());
        ExpiresHeader expiresHeader = null;
        try
        {
            expiresHeader = flibbleProvider.headerFactory.createExpiresHeader(line.getRegisterPeriod());
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

    public void processResponseAuthorization(Response response, Request newRequest)
    {
        FlibbleSipProvider flibbleProvider = InternalCallManager.getInstance().getProvider(callMgr);
        Request requestauth = null;
        try {
            String realm = "127.0.0.1";
            System.out.println("processResponseAuthorization()");
            String schema = ((ProxyAuthenticate)(response.getHeader(SIPHeaderNames.PROXY_AUTHENTICATE))).getScheme();
            String nonce = ((ProxyAuthenticate)(response.getHeader(SIPHeaderNames.PROXY_AUTHENTICATE))).getNonce();
            ProxyAuthorizationHeader proxyAuthheader = flibbleProvider.headerFactory.createProxyAuthorizationHeader(schema);
            proxyAuthheader.setRealm(realm);
            proxyAuthheader.setNonce(nonce);
            proxyAuthheader.setAlgorithm("MD5");
            proxyAuthheader.setUsername(line.getUser());
            proxyAuthheader.setURI(newRequest.getRequestURI());
            DigestClientAuthenticationMethod digest=new DigestClientAuthenticationMethod();

            digest.initialize(realm,
                    line.getUser(),
                    newRequest.getRequestURI().toString(),
                    nonce,line.getPassword(),
                    ((CSeqHeader) response.getHeader(CSeqHeader.NAME)).getMethod(),
                    null,
                    "MD5");
            System.out.println("Proxy Response antes de modificarlo : " + proxyAuthheader.getResponse());
            String digestResponse = digest.generateResponse();
            proxyAuthheader.setResponse(digestResponse);
            requestauth.addHeader(proxyAuthheader);
        }
        catch (Exception ex)
        {
            System.out.println("processResponseAuthorization() Exception:");
            System.out.println(ex.getMessage());
            ex.printStackTrace();
        }
    }
    
}
