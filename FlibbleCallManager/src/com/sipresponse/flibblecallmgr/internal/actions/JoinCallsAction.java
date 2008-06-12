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
package com.sipresponse.flibblecallmgr.internal.actions;


import com.sipresponse.flibblecallmgr.CallManager;
import com.sipresponse.flibblecallmgr.internal.Call;
import com.sipresponse.flibblecallmgr.internal.media.FlibbleMediaProvider;

public class JoinCallsAction extends ActionThread
{
    private Call[] calls;
    public JoinCallsAction(CallManager callMgr,
            Call[] calls)
    {
        super(callMgr, null, null);
        this.calls = calls;
    }

    public void run()
    {
        try
        {
            for (Call c : calls)
            {
                if (null != c)
                {
                    FlibbleMediaProvider provider = c.getMediaProvider();
                    provider.joinOtherCallsWithDataSource(c, calls);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
}
