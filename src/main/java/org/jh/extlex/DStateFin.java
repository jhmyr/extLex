/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jh.extlex;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jh.extlex.util.Initializer;
import org.jh.extlex.util.SortedList;

/**
 *
 * @author Jörg
 */
@SuppressWarnings("unchecked")
public class DStateFin<T> extends DState {
    String regexp;
    Initializer init;
    TokenMeth<T> matchToken;
    static final Consumer<Integer>[] DEFAULT_GROUP = new Consumer[0];

    Consumer<Integer>[] groups = DEFAULT_GROUP;
    
    DStateFin(SortedList<NDState> states, String stateName, String regexp, Initializer init, TokenMeth<T> matchToken, List<BracketInfo> closingBrackets) {
        super(states, stateName);
        
        this.regexp = regexp;
        this.init = init;
        this.matchToken = matchToken;
        
        if (!closingBrackets.isEmpty()) {
            int pos = 0;
            
            groups = new Consumer[closingBrackets.size()];
            
            for (BracketInfo bracketInfo : closingBrackets) {
                groups[pos++] = bracketInfo::saveEndPos;
            }
        }
    }
    
    @Override
    public final boolean isFinalState() { return true; }

    @Override
    final public int hashCode() {
        return super.hashCode();
    }

    @Override
    final public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    @Override
    final public int compareTo(DState d) {
        return super.compareTo(d);
    }
}