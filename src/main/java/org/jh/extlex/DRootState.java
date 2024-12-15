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
import java.util.Map;
import java.util.Stack;

class DRootState {
    private final List<DState> rootStates;
    private final List<GroupInfo> groupInfoList;
    private final Stack<DState> stackOfState;
    private final Map<DState, List<String>> debugInfo;
    
    DRootState(List<DState> rootList, List<GroupInfo> groupInfoList,
        int maxGroupCount, Stack<DState> stackOfState, Map<DState, List<String>> debugInfo) {
        this.rootStates = rootList;
        this.groupInfoList = groupInfoList;
        this.stackOfState = stackOfState;
        this.debugInfo = debugInfo;
    }
    
    protected List<DState> getRootStates() {
        return rootStates;
    }
    protected DState getStartState() {
        return rootStates.get(0);
    }
    
    protected Map<DState, List<String>> getDebugInfo() {
        return debugInfo;
    }
    
    protected List<GroupInfo> getGroupInfoList() {
        return groupInfoList;
    }
    
    protected Stack<DState> getStackOfState() {
        return stackOfState;
    }

    final private DState internalCheck(String word) {
        DState act = getStartState(); 
        
        for (int i = 0, n = word.length(); i < n && act != null; i++) {
            char ch = word.charAt(i);
            
            act = act.find(ch);
            
            if (act == null && !stackOfState.empty()) {
                act = stackOfState.pop();
            }
        }
        
        stackOfState.clear();
        
        return act;
    }
    
    final protected boolean check(String word) {
        DState act = internalCheck(word); 
        
        return act != null && act.isFinalState();
    }
    
    final protected boolean subCheck(String word) {
        return internalCheck(word) != null;
    }
}
