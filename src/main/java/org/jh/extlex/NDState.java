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

import org.jh.extlex.util.SortedList;
import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import org.jh.extlex.util.CharRange;
import org.jh.extlex.util.CharSet;
import org.jh.extlex.util.ConsumerException;
import org.jh.extlex.util.RangeSet;
import org.jh.extlex.util.SortKey;

class NDState extends State<NDState> implements SortKey {
    protected int stateNo = 0;
    private DState recState = null;
    RangeSet<Transition<NDState>> transSet = new RangeSet<>();
    
    NDState() {
    }
    
    NDState(int stateNo) {
        this.stateNo = stateNo;
    }
    
    NDState(int stateNo, NDState state) {
        this(stateNo);
        
        addEmptyTransition(state);
    }
    
    NDState(List<Transition<NDState>> acttrans) {       
        Transition<NDState> trans = new Transition<>();
        
        acttrans.add(trans);
        
        transSet.append(trans);
    }
    
    NDState(NDState nextState) {
        addEmptyTransition(nextState);
    }
    
    protected void addRecState(DState recState) throws Exception {
        if (this.recState != null) {
            throw new Exception("State still has a recursive state");
        } else {
            this.recState = recState; 
        }
    }
    
    // precondition the unique CharRange was calculated before
    protected NDState find(CharRange cr, int checkNo, ConsumerException<NDState> proc) throws Exception {
        if (yetChecked(checkNo)) return null;
       
        NDState result = null;
        
        for (Transition<NDState> t : getEmptyTransitions()) {
            result = t.st.find(cr, checkNo, proc);
        }
        
        Transition<NDState> t = transSet.find(cr);

        if (t != null) {
            if (result != null) {
                throw new Exception("Internal find double results!");
            }
            
            proc.accept(t.st);
            
            result = this;
        }
        
        return result;
    }
    
