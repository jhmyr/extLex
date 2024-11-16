/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jh.extlex;

import org.jh.extlex.util.SortedList;
import java.util.List;
import java.util.function.Consumer;
import org.jh.extlex.util.CharRange;

/**
 *
 * @author Jörg
 */
public class OpenBracketState extends NDState {
    int bracketNo;
    BracketInfo bracketInfo;
    
    OpenBracketState(int bracketNo, NDState next, BracketInfo brinfo) {
        this(bracketNo, brinfo);
        
        addEmptyTransition(next);
    }
 
    OpenBracketState(int bracketNo, BracketInfo brinfo) {
        this.bracketNo = bracketNo;
        this.bracketInfo = brinfo;
    }
    
    @Override 
    public String toString() {
        return "(" + bracketNo;
    }    

    @Override
    final protected boolean getNextStates(SortedList<NDState> nextStates, SingleNumber stateNo, int checkNo, 
        CharRange range, List<Consumer<Integer>> groupSavers, MutableRecursiveState recursivState) throws Exception {
        if (super.getNextStates(nextStates, stateNo, checkNo, range, groupSavers, recursivState)) {
            groupSavers.add(this instanceof CloseBracketState ? bracketInfo::saveEndPos : bracketInfo::saveStartPos);
            
            return true;
        }
        
        return false;
    }

    final public int getBracketNo() { return bracketNo; }
}
