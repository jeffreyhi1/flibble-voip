package com.sipresponse.placecall;

import com.sipresponse.flibblecallmgr.CallManager;

public class PlaceCall
{

    public void go()
    {
        CallManager callMgr = new CallManager();
        callMgr.initialize("192.168.0.105",
                5060,
                9300,
                9400,
                null,
                5060,
                false, true);
        String lineHandle = callMgr.addLine("sip:foo@192.168.0.105", "Foo Bar", false);
        String callHandle = callMgr.createCall(lineHandle, "sip:reliagility@192.168.0.103");
        callMgr.placeCall(callHandle);

    }
    /**
     * @param args
     */
    public static void main(String[] args)
    {
        PlaceCall placeCall = new PlaceCall();
        placeCall.go();

    }

}
