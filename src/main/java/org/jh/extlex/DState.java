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

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.jh.extlex.util.SortedList;
import org.jh.extlex.exception.DuplicateTransitionException;
import org.jh.extlex.util.CharRange;
import org.jh.extlex.util.RangeSet;

public class DState extends State<DState> implements Comparable<DState> {
    private final SortedList<NDState> states; // sorted Lists of States by their stateNo
    private final RangeSet<DTransition<DState>> transitions = new RangeSet<>();
    private final String stateName;

    DState(SortedList<NDState> states, String stateName) {
        this.states = states;
        this.stateName = stateName;
    }

    SortedList<NDState> getStates() { return states; }
    
    public String getName() {
        return stateName;
    }
    
    String statesToName() {
        return statesToName(states);
    }
    
    static String statesToName(SortedList<NDState> states) {
        StringBuilder sbuf = new StringBuilder();
        String space = "";
        
        for (NDState s : states) {
            sbuf.append(space).append(s.getStateNo());
            space = "_";            
        }
        
        return sbuf.toString();
    }

    DTransition<DState> addTransition(CharRange range, DState nextDState, List<Consumer<Integer>> groups) throws DuplicateTransitionException {
        DTransition<DState> result = new DTransition<>(range, nextDState, groups);
        
        if (transitions.add(result) != null) {
            throw new DuplicateTransitionException("DState(" + stateName + ") still contains a transition for '" + range.lowChar + "-" + range.highChar + "'!");
        }
        
        return result;
    }
    
    protected DState find(char key) {
        Transition<DState> trans = transitions.find(key);
        
        return trans != null? trans.st : null;
    }
    
    protected DTransition<DState> getTransition(char key) {
        return transitions.find(key);
    }
    
    protected int getNoOfTransitions() { return transitions.size(); }
    
    @Override
    final protected boolean hasTransition() { return !transitions.isEmpty(); }
    
    protected boolean isFinalState() { return false; }
    
    @Override
    public String toString() { return isFinalState() ? "[" + stateName + "]" : "(" + stateName + ")"; }
    
    final protected int noOfTransitions() {
        return transitions.size();
    }
    
    final protected DTransition<DState> getTransition(int pos) {
        return transitions.get(pos);
    }
    
    @Override
    protected Iterable<Transition<DState>> getTransitions() {
        return () -> new DStateIterator(transitions.iterator());
    }

    @Override
    public int hashCode() {
        return stateName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DState other = (DState) obj;
        
        return Objects.equals(this.stateName, other.stateName);
    }
    
    @Override
    public int compareTo(DState d) {
        return this.stateName.compareTo(d.stateName);
    }
    
    class DStateIterator implements Iterator<Transition<DState>> {
        Iterator<DTransition<DState>> it = transitions.iterator();
    
        DStateIterator(Iterator<DTransition<DState>> it) {
            this.it = it;
        }
        
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Transition<DState> next() {
            return it.next();
        }
    }
}
