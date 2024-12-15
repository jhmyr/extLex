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
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class TestExample {
    @Test
    public void test1() throws Exception {
        String token = new Lexer<String>()
            .addPattern("a+", (char[] text, int start, int length) -> new String(text, start, length))
            .scan("aa")
            .getNextToken();
        
        assertEquals("aa", token);
    }

    @Test
    public void test2() throws Exception {
        List<String> tokens = new ArrayList<>();

        Scanner<String> scanner = new Lexer<String>()
            .addPattern("[a-z]+", (char[] text, int start, int length) -> new String(text, start, length))
            .addPattern(",")
            .scan("ab,cd");
        
        while (scanner.hasNext()) {
            String t = scanner.getToken();
            
            if (t != null) {
                tokens.add(t);
            }
        }

        assertArrayEquals(new String[]{"ab", "cd"}, tokens.toArray());
    }
    
    @Test
    public void test2Simplified() throws Exception {
        List<String> tokens = new ArrayList<>();

        new Lexer<String>()
            .addPattern("[a-z]+", (char[] text, int start, int length) -> new String(text, start, length))
            .addPattern(",")
            .scan("ab,cd")
            .getAllTokens((t) -> tokens.add(t));

        assertArrayEquals(new String[]{"ab", "cd"}, tokens.toArray());
    }
    
    @Test
    public void test3() throws Exception {
        List<String> tokens = new ArrayList<>();
        String[] emptyStr = new String[0];
        
        String[] result = new Lexer<String[]>()
            .addPattern("([a-z]+){:,}",
                () -> tokens.clear(),
                (t,s,l) -> tokens.toArray(emptyStr),
                (text, start, length) -> tokens.add(new String(text, start, length)))
            .scan("ab,cd,ef")
            .getNextToken();
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, result);
    }
        
    class Counter {
        int hits;
        
        void inc() { hits++; }
        
        void init() { hits = 0; }
        
        Integer getHits() { return hits; }
    };
    
    @Test
    public void test4() throws Exception {
        Counter con = new Counter();
        
        Tokenizer<Integer> to = new Lexer<Integer>()
            .addPattern("(<(?R)>)*",
                con::init,
                (t, s, l) -> con.getHits(),
                (t, s, l) -> con.inc())
            .createTokenizer();
       
        assertEquals(1, to.scan("<>").getNextToken());
        assertEquals(2, to.scan("<><>").getNextToken());
        assertEquals(2, to.scan("<<>>").getNextToken());        
        assertEquals(3, to.scan("<<><>>").getNextToken());
        assertEquals(4, to.scan("<<><>><>").getNextToken());
    }
}
