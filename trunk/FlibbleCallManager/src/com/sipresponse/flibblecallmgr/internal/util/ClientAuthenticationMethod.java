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

/*
 *   SOURCE FROM: NIST JAIN SIP PROJECT
 *   
 *   
 */

package com.sipresponse.flibblecallmgr.internal.util;

/**
 * Get this interface from the nist-sip IM
 * @author  olivier deruelle
 */
public interface ClientAuthenticationMethod {
    
    /**
     * Initialize the Client authentication method. This has to be
     * done outside the constructor.
     * @throws Exception if the parameters are not correct.
     */
    public void initialize(String realm,String userName,String uri,String nonce
    ,String password,String method,String cnonce,String algorithm) throws Exception;
    
    
    /**
     * generate the response
     * @returns null if the parameters given in the initialization are not
     * correct.
     */
    public String generateResponse();
    
}
