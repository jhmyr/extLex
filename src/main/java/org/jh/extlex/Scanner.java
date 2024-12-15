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
    private final DState rootState;
    private final Stack<DState> stackState;
    private final List<GroupInfo> groupList;
    private final TokenReader tr;
    private DStateFin<T> finState;

    Scanner(DRootState root, TokenReader tr) {
        this.rootState = root.getStartState();
        this.stackState = root.getStackOfState();
        this.groupList = root.getGroupInfoList();
        this.tr = tr;
    }
    
    @SuppressWarnings("unchecked")
    public boolean hasNext() throws UnknownTokenException, IOException {
        tr.accepted();

        finState = null;

        DState act = rootState;
        int ppos = tr.getPos();

        if (tr.reachedEndOfReader()) {
            return false;
        }

        stackState.clear();
        groupList.clear();

        for (char ch = tr.read(); ch != 0; ch = tr.read()) {
            DTransition<DState> trans = act.getTransition(ch);

            if (trans != null) {
                act = trans.st;

                if (Logger.DEBUG) {
                    System.out.printf("     Matcher: %s -%c-> %s\n", act.toString(), ch, act.toString());
                }

                for (Consumer<Integer> cons : trans.groups) {
                    cons.accept(ppos);
                }

                if (act instanceof DStateFin) {
                    finState = (DStateFin<T>) act;

                    tr.mark();
                }
            } else {
                if (finState == null) {
                    int offset = tr.getOffset(); // Startpunkt
                    int trlen = tr.getPos() - tr.getDelta() - offset;

                    throw new UnknownTokenException("Unknown token '" + new String(tr.getBuffer(), offset, trlen) + "'!");
                }

                tr.reset();

                if (stackState.isEmpty()) {
                    break;
                }

                ppos = tr.getPos();

                for (Consumer<Integer> cons : finState.groups) {
                    cons.accept(ppos);
                }

                finState = null;
                act = stackState.pop();
            }

            ppos = tr.getPos();
        }

        ppos = tr.reset();

        while (finState != null) {
            for (Consumer<Integer> cons : finState.groups) {
                cons.accept(ppos);
            }

            if (!stackState.isEmpty()) {
                act = stackState.pop();

                if (Logger.DEBUG) {
                    System.out.printf("     Matcher: pop state %s\n", act.getName());
                }

                finState = act instanceof DStateFin ? (DStateFin<T>) act : null;
            } else {
                break;
            }
        }

        if (finState == null) {
            throw new UnknownTokenException("Unknown token not read '" + tr.getNonReadString() + "'!");
        }

        return true;
    }

    public T getNextToken() throws Exception {
        hasNext();

        if (finState != null) {
            if (finState.matchToken != null) {
                return getToken();
            } else {
                getGroups();
            }
        }
        
        return null;
    }
    
    public void getAllToken() throws Exception {
        while (hasNext()) {
            if (finState != null) {
                if (finState.matchToken != null) {
                    getToken();
                } else {
                    getGroups();
                }
            }
        }
    }
    
    public void getAllTokens(Consumer<T> cons) throws Exception {
        while (hasNext()) {
            if (finState != null) {
                if (finState.matchToken != null) {
                    cons.accept(getToken());
                } else {
                    getGroups();
                }
            }
        }
    }

    private void getGroups() throws Exception {
        String regexp = finState.regexp;
        int delta = tr.getDelta();
        
        for (GroupInfo groupinfo : groupList) {
            if (groupinfo.getRegExp().equals(regexp)) {
                int startPos = groupinfo.getStartPos();

                groupinfo.getGroupMeth().accept(tr.getBuffer(), startPos - delta, groupinfo.getEndPos() - startPos);
            }
        }
    }
    
    public T getToken() throws Exception {
        finState.init.init();

        getGroups();
        
        int delta = tr.getDelta();
        int offset = tr.getOffset();

        return finState.matchToken != null 
                ? finState.matchToken.apply(tr.getBuffer(), offset, tr.getPos() - delta - offset)
                : null;
    }
}
