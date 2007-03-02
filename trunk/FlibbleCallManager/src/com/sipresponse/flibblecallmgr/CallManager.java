package com.sipresponse.flibblecallmgr;

public class CallManager
{
    private FlibbleMediaProvider mediaProvider;
    private FlibbleUiProvider uiProvider;
    private static CallManager instance;
    public static synchronized CallManager getInstance()
    {
        if (null == instance)
        {
            instance = new CallManager();
        }
        return instance;
    }
    private CallManager()
    {
    }
    public void initialize(FlibbleUiProvider uiProvider,
                           FlibbleMediaProvider mediaProvider)
    {
        
    }
                           
}
