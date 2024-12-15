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
import org.jh.extlex.exception.AmbiguousRuleException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static java.lang.Math.max;
import java.util.Stack;
import java.util.TreeMap;
import java.util.function.Consumer;
import static org.jh.extlex.Logger.DEBUG;
import org.jh.extlex.exception.RegExpExpectedCharException;
import org.jh.extlex.exception.RegExpExpectedException;
import org.jh.extlex.exception.RegExpNotAllowedCharException;
import org.jh.extlex.util.CharRange;
import org.jh.extlex.util.CharSet;
import org.jh.extlex.util.Initializer;

public final class Lexer<T> {
    private final SingleNumber stateNo = new SingleNumber();
    private final NDState root = new NDState(stateNo.getNextNumber());
    private final List<NDState> roots = new ArrayList<>();
    private String actregexp = null;
    private int actregexplen = 0;
    private int actregexppos = 0;
    private final SingleNumber checkNo = new SingleNumber();
    private int bracketStart = 0;
    private int bracketNo = 1;
    private int actGroupCount;
    private int maxGroupCount;
    private int groupPos;
    private char actChar;
    private GroupMeth[] actGroupMeths;
    private final List<GroupInfo> groupInfoList = new ArrayList<>();
    private final List<NDState> openBracketList = new ArrayList<>();
    private final List<RecursiveState> recStateList = new ArrayList<>();
    private Stack<DState> stackOfState;
    private final Map<DState, List<String>> debugInfoDState = new TreeMap<>();

    /*
    * A regular expression has the form:
    * e -> e*
    * e -> e+
    * e -> e?
    * e -> (e)
    * e -> (?:e)
    * e -> e1e2
    * e -> e1|e2
    * The non deterministic state diagrams looks as follow:
    * a:   (1) -a-> [2]
    * ab:  (1) -a-> (2) -b-> [3]
    * a+:  (1) -a-> (2) ---> (1)
    *               (2) ---> [3]
    * a*:  (1) ---> [2]
    *      (1) -a-> (3) --> (1)
    * a?:  (1) -a-> [2]
    *      (1) ---> [2]
    * a|b :(1) ---> (2) -a-> (4) ---> [5]
    *     :(1) ---> (3) -b-> (4)
    * (a): (1  ---> (1) -a-> (2) ---> 1) ---> [3]
    * (a+):(1  ---> (1) -a-> (2) ---> 1) ---> [3]
    *                        (2) ---> (1)
    * (a*):(1  ---> (1) -a-> 1) ---> [2]
    *               (1) ---> 1)
    * (a?):(1  ---> (1) -a°-> )1 ---> [2]
    * (a)+:(1  ---> (1) -a->  )1 ---> (2) ---> [3]
    *                                 (2) ---> (1          
     */
    public Lexer() {
        roots.add(root);
    }

    protected List<NDState> getRootStates() {
        return roots;
    }

    public Lexer<T> addPattern(String regexp, Initializer init, TokenMeth<T> matchToken, GroupMeth ... matchGroups) throws Exception {
        NDState node = parse(regexp, init, matchToken, matchGroups);

        root.addEmptyTransition(node);

        for (RecursiveState recState : recStateList) {
            recState.addEmptyBackwardTransition(node);
            
            if (node.findEmptyTransitionToFinState(bracketNo)) {
                recState.addEmptyTransition(recState.getInternState());
            }
            
            roots.add(recState.getInternState());
        }

        openBracketList.clear();
        recStateList.clear();

        return this;
    }
    
    private void nada() {       
    }
    
    public Lexer<T> addPattern(String regexp, TokenMeth<T> matchToken, GroupMeth... matchGroups) throws Exception {
        return addPattern(regexp, this::nada, matchToken, matchGroups);
    }
    
    public Lexer<T> addPattern(String regexp, GroupMeth... matchGroups) throws Exception {
        return addPattern(regexp, this::nada, null, matchGroups);
    }
    
    public Lexer<T> addPattern(String regexp) throws Exception {
        return addPattern(regexp, this::nada, null);
    }
    
    public Tokenizer<T> createTokenizer() throws Exception {
        DRootState dRoot = createDFA();

        return new Tokenizer<>(dRoot);
    }
    
    public Scanner<T> scan(String input) throws Exception {
        return createTokenizer().scan(input);
    }
    
    public int getMaxGroupCount() {
        return maxGroupCount;
    }

    void setNextState(List<Transition<NDState>> trans, NDState nstate) {
        for (Transition<NDState> t : trans) {
            t.st = nstate;
        }

        trans.clear();
    }

