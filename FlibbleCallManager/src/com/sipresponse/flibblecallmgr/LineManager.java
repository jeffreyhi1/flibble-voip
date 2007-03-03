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
package com.sipresponse.flibblecallmgr;

import java.util.concurrent.ConcurrentHashMap;

public class LineManager
{
    private Object syncObj = new Object();
    private static int lineHandle = 0;
    private ConcurrentHashMap<String, Line> lines = 
        new ConcurrentHashMap<String, Line>();
    public String addLine(String sipUrl, boolean register)
    {
        String sLineHandle = null;
        synchronized (syncObj)
        {
            sLineHandle = new Integer(++lineHandle).toString();
        }
        Line line = new Line();
        line.setHandle(sLineHandle);
        line.setSipUrl(sipUrl);
        line.setRegister(register);
        return sLineHandle;
    }

}