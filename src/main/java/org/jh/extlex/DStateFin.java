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

import java.util.List;
import java.util.function.Consumer;
import org.jh.extlex.util.Initializer;
import org.jh.extlex.util.SortedList;

@SuppressWarnings("unchecked")
public class DStateFin<T> extends DState {
    String regexp;
    Initializer init;
    TokenMeth<T> matchToken;
    static final Consumer<Integer>[] DEFAULT_GROUP = new Consumer[0];

    private Consumer<Integer>[] groups = DEFAULT_GROUP;
    
    DStateFin(SortedList<NDState> states, String stateName, String regexp, Initializer init, TokenMeth<T> matchToken, List<BracketInfo> closingBrackets) {
        super(states, stateName);
        
        this.regexp = regexp;
        this.init = init;
        this.matchToken = matchToken;
        
        if (!closingBrackets.isEmpty()) {
            int pos = 0;
            
            groups = new Consumer[closingBrackets.size()];
            
            for (BracketInfo bracketInfo : closingBrackets) {
                groups[pos++] = bracketInfo::saveEndPos;
            }
        }
    }
    
    @Override
    public final boolean isFinalState() { return true; }

    @Override
    final public int hashCode() {
        return super.hashCode();
    }

    @Override
    final public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    @Override
    final public int compareTo(DState d) {
        return super.compareTo(d);
    }
    
    final public void saveGroupPos(int pos) {
        for (Consumer<Integer> group : groups) {
            group.accept(pos);
        }
    }
}