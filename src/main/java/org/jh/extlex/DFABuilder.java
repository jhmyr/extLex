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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Consumer;
import static org.jh.extlex.Logger.DEBUG;
import org.jh.extlex.exception.AmbiguousRuleException;
import org.jh.extlex.util.CharRange;
import org.jh.extlex.util.CharSet;
import org.jh.extlex.util.SortedList;

/**
 *
 * @author Jörg
 */
public class DFABuilder<T> {
    private Stack<DState> stackOfState;
    private final Map<DState, List<String>> debugInfoDState = new TreeMap<>();
    private final SingleNumber checkNo;
    private final SingleNumber stateNo;
    private final List<GroupInfo> groupInfoList;
    private final int maxGroupCount;

    public DFABuilder(SingleNumber checkNo, SingleNumber stateNo, List<GroupInfo> groupInfoList, int maxGroupCount) {
        this.checkNo = checkNo;
        this.stateNo = stateNo;
        this.groupInfoList = groupInfoList;
        this.maxGroupCount = maxGroupCount;
    }
    
    boolean existsRecState(SortedList<NDState> states) {
        boolean existRecState = false;

        for (NDState state : states) {
            existRecState |= state.hasRecState();
        }
        
        return existRecState;
    }

    void addDStateDebugInfo(DState dstate, String message) {
        List<String> messages = debugInfoDState.get(dstate);

        if (messages == null) {
            debugInfoDState.put(dstate, messages = new ArrayList<>());
        }

        messages.add(message);
    }

    protected DRootState createDFA(NDState root) throws Exception {
        stackOfState = new Stack<>();

        // put first step as DState node in queue with the start node as list!
        SortedList<NDState> actStateList = new SortedList<>(); // sorted by their stateNo
        List<DState> dstqueues = new ArrayList<>();
        CharSet scl = new CharSet();

        actStateList.add(root);

        String stateName = DState.statesToName(actStateList);
        DState dstart = createDState(actStateList, stateName);
        List<DState> rootStateList = new ArrayList<>();
        Map<String, DState> dstates = new HashMap<>();
        List<Consumer<Integer>> groupSavers = new ArrayList<>();

        rootStateList.add(dstart);
        dstates.put(stateName, dstart);
        dstqueues.add(dstart);

        while (!dstqueues.isEmpty()) {
            DState actdstate = dstqueues.remove(0);
            SortedList<NDState> states = actdstate.getStates();
            boolean containRecState = existsRecState(states);
            Transition<DState> prev = null;
            DState prevState = null;
            MutableRecursiveState recursivState = new MutableRecursiveState();

            getNextChars(states, scl, checkNo.getNextNumber());

            // the character range is orderered
            for (CharRange cr : scl) {
                SortedList<NDState> nextStates
                    = getNextStates(states, stateNo, checkNo.getNextNumber(), cr, groupSavers, recursivState);
                String nextStateName = DState.statesToName(nextStates);
                DState nextDState = dstates.get(nextStateName);
                boolean createNextDState = false;

                if (nextDState == null) {
                    nextDState = createDState(nextStates, nextStateName);
                    createNextDState = true;

                    dstates.put(nextStateName, nextDState);
                    dstqueues.add(nextDState);
                } else {
                    // Only merge characters if there is no group && no recursive state
                    if (prevState == nextDState && prev != null && groupSavers.isEmpty()
                        && !recursivState.exists() && prev.couldBeExtendedBy(cr)) {
                        continue;
                    }
                }

                if (recursivState.exists()) {
                    if (containRecState) {
                        throw new Exception(DState.statesToName(actStateList) + " may not contain a recursive state!");
                    }
                    RecursiveState rstate = recursivState.getState();
                    NDState ristate = rstate.getInternState();
                    
                    ristate.setUnassignedNO(stateNo);
                    
                    String rstateName = Integer.toString(ristate.getStateNo());
                    DState rDState = dstates.get(rstateName);

                    if (rDState == null) {
                        rDState = createDState(new SortedList<>(ristate), rstateName);

                        dstates.put(rstateName, rDState);
                        dstqueues.add(rDState);
                        rootStateList.add(rDState);
                    }

                    final DState lambdaState = rDState;

                    if (createNextDState) {
                        addDStateDebugInfo(actdstate, "set recursive state for '" + cr + "' to " + lambdaState.getName());
                        rstate.find(cr, checkNo.getNextNumber(), x -> x.addRecState(lambdaState));
                    } else {
                        addDStateDebugInfo(actdstate, "push recursive state for '" + cr + "' to " + nextStateName);
                        if (Logger.DEBUG) {
                            groupSavers.add(x -> {
                                DEBUG(String.format("  Transition: push %s on stack%n", lambdaState.getName()));
                                stackOfState.add(lambdaState);
                            });
                        } else {
                            groupSavers.add(x -> stackOfState.add(lambdaState));
                        }
                    }
                }

                if (containRecState) {
                    for (NDState state : states) {
                        NDState next;
                        if (state.hasRecState() && (next = state.findForward(cr, checkNo.getNextNumber())) != null) {
                            if (createNextDState) {
                                addDStateDebugInfo(actdstate, "set recursive state for '" + cr + "' to " + nextStateName);
                                next.addRecState(state.releaseRecState());
                            } else {
                                addDStateDebugInfo(actdstate, "push recursive state for '" + cr + "' to " + nextStateName);
                                
                                DState recState = state.getRecState();
                                
                                if (Logger.DEBUG) {
                                    groupSavers.add(x -> {
                                        DEBUG(String.format("  Transition: push %s on stack%n", recState.getName()));
                                        stackOfState.add(recState);
                                    });
                                } else {
                                    groupSavers.add(x -> stackOfState.add(recState));
                                }
                            }
                        }
                    }
                }

                prev = actdstate.addTransition(cr, nextDState, groupSavers);
                prevState = groupSavers.isEmpty() ? nextDState : null;

                recursivState.clear();
            }
        }

        return new DRootState(rootStateList, groupInfoList, maxGroupCount, stackOfState, debugInfoDState);
    }

