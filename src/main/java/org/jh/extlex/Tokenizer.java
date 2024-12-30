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
import java.io.StringReader;

public class Tokenizer<T> {
    private final DRootState root;

    Tokenizer(DRootState dRoot) {
        this.root = dRoot;
    }

    public Matcher<T> match(TokenReader tr) throws Exception {
        return new Matcher<>(root, tr.init());
    }
    
    public Matcher<T> match(Reader sr) throws Exception {
        TokenReader tr = new TokenReader(sr, 256);
        
        return match(tr);
    }

    public Matcher<T> match(String input) throws Exception {
        return match(new StringReader(input));
    }    

    public Scanner<T> scan(TokenReader tr) throws Exception {
        return new Scanner<>(root, tr.init());
    }
    
    public Scanner<T> scan(Reader sr) throws Exception {
        TokenReader tr = new TokenReader(sr, 256);
        
        return scan(tr);
    }

    public Scanner<T> scan(String input) throws Exception {
        return scan(new StringReader(input));
    }    
}