    protected boolean findEmptyTransitionToFinState(int checkNo) {
        if (yetChecked(checkNo)) return false;
        
        for (Transition<NDState> t : getEmptyTransitions()) {
            if (t.st instanceof FinState) {
                return true;
            } else {
                if (t.st.findEmptyTransitionToFinState(checkNo)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    // precondition the unique CharRange was calculated before
    // ToDo: But there could be several NDStates!
    protected NDState findForward(CharRange cr, int checkNo) {
        if (yetChecked(checkNo)) return null;
       
        for (Transition<NDState> t : getEmptyTransitions()) {
            if (t.isForwardTransition()) {
                NDState result = t.st.findForward(cr, checkNo);
                
                if (result != null) return result;
            }
        }
        
        Transition<NDState> trans = transSet.find(cr);
        
        return trans == null? null : trans.st;
    }
    
    protected DState getRecState() {
        return recState;
    }
    
    protected DState releaseRecState() {
        DState dstate = recState;
        
        recState = null;
        
        return dstate;
    }
    
    final protected boolean hasRecState() {
        return recState != null;
    }
        
    @Override
    final protected boolean hasTransition() { 
        return  !transSet.isEmpty();
    }
    
    protected boolean isFinalState() { return false; }
        
    @Override
    public String toString() {
        return isFinalState() ? "[" + stateNo + "]" : "(" + stateNo + ")";
    }
    
    protected final Transition<NDState> addTransition(char key) throws Exception {
        Transition<NDState> trans = new Transition<>(key);
        
        if (transSet.add(trans) != null) {
            throw new DoubleEntryException("Double entry '" + key + "' not allowed!");
        }

        return trans;
    }

    protected final Transition<NDState> addTransition(char key, boolean forward, NDState next) throws Exception {
        Transition<NDState> trans = new Transition<>(key, forward, next);
        
        if (transSet.add(trans) != null) {
            throw new DoubleEntryException("Double entry '" + key + "' not allowed!");
        }

        return trans;
    }

    protected final NDState addTransition(char key, List<Transition<NDState>> tlist) throws Exception {
        Transition<NDState> trans = new Transition<>(key);

        if (transSet.add(trans) != null) {
            throw new DoubleEntryException("Double entry '" + key + "' not allowed!");
        }
        
        tlist.add(trans);
        
        return this;
    }
    
    protected final NDState addTransition(int lkey, int hkey, List<Transition<NDState>> tlist) throws Exception {
        Transition<NDState> trans = new Transition<>(lkey, hkey);

        if (transSet.add(trans) != null) {
            throw new DoubleEntryException("Double entry '" + lkey + "-" + hkey + "' not allowed!");
        }
        
        tlist.add(trans);
        
        return this;
    }
    
    final protected NDState add(char ch) throws Exception {
        return addTransition(ch).st = new NDState();
    }
    
    final protected String getKeys() {
        StringBuilder sbuf = new StringBuilder();
        String space = "";
        
        for (Transition<NDState> t : transSet) {
            sbuf.append(space).append((char)t.lowChar);
            space = " ";
        }
        
        return sbuf.toString();
    }

    final protected void addEmptyTransition(List<Transition<NDState>> acttrans) {
        Transition<NDState> trans = new Transition<>();
        
        acttrans.add(trans);
        
        transSet.append(trans);
    }

    final protected void addEmptyTransition(NDState nextState) {
        transSet.append(new Transition<>(nextState));
    }
    
    final protected void addEmptyBackwardTransition(NDState nextState) {
        transSet.append(new Transition<>(nextState, false));
    }
    
    final protected void getNextChars(CharSet scl, int checkNo) {
        if (yetChecked(checkNo)) return;
        
        for (Transition<NDState> t : transSet) {
            if (t.isEmptyTransaction()) {
                t.st.getNextChars(scl, checkNo);
            } else {
                scl.add((char)t.lowChar, (char)t.highChar);
            }
        }
    }
    
    final protected int getStateNo() { return stateNo; }
    
    final protected void setUnassignedNO(SingleNumber stateNo) {
        if (this.stateNo == 0) {
            this.stateNo = stateNo.getNextNumber();
        }
    }
    
    @Override
    final public int sortKey() {
        return stateNo;
    }

    protected boolean hasNonEmptyTransition(int checkNo) {
        if (!yetChecked(checkNo)) {
             for (Transition<NDState> t : getTransitions()) {
                if (t.isCharTransaction() || t.st.hasNonEmptyTransition(checkNo)) {
                    return true;
                }
             }
        }
        
        return false;
    }
    
    protected boolean getNextStates(SortedList<NDState> nextStates, SingleNumber stateNo, int checkNo,
        CharRange range, List<Consumer<Integer>> groupSavers, MutableRecursiveState recursivState) throws Exception {
        boolean matchChar = false;
        
        // test for loop
        if (!yetChecked(checkNo)) {
            for (Transition<NDState> t : getEmptyTransitions()) {
                if (t.st.getNextStates(nextStates, stateNo, checkNo, range, groupSavers, recursivState)) {
                    if (!t.isForwardTransition() && this instanceof RecursiveState) {
                        recursivState.setState((RecursiveState) this);                
                    }
                    
                    matchChar = true;
                }
            }

            Transition<NDState> t = transSet.find(range);

            if (t != null) {
                NDState st = t.st;

                st.setUnassignedNO(stateNo);

                nextStates.add(st);

                matchChar = true;
            }
        }
        
        return matchChar;
    }

    @Override
    final public Iterable<Transition<NDState>> getTransitions() {
        return () -> new Iterator<Transition<NDState>>() {
            int size = transSet.size();
            int pos = 0;

            @Override
            public boolean hasNext() {
                return pos < size;
            }

            @Override
            public Transition<NDState> next() {
                if (pos > size) {
                    throw new NoSuchElementException("No element exists!");
                }
                return transSet.get(pos++);
            }
        };
    }
    
    final protected boolean isBefore(NDState st, int checkNo) {
        if (!yetChecked(checkNo)) { 
            for (Transition<NDState> t : getTransitions()) {
                if (t.isForwardTransition() && (t.st == st || t.st.isBefore(st, checkNo))) {
                    return true;
                }
            }
        }
    
        return false;
    }
    
    final protected Iterable<Transition<NDState>> getEmptyTransitions() {
        return () -> new Iterator<Transition<NDState>>() {
            int pos = transSet.size() -1;
            Transition<NDState> trans = null;
            
            @Override
            public boolean hasNext() {
                return pos >= 0 && (trans = transSet.get(pos--)).isEmptyTransaction();
            }
            
            @Override
            public Transition<NDState> next() {
                if (pos < -1 || !trans.isEmptyTransaction()) {
                    throw new NoSuchElementException("No element exists!");
                }
                return trans;
            }
        };
    }

    final protected Transition<NDState> newTransition(int lowChar, int highChar, List<Transition<NDState>> openTransitions) {
        Transition<NDState> trans = new Transition<>(lowChar, highChar);
    
        openTransitions.add(trans);
        return trans;
        
    }
    
    void invertTransSet(List<Transition<NDState>> openTransitions) {
        RangeSet<Transition<NDState>> newTransSet = new RangeSet<>();
        int lowChar = 0;
        
        for (Transition<NDState> t : transSet) {
            openTransitions.remove(t);
            
            if (t.lowChar > lowChar) {
                newTransSet.add(newTransition(lowChar, t.lowChar-1, openTransitions));
            }    
                
            lowChar = t.highChar+1;
        }
        
        if (lowChar < Character.MAX_CODE_POINT) {
            newTransSet.add(newTransition(lowChar, Character.MAX_CODE_POINT, openTransitions));
        }
        
        transSet = newTransSet;
    }
    
    final FinState reachFinState(int checkNo) {
        if (!yetChecked(checkNo)) {
            if (this instanceof FinState) {
                return (FinState)this;
            }

            for (Transition<NDState> t : getEmptyTransitions()) {
                FinState fstate = t.st.reachFinState(checkNo);
                
                if (fstate != null) return fstate;
            }
        } 
        return null;
    }
}
