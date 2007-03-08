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

public class Event
{
    private EventType eventType;
    private EventCode eventCode;
    private EventReason eventReason;
    
    public Event(EventType eventType,
                 EventCode eventCode,
                 EventReason eventReason)
    {
        this.eventType = eventType;
        this.eventCode = eventCode;
        this.eventReason = eventReason;
    }
    public EventType getEventType()
    {
        return eventType;
    }
    public void setEventType(EventType eventType)
    {
        this.eventType = eventType;
    }
    public EventCode getEventCode()
    {
        return eventCode;
    }
    public void setEventCode(EventCode eventCode)
    {
        this.eventCode = eventCode;
    }
    public EventReason getEventReason()
    {
        return eventReason;
    }
    public void setEventReason(EventReason eventReason)
    {
        this.eventReason = eventReason;
    }
}