    private char getChar() {
        return actregexppos < actregexplen ? actregexp.charAt(actregexppos++) : 0;
    }

    protected SingleNumber getCheckNo() {
        return checkNo;
    }
    
    protected SingleNumber getStateNo() {
        return stateNo;
    }
    
    private char lookahead(int pos) {
        return actregexppos + pos < actregexplen ? actregexp.charAt(actregexppos + pos) : 0;
    }

    private NDState parse(String regexp, Initializer init, TokenMeth<T> matchToken, GroupMeth... groupMeths) throws Exception {
        try {
            actregexp = regexp;
            actregexplen = regexp.length();
            actregexppos = 0;
            actGroupMeths = groupMeths;
            groupPos = 0;
            bracketStart = bracketNo;

            // ToDo: Think about to use for tlist an ArrayList and add only
            // on this list all entries in the sub calls. Remember than the
            // position by an index and add only all element after this index!
            // This would enormously reduce the memory amount
            List<Transition<NDState>> tlist = new ArrayList<>();
            NDState node = parseOrFragment(tlist);

            if (actChar != 0) {
                throwNotAllowedChar(actChar);
            }

            actGroupCount = bracketNo - bracketStart;
            maxGroupCount = max(maxGroupCount, actGroupCount);

            setNextState(tlist, new FinState<>(actregexp, init, matchToken));

            return node;
        } catch (DoubleEntryException dee) {
            System.err.println(regexp);

            throw dee;
        }
    }

    private void throwNotAllowedChar(char ch) throws Exception {
        throw new RegExpNotAllowedCharException(ch, actregexppos);
    }

    private void throwExpectedChar(char ch) throws Exception {
        throw new RegExpExpectedCharException(ch, actregexppos);
    }

    private void throwExpected(String message) throws Exception {
        throw new RegExpExpectedException(actregexppos, actregexp, message);
    }

    private NDState parseOrFragment(List<Transition<NDState>> trans) throws Exception {
        NDState e = parseFragment(trans);

        if (actChar == '|') {
            e = new NDState(e);

            List<Transition<NDState>> newTrans = new ArrayList<>();

            do {
                e.addEmptyTransition(parseFragment(newTrans));
                trans.addAll(newTrans);
            } while (actChar == '|');
        }

        return e;
    }

    NDState parseGroupExpr(List<Transition<NDState>> trans) throws Exception {
        int actBracketNo = bracketNo;
        BracketInfo brinfo = null;

        if (lookahead(0) == '?' && lookahead(1) == ':') {
            getChar();
            actChar = getChar();
        } else {
            bracketNo++;

            if (actGroupMeths != null && groupPos < actGroupMeths.length) {
                brinfo = new BracketInfo(actregexp, actGroupMeths[groupPos++], groupInfoList);
            } 
        }

        NDState state = parseOrFragment(trans);

        if (actChar != ')') {
            throwExpectedChar(')');
        }

        if (brinfo != null) {
            openBracketList.add(state = new OpenBracketState(actBracketNo, state, brinfo));

            NDState closeBracket = new CloseBracketState(actBracketNo, brinfo);

            setNextState(trans, closeBracket);
            closeBracket.addEmptyTransition(trans);
        }

        return state;
    }

    NDState parseChoiceExpr(List<Transition<NDState>> trans) throws Exception {
        NDState state = new NDState();
        char prevChar = 0;
        boolean detectRangeSign = false;
        boolean invert = false;

        if (lookahead(0) == '^') {
            invert = true;
            getChar();
        }
        while ((actChar = getChar()) != 0 && actChar != ']') {
            if (actChar == '\\') {
                actChar = getQuotedChar(getChar());
            }
            if (prevChar == 0) {
                prevChar = actChar;
            } else {
                if (detectRangeSign) {
                    if (prevChar > actChar) {
                        state.addTransition(actChar, prevChar, trans);
                    } else {
                        state.addTransition(prevChar, actChar, trans);
                    }

                    prevChar = 0;
                    detectRangeSign = false;
                } else {
                    if (actChar == '-') {
                        detectRangeSign = true;
                    } else {
                        state.addTransition(prevChar, trans);
                        prevChar = actChar;
                    }
                }
            }
        }

        if (actChar != ']') {
            throwExpectedChar('[');
        }

        if (prevChar != 0) {
            state.addTransition(prevChar, trans);
        }

        if (detectRangeSign) {
            state.addTransition('-', trans);
        }
        
        if (invert) {
            state.invertTransSet(trans);
        }

        return state;
    }