    private void getNextChars(SortedList<NDState> states, CharSet scl, int checkNo) {
        scl.clear();

        for (NDState is : states) {
            is.getNextChars(scl, checkNo);
        }
    }

    private SortedList<NDState> getNextStates(SortedList<NDState> states,
        SingleNumber stateNo, int checkNo, CharRange cr, List<Consumer<Integer>> groupSavers,
        MutableRecursiveState recursivState) throws Exception {
        SortedList<NDState> nextStates = new SortedList<>();

        groupSavers.clear();

        for (NDState is : states) {
            is.setUnassignedNO(stateNo); 
            is.getNextStates(nextStates, stateNo, checkNo, cr, groupSavers, recursivState);
        }

        return nextStates;
    }

    List<BracketInfo> closingBrackets = new ArrayList<>();

    private DState createDState(SortedList<NDState> states, String statesName) throws Exception {
        int chNo = checkNo.getNextNumber();
        FinState<T> fstate = null;

        for (int i = 0, count = states.size(); i < count; i++) {
            NDState state = states.get(i);

            if (fstate == null) {
                closingBrackets.clear();

                if (state instanceof CloseBracketState) {
                    closingBrackets.add(((CloseBracketState) state).bracketInfo);
                }

                fstate = getFinState(state, chNo, closingBrackets);
            } else {
                FinState ambState = state.reachFinState(chNo);

                if (ambState != null) {
                    throw new AmbiguousRuleException(fstate.regexp, ambState.regexp);
                }
            }
        }

        return fstate == null
            ? new DState(states, statesName)
            : new DStateFin<>(states, statesName, fstate.regexp, fstate.init, fstate.matchToken, closingBrackets);
    }

    @SuppressWarnings("unchecked")
    private FinState<T> getFinState(NDState state, int checkNo, List<BracketInfo> closingBrackets) throws AmbiguousRuleException {
        FinState finstate = null;

        if (!state.yetChecked(checkNo)) { // avoid endless loops
            if (state instanceof FinState) {
                finstate = (FinState) state;

                for (Transition<NDState> t : state.getEmptyTransitions()) {
                    FinState ambState = t.st.reachFinState(checkNo);

                    if (ambState != null) {
                        throw new AmbiguousRuleException(finstate.regexp, ambState.regexp);
                    }
                }
            } else {
                for (Transition<NDState> t : state.getEmptyTransitions()) {
                    NDState ast = t.st;

                    if (finstate == null) {
                        if (ast instanceof CloseBracketState) {
                            closingBrackets.add(((CloseBracketState) ast).bracketInfo);

                            if ((finstate = getFinState(ast, checkNo, closingBrackets)) == null) {
                                closingBrackets.remove(closingBrackets.size() - 1);
                            }
                        } else {
                            finstate = getFinState(ast, checkNo, closingBrackets);
                        }
                    } else {
                        FinState ambState = ast.reachFinState(checkNo);
                        
                        if (ambState != null) {
                            throw new AmbiguousRuleException(finstate.regexp, ambState.regexp);
                        }
                    }
                }
            }
        }

        return finstate;
    }
}
