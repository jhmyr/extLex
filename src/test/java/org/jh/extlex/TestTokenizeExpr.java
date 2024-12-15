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
import java.util.List;
import java.util.function.BiConsumer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class TestTokenizeExpr {
    List<Object> output = new ArrayList<>();
    List<Operator> operator = new ArrayList<>();
    
    void init() {
        output.clear();
        operator.clear();
    }
    
    final void parseInt(char[] buffer, int start, int len) {
        double no = 0.0;
        int sign = 1;
        char ch = buffer[start];
        
        len += start++;
        
        switch(ch) {
            case '-' : sign = -1;
            case '+' : break;
            default: no = ch - '0'; 
        }
        
        while (start < len) {
            no = no * 10 + buffer[start++] - '0';
        }
    
        output.add(sign * no);
    }
    
    final void open(char[] buffer, int start, int len) {
        operator.add(Operator.LEFT);
    }
 
    final Operator getOp(char ch) throws Exception {
        switch (ch) {
            case '^': return Operator.EXP;
            case '*': return Operator.MUL;
            case '/': return Operator.DIV;
            case '+': return Operator.PLUS;
            case '-': return Operator.MINUS;
            default: throw new Exception("Unknown operator " + ch + "!");
        }
    }
    
    final void op(char[] buffer, int start, int len) throws Exception {
        Operator op1 = getOp(buffer[start]);
        Operator op2;
        int lastPos = operator.size()-1;
        
        while (lastPos >= 0 && (op2 = operator.get(lastPos)) != Operator.LEFT &&
            (op2.precedence > op1.precedence || (op2.precedence == op1.precedence && op1.isLeftAssociativ))) {
                operator.remove(lastPos--);
                output.add(op2);
        }
        
        operator.add(op1);
    }
 
    final void close(char[] buffer, int start, int len) {
        while (operator.get(operator.size()-1) != Operator.LEFT) {
            output.add(operator.remove(operator.size()-1));
        }
        
        operator.remove(operator.size()-1); // pop left bracket from operator;
    }
 
    void printOutputStack() {
        StringBuilder sbuf = new StringBuilder();
        String sep = "";
        
        for (int p = 0, s = output.size(); p < s; p++) {
            sbuf.append(sep).append(output.get(p).toString());
            sep = " ";
        }
        
        System.out.println(sbuf.toString());        
    }
    
    final Double getExpr(char[] buffer, int start, int len) {
        int size;
        
        while ((size = operator.size()) > 0) {
            output.add(operator.remove(size-1));
        }
        
        if (Logger.DEBUG) printOutputStack();
        
        for (int p = 0; p < output.size(); p++) {
            Object o = output.get(p);
            
            if (o instanceof Operator) {
                int osize = output.size();
                Operator op = (Operator)o;
                
                op.eval(output, p);
                
                p = p - osize + output.size();
            }
        }
        
        return (Double)output.get(0);
    }

    @Test
    void test() throws Exception {
        String re = "(?:([-+]?[0-9]+)|(\\()(?R)(\\))){:(-+*/^)}";
        Tokenizer<Double> m = new Lexer<Double>()
            .addPattern(re, this::init, this::getExpr, this::parseInt, this::open, this::close, this::op)
            .createTokenizer();
        
        assertEquals(1.0, m.scan("1").getNextToken());
        assertEquals(1.0, m.scan("(1)").getNextToken());
        assertEquals(3.0, m.scan("1+2").getNextToken());
        assertEquals(6.0, m.scan("1+2+3").getNextToken());
        assertEquals(7.0, m.scan("1+2*3").getNextToken());
        assertEquals(0.0, m.scan("5-3-2").getNextToken());
        assertEquals(14.0, m.scan("(5+3-1)*8/4").getNextToken());
        assertEquals(48.0, m.scan("3*2^2^2").getNextToken());
    }

    enum Operator {
        EXP(false, 4, "^", Operator::exp),
        MUL(true, 3, "*", Operator::mul),
        DIV(true, 3, "/", Operator::div),
        PLUS(true, 2, "+", Operator::plus),
        MINUS(true, 2, "-", Operator::minus),
        LEFT(true, 5, "(", null),
        RIGHT(true, 5, ")", null);
        
        public boolean isLeftAssociativ;
        public int precedence;
        String op;
        private final BiConsumer<List<Object>, Integer> opfunc;
        
        private Operator(boolean assocciativity, int precedence, String op, BiConsumer<List<Object>, Integer> opfunc) {
            this.isLeftAssociativ = assocciativity;
            this.op = op;
            this.precedence = precedence;
            this.opfunc = opfunc;
        }
        
        void eval(List<Object> output, int pos) {
            opfunc.accept(output, pos);
        }

        static void exp(List<Object> out, int pos) {
            out.set(pos - 2, Math.pow((double)out.remove(pos - 2), (double)out.remove(pos - 2)));
        }

        static void mul(List<Object> out, int pos) {
            out.set(pos - 2, (double)out.remove(pos - 2) * (double)out.remove(pos - 2));
        }

        static void div(List<Object> out, int pos) {
            out.set(pos - 2, (double)out.remove(pos - 2) / (double)out.remove(pos - 2));
        }

        static void plus(List<Object> out, int pos) {
            out.set(pos - 2, (double)out.remove(pos - 2) + (double)out.remove(pos - 2));
        }

        static void minus(List<Object> out, int pos) {
            out.set(pos - 2, (double)out.remove(pos - 2) - (double)out.remove(pos - 2));
        }
        
        @Override
        final public String toString() {
            return op;
        }
    }
}