    char getQuotedChar(char ch) throws Exception {
        switch (ch) {
            case '\\':
            case '+':
            case '(':
            case ')':
            case '[':
            case ']':
            case '{':
            case '}':
            case '*':
            case '\'':
            case '\"':
            case '?':
            case '|':
            case '.':
                return ch;
            case 't':
                return '\t';
            case 'b':
                return '\b';
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 'f':
                return '\f';
        }
        throw new Exception("Illegal escape character '" + ch + "'!");
    }

    NDState parseRecursiveExpr(List<Transition<NDState>> acttrans) throws Exception {
        RecursiveState state = new RecursiveState(acttrans);

        recStateList.add(state);

        return state;
    }

    NDState parseSubExpr(List<Transition<NDState>> acttrans) throws Exception {
        switch (actChar) {
            case '(':
                if (lookahead(0) == '?' && lookahead(1) == 'R' && lookahead(2) == ')') {
                    getChar();
                    getChar();
                    getChar();
                    
                    return parseRecursiveExpr(acttrans);
                } else {
                    return parseGroupExpr(acttrans);
                }
             case '[':
                return parseChoiceExpr(acttrans);
            case '.':
                return new NDState().addTransition(0, Character.MAX_CODE_POINT, acttrans);
            case '\\':
                actChar = getQuotedChar(getChar());
                break;
            default:
        }
        
        return new NDState().addTransition(actChar, acttrans);
    }

    private void parsePlus(List<Transition<NDState>> acttrans, NDState state) {
        NDState nextState = new NDState();

        setNextState(acttrans, nextState);

        nextState.addEmptyBackwardTransition(state);
        nextState.addEmptyTransition(acttrans);

        actChar = getChar();
    }

    private void parseStar(List<Transition<NDState>> acttrans, NDState state) {
        setNextState(acttrans, state);
        state.addEmptyTransition(acttrans);

        actChar = getChar();
    }

    private void parseOpenCurlyBracket(List<Transition<NDState>> acttrans, NDState state) throws Exception {
        if ((actChar = getChar()) != ':') {
            throwExpected("excpected ':'!");
        }            

        NDState quantState;
        boolean forward = false;
        char closeChar = '}';
        
        if ((actChar = getChar()) == '(') {
            actChar = getChar();
            if (actGroupMeths != null && groupPos < actGroupMeths.length) {
                BracketInfo brinfo = new BracketInfo(actregexp, actGroupMeths[groupPos++], groupInfoList);

                forward = true;

                openBracketList.add(quantState = new OpenBracketState(bracketNo, brinfo));

                NDState closeBracket = new CloseBracketState(bracketNo, brinfo);

                closeBracket.addEmptyBackwardTransition(state);

                state = closeBracket;
            } else {
                quantState = new NDState();
            }

            closeChar = ')';
        } else {
            quantState = new NDState();
        }

        setNextState(acttrans, quantState);
        quantState.addEmptyTransition(acttrans);

        do {
            // ToDo: take care for quoted chars
            quantState.addTransition(actChar, forward, state);

            actChar = getChar();
        } while (actChar != 0 && actChar != closeChar);

        if (closeChar == ')') {
            actChar = getChar();
        }
        if (actChar != '}') {
            throwExpectedChar('}');
        }
    }

    NDState parseExpr(List<Transition<NDState>> trans) throws Exception {
        NDState state;
        List<Transition<NDState>> acttrans = new ArrayList<>();

        if (actChar == '+' || actChar == '*' || actChar == '|' || actChar == '?' || actChar == ')' || actChar == '{' || actChar == '}') {
            throwNotAllowedChar(actChar);
        }

        state = parseSubExpr(acttrans);
        actChar = getChar();

        switch (actChar) {
            case '+':
                parsePlus(acttrans, state);
                break;
            case '*':
                parseStar(acttrans, state);
                break;
            case '{':
                parseOpenCurlyBracket(acttrans, state);

                if ((actChar = getChar()) != '?') {
                    break;
                }
            case '?':
                state.addEmptyTransition(acttrans);

                actChar = getChar();
                break;
            default:
        }

        setNextState(trans, state);

        trans.addAll(acttrans);

        return state;
    }

    NDState parseFragment(List<Transition<NDState>> acttrans) throws Exception {
        actChar = getChar();

        NDState firstState = parseExpr(acttrans);

        while (actChar != 0 && actChar != '|' && actChar != ')') {
            parseExpr(acttrans);
        }

        return firstState;
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

    protected DRootState createDFA() throws Exception {
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

            if (!(state instanceof RecursiveState)) {            
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
