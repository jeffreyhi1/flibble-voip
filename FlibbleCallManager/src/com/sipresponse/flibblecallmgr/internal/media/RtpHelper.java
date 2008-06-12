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
package com.sipresponse.flibblecallmgr.internal.media;

import com.sipresponse.flibblecallmgr.DtmfCode;

public class RtpHelper
{
    public static int getSeqNo(byte[] data)
    {
        
        int seqNo = byteArrayToInt((byte)0x00, (byte)0x00, data[2], data[3]);
        return seqNo;
    }
    
    public static int getTimestamp(byte[] data)
    {
        return byteArrayToInt(data[4], data[5], data[6], data[7]);
    }
    
    public static int getSSID(byte[] data)
    {
        return byteArrayToInt(data[8], data[9], data[10], data[11]);
    }
    
  public static byte[] shortToByteArray(short value) {
                byte[] serverValue = new byte[2];
                serverValue[0] = (byte) (value >>> 8);
                serverValue[1] = (byte) value;
                return serverValue;
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
    public static int byteArrayToShort(byte b1, byte b2)
    {
        int value = 0;
        value = (0xFF & b2);
        value += ( b1 & 0xFF) << 8;
        return value;
    }
    
  public static short bytesToShort(byte[] bytes, int off, int len,
    boolean little)
  {
    if (bytes.length - off < len) len = bytes.length - off;
    short total = 0;
    for (int i=0, ndx=off; i<len; i++, ndx++) {
      total |= (bytes[ndx] < 0 ? 256 + bytes[ndx] :
        (int) bytes[ndx]) << ((little ? i : len - i - 1) * 8);
    }
    return total;
  }    
  
  public static short bigEndianBytesToShort (byte[] buffer, int offset)
  {
    return (short) (((buffer[offset+0] & 0xff) << 8) +
                    ((buffer[offset+1] & 0xff) << 0));
  }

  public static short littleEndianBytesToShort (byte[] buffer, int offset)
  {
    return (short) (((buffer[offset+1] & 0xff) << 8) +
                    ((buffer[offset+0] & 0xff) << 0));
  }

    public static byte[] createDtmfEvent(int payloadId,
            int seqNo,
            long timestamp,
            long ssid,
            int code,
            boolean marker,
            boolean end)
    {
        byte[] data = new byte[16];
        data[0] = (byte) 0x80; // version, padding, extension, contributing
        data[1] = (byte) payloadId;
        if (true == marker)
        {
            data[1] &= (byte)0x80;
        }
        data[2] = (byte)((seqNo >> 8) & 0xFF);
        data[3] = (byte)(seqNo        & 0xFF);
        
        // TIMESTAMP
        data[4] = (byte)((timestamp >> 24)& 0xFF);
        data[5] = (byte)((timestamp >> 16)& 0xFF); 
        data[6] = (byte)((timestamp >> 8) & 0xFF); 
        data[7] = (byte) (timestamp       & 0xFF);
        
        // SSID
        data[8]  = (byte)((ssid >> 24)& 0xFF);
        data[9]  = (byte)((ssid >> 16)& 0xFF); 
        data[10] = (byte)((ssid >> 8) & 0xFF); 
        data[11] = (byte) (ssid       & 0xFF);
        
        data[12] = (byte) code;
        data[13] = (byte)0x00;
        data[14] = (byte)0x00;
        data[15] = (byte)0xFA;
        
        
        return data;
    }
}
