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
import java.io.StringReader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author Jörg
 */
public class TokenReaderTest {
    StringBuilder sb;
    char ch;

    public TokenReaderTest() {
        sb = new StringBuilder();
    }
    
    void readToSB(TokenReader tr) throws IOException {
        while ((ch = tr.read()) != 0) {
            sb.append(ch);
        }        
    }

    void readNoCharToSB(TokenReader tr, int no) throws IOException {
        for (int i = 0; i < no; i++) {
            sb.append(tr.read());
        }        
    }

    @Test
    public void testRead() throws Exception {
        StringReader sr = new StringReader("abcdef");
        TokenReader tr = new TokenReader(sr, 8).init();
        
        readToSB(tr);
        
        assertEquals("abcdef", sb.toString());      
    }

    @Test
    public void testReadWithReducedBuffer() throws Exception {
        StringReader sr = new StringReader("abcdef");
        TokenReader tr = new TokenReader(sr, 4).init();
        
        readToSB(tr);
        
        assertEquals("abcdef", sb.toString());      
    }

    @Test
    public void testMarkAndReset() throws Exception {
        StringReader sr = new StringReader("abc_def");
        TokenReader tr = new TokenReader(sr, 8).init();
        
        while ((ch = tr.read()) != 0) {
            sb.append(ch);
            
            if (ch == '_') {
                tr.mark();
            }
        }
        
        tr.reset();
        
        readToSB(tr);
        
        assertEquals("abc_defdef", sb.toString());      
    }
    
    @Test
    public void testReadTwoTokens() throws Exception {
        StringReader sr = new StringReader("abc_def");
        TokenReader tr = new TokenReader(sr, 8).init();
        
        readNoCharToSB(tr, 4);
        
        tr.accepted();
        
        readToSB(tr);
        
        assertEquals("abc_def", sb.toString());
    }
    
    @Test
    public void testReadTwoTokensWithReducedBuffer() throws Exception {
        StringReader sr = new StringReader("abc_def");
        TokenReader tr = new TokenReader(sr, 4).init();
        
        readNoCharToSB(tr, 4);
        
        tr.accepted();
        
        readToSB(tr);
        
        assertEquals("abc_def", sb.toString());
        assertEquals(4, tr.getBufSize());        
    }

    @Test
    public void testReadTwoTokensWithReducedBufferAndLongToken() throws Exception {
        StringReader sr = new StringReader("abc_def");
        TokenReader tr = new TokenReader(sr, 3).init();
        
        readNoCharToSB(tr, 4);
        
        tr.accepted();
        
        readToSB(tr);
        
        assertEquals("abc_def", sb.toString());
        assertEquals(6, tr.getBufSize());        
    }

    @Test
    public void testGetPos() throws Exception {
        StringReader sr = new StringReader("abc_def");
        TokenReader tr = new TokenReader(sr, 3).init();
        
        readToSB(tr);
        
        assertEquals(7, tr.getPos());
    }

    @Test
    public void testGetPos2Token() throws Exception {
        StringReader sr = new StringReader("abc_def");
        TokenReader tr = new TokenReader(sr, 4).init();
        
        readNoCharToSB(tr, 4);
        tr.accepted();
        readToSB(tr);
        assertEquals("abc_def", sb.toString());
        assertEquals(4, tr.getBufSize());
        assertEquals(7, tr.getPos());
        assertEquals(0, tr.getOffset());
        assertEquals(4, tr.getDelta());
    }

    @Test
    public void testGetPos2Token3() throws Exception {
        StringReader sr = new StringReader("abc_def");
        TokenReader tr = new TokenReader(sr, 3).init();
        
        readNoCharToSB(tr, 4);
        tr.accepted();
        readToSB(tr);
        assertEquals("abc_def", sb.toString());
        assertEquals(6, tr.getBufSize());
        assertEquals(7, tr.getPos());
        assertEquals(0, tr.getOffset());
        assertEquals(4, tr.getDelta());
    }
    
    @Test
    public void test3Tokens() throws Exception {
        StringReader sr = new StringReader("ab,cd");
        TokenReader tr = new TokenReader(sr, 4).init();
        
        readNoCharToSB(tr, 2);
        tr.accepted();
        readNoCharToSB(tr, 1);
        tr.accepted();
        readNoCharToSB(tr, 2);
        assertEquals(0, tr.getOffset());
        assertEquals(5, tr.getPos());
        assertEquals(3, tr.getDelta());
    }
}
