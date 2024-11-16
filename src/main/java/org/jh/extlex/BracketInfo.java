/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex;

import java.util.List;
import static org.jh.extlex.Logger.DEBUG;
import org.jh.extlex.util.Pair;

/**
 *
 * @author Jörg
 */
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
