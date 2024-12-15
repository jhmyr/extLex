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
import org.jh.extlex.util.CharRange;

@SuppressWarnings("unchecked")
public class DTransition<S> extends Transition<S> {
    static final Consumer<Integer>[] DEFAULT_GROUP = new Consumer[0];

    Consumer<Integer>[] groups;
    
    public DTransition(CharRange range, S nextDState, List<Consumer<Integer>> groups) {
        super(range, nextDState);
        
        this.groups = groups.isEmpty()? DEFAULT_GROUP : toArray(groups);
    }
    
    final Consumer<Integer>[] toArray(List<Consumer<Integer>> list) {
        Consumer<Integer>[] result = (Consumer<Integer>[])new Consumer[list.size()];
        int i = 0;
        
        for (Consumer<Integer> cons : list) {
            result[i++] = cons;
        }
        
        return result;
    }
    
    @Override
    final public int hashCode() {
        return lowChar;
    }

    public boolean hasGroups() {
        return groups.length > 0;
    }
    
    @Override
    final public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    public void saveGroupPos(int pos) {
        for (Consumer<Integer> cons : groups) {
            cons.accept(pos);
        }
    }
}
