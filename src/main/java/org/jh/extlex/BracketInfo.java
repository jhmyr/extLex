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

import java.util.List;
import static org.jh.extlex.Logger.DEBUG;
import org.jh.extlex.util.Pair;

class BracketInfo {
    protected String regexp;
    protected GroupMeth groupMeth;
    protected List<GroupInfo> groupList;
    protected Pair<GroupInfo> actgi = null;
    
    public BracketInfo(String regexp, GroupMeth groupMeth, List<GroupInfo> groupList) {
        this.regexp = regexp;
        this.groupMeth = groupMeth;
        this.groupList = groupList;
    }
       
    final protected void saveStartPos(int startpos) {
        DEBUG(String.format("saveStartPos: pos=%d%n", startpos));
        
        actgi = new Pair<>(new GroupInfo(regexp, groupMeth, startpos), actgi);
    }
    
    final protected void saveEndPos(int endpos) {
        GroupInfo gi = actgi.car().setEndPos(endpos);        
        
        DEBUG(String.format("saveEndPos: startpos=%d pos=%d%n", gi.getStartPos(), gi.getEndPos()));
        
        groupList.add(gi);
        
        actgi = actgi.cdr();
    }
}
