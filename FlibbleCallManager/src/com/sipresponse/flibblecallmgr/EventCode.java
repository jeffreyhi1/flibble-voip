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

public enum EventCode
{
    // CALL CODES
    CALL_TRYING,
    CALL_REMOTE_RINGING,
    CALL_LOCAL_RINGING,
    CALL_CONNECTED,
    CALL_TALKING,
    CALL_HELD_BY_REMOTE_PARTY,
    CALL_HOLDING_REMOTE_PARTY,
    CALL_BIDIRECTIONAL_HOLD,
    CALL_FAILED,
    CALL_DISCONNECTED,
    
    // LINE CODES
    LINE_UNREGISTERED,
    LINE_UNREGISTERING,
    LINE_REGISTERING,
    LINE_REGISTERED,
    LINE_PROVISIONED,
    
}
