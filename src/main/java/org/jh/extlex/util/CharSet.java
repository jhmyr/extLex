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
package org.jh.extlex.util;

public class CharSet extends RangeSet<CharRange> {
    @Override
    public CharRange add(CharRange nentry) {
        CharRange entry = super.add(nentry);
        
        if (entry != null) { // an entry exists
            char nl = (char)nentry.lowChar;
            char nh = (char)nentry.highChar;
            char el = (char)entry.lowChar;
            char eh = (char)entry.highChar;

            if (nl != el || nh != eh) {
                if (nl < el) {
                    add(new CharRange(nl,(char) (el-1)));
                
                    return add(new CharRange(el, nh));
                }
                if (nl == el) {
                    if (nh < eh) {
                        entry.highChar = nh;
                    
                        return add(new CharRange((char)(nh+1), eh));
                    } else { // nh > eh
                        return add(new CharRange((char)(eh+1), nh));
                    }
                }
                // nl > el
                entry.lowChar = nl;
                add(new CharRange(el, (char)(nl-1)));
            
                return add(nentry);
            } 
        }
        
        return null;
    }
    
    public CharRange add(char ch) {
        return add(new CharRange(ch, ch));
    }
    
    public CharRange add(char lch, char hch) {
        return add(new CharRange(lch, hch));
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String space = "";
        
        for (CharRange cr : this) {
            sb.append(space).append((char)cr.lowChar).append('-').append((char)cr.highChar);
            space = ",";
        }
        
        return sb.toString();
    }
}    
