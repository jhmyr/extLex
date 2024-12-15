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
import java.util.List;
import java.util.function.Consumer;
import org.jh.extlex.util.CharRange;

public class OpenBracketState extends NDState {
    int bracketNo;
    BracketInfo bracketInfo;
    
    OpenBracketState(int bracketNo, NDState next, BracketInfo brinfo) {
        this(bracketNo, brinfo);
        
        addEmptyTransition(next);
    }
 
    OpenBracketState(int bracketNo, BracketInfo brinfo) {
        this.bracketNo = bracketNo;
        this.bracketInfo = brinfo;
    }
    
    @Override 
    public String toString() {
        return "(" + bracketNo;
    }    

    @Override
    final protected boolean getNextStates(SortedList<NDState> nextStates, SingleNumber stateNo, int checkNo, 
        CharRange range, List<Consumer<Integer>> groupSavers, MutableRecursiveState recursivState) throws Exception {
        if (super.getNextStates(nextStates, stateNo, checkNo, range, groupSavers, recursivState)) {
            groupSavers.add(this instanceof CloseBracketState ? bracketInfo::saveEndPos : bracketInfo::saveStartPos);
            
            return true;
        }
        
        return false;
    }

    final public int getBracketNo() { return bracketNo; }
}
