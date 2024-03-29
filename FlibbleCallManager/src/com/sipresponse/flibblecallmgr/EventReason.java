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

public enum EventReason
{
    // CALL REASONS
    CALL_NORMAL,
    CALL_BUSY,
    CALL_TEMPORARILY_UNAVAILABLE,
    CALL_USER_NOT_FOUND,
    CALL_FAILURE_NETWORK,
    CALL_FAILURE_REJECTED,
    CALL_FAILED_REQUEST,
    CALL_FAILED_LOOP_DETECTED,
    CALL_TRANSFER_AS_CONTROLLER,
    CALL_TRANSFER_AS_TRANSFEREE,
    CALL_TRANSFER_AS_TARGET,
    CALL_DISCONNECT_LOCAL,
    CALL_DISCONNECT_REMOTE,
    CALL_UNHOLD,
    CALL_CANCELLED,
    
    // LINE REASONS
    LINE_NORMAL,
    LINE_NETWORK,
    
    // MEDIA REAONS
    MEDIA_NORMAL,
    
    // VOICE RECOGNITION REASONS
    VR_NORMAL,
}
