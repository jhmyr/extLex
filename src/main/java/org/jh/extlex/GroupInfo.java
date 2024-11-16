/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex;

/**
 *
 * @author Jörg
 */

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
