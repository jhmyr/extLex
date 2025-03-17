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

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static java.lang.Math.max;
import java.util.Stack;
import java.util.TreeMap;
import org.jh.extlex.exception.RegExpExpectedCharException;
import org.jh.extlex.exception.RegExpExpectedException;
import org.jh.extlex.exception.RegExpNotAllowedCharException;
import org.jh.extlex.util.Initializer;

public final class Lexer<T> {
    private final SingleNumber stateNo = new SingleNumber();
    private NDState root = null;
    private boolean isSecond = false;
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
    private NDState[] bracketList = new NDState[1];
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
    * a:   (1)-a->[2]
    * ab:  (1)-a->(2)-b->[3]
    * a+:  (1)-a->(2)-->(1)
                  (2)-->[3]
    * a*:  (1)-a->(1)
    *      (1)-->[2]
    * a?:  (1)-a°->[2]
    * a|b: (1)-a..b->[2]
    * (a): (1-->(1)-a->1)-->[2]
    * a(?R): (1)-a->{2}-->[4]
    *               {2}-->(1)
    *        (3)-->{2}
    * (a)+: (1)-->(1-->(2)-a->1)-->(3)-->(1
    *                  (3)-->[4]

    */
    public Lexer() {
    }

    protected List<NDState> getRootStates() {
        return roots;
    }

    public Lexer<T> addPattern(String regexp, Initializer init, TokenMeth<T> matchToken, GroupMeth ... matchGroups) throws Exception {
        if (matchGroups.length + 1 > bracketList.length) {
            bracketList = new NDState[matchGroups.length + 1];
        }
        
        NDState node = bracketList[0] = parse(regexp, init, matchToken, matchGroups);

        if (root == null) {
            root = node;
            isSecond = true;
            
            roots.add(root);
        } else {
            if (isSecond) {
                root = new NDState(stateNo.getNextNumber(), root);
                roots.set(0, root);
                isSecond = false;
            } 
            
            root.addEmptyTransition(node);
        }

        for (RecursiveState recState : recStateList) {
            recState.addEmptyBackwardTransition(node);
            
            if (node.findEmptyTransitionToFinState(checkNo.getNextNumber())) {
                recState.addEmptyTransition(recState.getInternState());
            }
            
            roots.add(recState.getInternState());
        }

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
    
    DRootState createDFA() throws Exception {
        DFABuilder<T> dfaBuilder = new DFABuilder<>(checkNo, stateNo, groupInfoList, maxGroupCount);
        
        return dfaBuilder.createDFA(root);        
    }
    
    public Tokenizer<T> createTokenizer() throws Exception {
        return new Tokenizer<>(createDFA());
    }
    
    public Matcher<T> match(String input) throws Exception {
        return createTokenizer().match(input);
    }
    
    public Matcher<T> match(Reader input) throws Exception {
        return createTokenizer().match(input);
    }
    
    public Matcher<T> match(TokenReader input) throws Exception {
        return createTokenizer().match(input);
    }
    
    public Scanner<T> scan(String input) throws Exception {
        return createTokenizer().scan(input);
    }
    
    public Scanner<T> scan(Reader input) throws Exception {
        return createTokenizer().scan(input);
    }
    
    public Scanner<T> scan(TokenReader input) throws Exception {
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
                if (lookahead(0) == ')') {
                    actChar = getChar();
                    e.addEmptyTransition(newTrans);
                } else {
                    e.addEmptyTransition(parseFragment(newTrans));
                }
                trans.addAll(newTrans);
            } while (actChar == '|');
        }

        return e;
    }

    NDState parseGroupExpr(List<Transition<NDState>> trans) throws Exception {
        int actBracketNo = bracketNo;
        int actGroupPos = groupPos + 1;
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
            state = new OpenBracketState(actBracketNo, state, brinfo);
            bracketList[actGroupPos] = state;

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

    NDState parseRecursiveExpr(List<Transition<NDState>> acttrans, int recGroupNo) throws Exception {
        RecursiveState state = new RecursiveState(acttrans, recGroupNo);

        recStateList.add(state);

        return state;
    }

    NDState parseSubExpr(List<Transition<NDState>> acttrans) throws Exception {
        switch (actChar) {
            case '(':
                if (lookahead(0) == '?' && lookahead(1) == 'R') {
                    getChar();
                    getChar();

                    int rNo = 0;
                    while (Character.isDigit(actChar = getChar())) {
                        rNo = rNo * 10 + actChar - '0';
                    }
                    
                    if (actChar != ')') {
                        throwExpected("excpected '[0-9]*'!");
                    }
                    
                    return parseRecursiveExpr(acttrans, rNo);
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
                BracketInfo brinfo = new BracketInfo(actregexp, actGroupMeths[groupPos], groupInfoList);

                forward = true;
                quantState = new OpenBracketState(bracketNo, brinfo);               
                bracketList[groupPos++] = quantState;

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
}
