/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex;

import java.util.List;
import java.util.function.Consumer;
import org.jh.extlex.util.CharRange;
import java.lang.reflect.Array;

/**
 *
 * @author Jörg
 */
@SuppressWarnings("unchecked")
public class DTransition<S> extends Transition<S> {
    static final Consumer<Integer>[] DEFAULT_GROUP = new Consumer[0];

    Consumer<Integer>[] groups;
    
    public DTransition(CharRange range, S nextDState, List<Consumer<Integer>> groups) {
        super(range, nextDState);
        
        this.groups = groups.isEmpty()? DEFAULT_GROUP : toArray(groups);
    }
    
    final Consumer<Integer>[] toArray(List<Consumer<Integer>> list) {
        Consumer<Integer>[] result = (Consumer<Integer>[])new Consumer[list.size()];
        int i = 0;
        
        for (Consumer<Integer> cons : list) {
            result[i++] = cons;
        }
        
        return result;
    }
    
    @Override
    final public int hashCode() {
        return lowChar;
    }

    public boolean hasGroups() {
        return groups.length > 0;
    }
    
    @Override
    final public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    public void saveGroupPos(int pos) {
        for (Consumer<Integer> cons : groups) {
            cons.accept(pos);
        }
    }
}
