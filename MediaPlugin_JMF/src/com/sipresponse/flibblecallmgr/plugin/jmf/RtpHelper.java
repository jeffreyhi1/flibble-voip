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
package com.sipresponse.flibblecallmgr.plugin.jmf;

public class RtpHelper
{
    public static int getSeqNo(byte[] data)
    {
        
        int seqNo = byteArrayToInt((byte)0x00, (byte)0x00, data[2], data[3]);
        return seqNo;
    }
    
    public static int byteArrayToInt(byte b1, byte b2, byte b3, byte b4)
    {
        int value = 0;
        value = (0xFF & b4);
        value += ( b3 & 0xFF) << 8;
        value += (b2 & 0xFF) << 16;
        value += (b1 & 0xFF) << 24;
        return value;
    }
    
}
