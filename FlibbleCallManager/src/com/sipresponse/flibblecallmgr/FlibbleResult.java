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

/**
 * Enumeration of flibble method invocation results.
 * @author michael cohen
 */
public enum FlibbleResult
{
    RESULT_SUCCESS,
    RESULT_INVALID_PARAMS,
    RESULT_UNKNOWN_FAILURE
    ;
    
    static String getDescription(FlibbleResult result)
    {
        String desc = null;
        
        // TODO - get these from an external file, 
        //        for easier localization
        switch (result)
        {
            case RESULT_SUCCESS:
            {
                desc = "OK";
                break;
            }
            case RESULT_INVALID_PARAMS:
            {
                desc = "Invalid Parameters";
                break;
            }
            case RESULT_UNKNOWN_FAILURE:
            {
                desc = "Unknown Failure";
                break;
            }
        }
        return desc;
    }
    
}
