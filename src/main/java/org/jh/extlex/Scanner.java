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

import java.io.IOException;
import java.util.List;
import java.util.Stack;
import java.util.function.Consumer;
import org.jh.extlex.exception.UnknownTokenException;

public class Scanner<T> {
    protected final DState rootState;
    protected final Stack<DState> stackState;
    private final List<GroupInfo> groupList;
    protected final TokenReader tr;
    protected DStateFin<T> finState;

    Scanner(DRootState root, TokenReader tr) {
        this.rootState = root.getStartState();
        this.stackState = root.getStackOfState();
        this.groupList = root.getGroupInfoList();
        this.tr = tr;
    }

    final protected void clear() {
        tr.accepted();
        stackState.clear();
        groupList.clear();

        finState = null;      
    }

    @SuppressWarnings("unchecked")
    final protected boolean handleFinStates(int ppos) throws UnknownTokenException {
        while (finState != null) {
            finState.saveGroupPos(ppos);

            if (!stackState.isEmpty()) {
                DState act = stackState.pop();
                
                finState = act instanceof DStateFin ? (DStateFin<T>) act : null;
            } else {
                break;
            }
        }

        return finState != null;
    }

    @SuppressWarnings("unchecked")
    public boolean hasNext() throws UnknownTokenException, IOException {
        if (tr.reachedEndOfReader()) return false;

        DState act = rootState;
        int ppos = tr.getPos();
        
        clear();

        for (char ch = tr.read(); ch != 0; ch = tr.read()) {
            DTransition<DState> trans = act.getTransition(ch);

            if (trans != null) {
                act = trans.st;

                trans.saveGroupPos(ppos);

                if (act instanceof DStateFin) {
                    finState = (DStateFin<T>) act;

                    tr.mark();
                }
                
                ppos = tr.getPos();
            } else {
                if (finState == null) tr.throwUnknownTokenException();

                ppos = tr.reset();

                if (stackState.isEmpty()) break;

                finState.saveGroupPos(ppos);

                finState = null;
                act = stackState.pop();
            }
        }

        if (!handleFinStates(tr.reset())) {
            throw new UnknownTokenException("Unknown token not read '" + tr.getNonReadString() + "'!");
        }

        return true;
    }

    final public T getNextToken() throws Exception {
        hasNext();

        if (finState != null) {
            if (finState.matchToken != null) {
                return getToken();
            } else {
                applyGroups();
            }
        }
        
        return null;
    }
    
    final public void getAllToken() throws Exception {
        while (hasNext()) {
            if (finState != null) {
                if (finState.matchToken != null) {
                    getToken();
                } else {
                    applyGroups();
                }
            }
        }
    }
    
    final public void getAllTokens(Consumer<T> cons) throws Exception {
        while (hasNext()) {
            if (finState != null) {
                if (finState.matchToken != null) {
                    cons.accept(getToken());
                } else {
                    applyGroups();
                }
            }
        }
    }

    final private void applyGroups() throws Exception {
        String regexp = finState.regexp;
        int delta = tr.getDelta();
        
        for (GroupInfo groupinfo : groupList) {
            if (groupinfo.getRegExp() == regexp) {
                int startPos = groupinfo.getStartPos();

                groupinfo.getGroupMeth().accept(tr.getBuffer(), startPos - delta, groupinfo.getEndPos() - startPos);
            }
        }
    }
    
    final public T getToken() throws Exception {
        if (finState != null) {
            finState.init.init();

            applyGroups();
        
            int offset = tr.getOffset();

            if (finState.matchToken != null) { 
                return finState.matchToken.apply(tr.getBuffer(), offset, tr.getPos() - tr.getDelta() - offset);
            }
        }
        
        return null;
    }
}
