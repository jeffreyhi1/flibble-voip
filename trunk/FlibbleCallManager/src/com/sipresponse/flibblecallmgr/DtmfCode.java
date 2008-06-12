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
package com.sipresponse.flibblecallmgr;

public enum DtmfCode
{
    DTMF_0,
    DTMF_1,
    DTMF_2,
    DTMF_3,
    DTMF_4,
    DTMF_5,
    DTMF_6,
    DTMF_7,
    DTMF_8,
    DTMF_9,
    DTMF_STAR,
    DTMF_POUND;
    
    public byte getByte()
    {
        
        byte code = (byte) this.ordinal();
        return code;
    }
}
