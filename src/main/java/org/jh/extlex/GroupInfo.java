/*
 * Copyright 2024 jhmyr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jh.extlex;

final class GroupInfo {
    private String regexp; 
    private GroupMeth groupMeth;
    private int startPos;
    private int endPos = -1;
    
    GroupInfo(String regexp, GroupMeth groupMeth, int startPos) {
        init(regexp, groupMeth, startPos);
    }
    
    protected void init(String regexp, GroupMeth groupMeth, int startPos) {
        this.regexp = regexp;
        this.groupMeth = groupMeth;
        this.startPos = startPos;        
    }
    
    protected int getEndPos() {
        return endPos;
    }
    
    protected String getRegExp() {
        return regexp;
    }
    
    protected GroupMeth getGroupMeth() {
        return groupMeth;
    }
    
    protected int getStartPos() {
        return startPos;
    }
    
    protected GroupInfo setEndPos(int endPos) {
        this.endPos = endPos;
        
        return this;
    }
    
    protected boolean hasEndPos() {
        return endPos > -1;
    }
}
