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

public abstract class State<S extends State> {
    int checkNo;

    protected final boolean yetChecked(int checkNo) {
        if (this.checkNo == checkNo) return true;
        
        this.checkNo = checkNo;
        
        return false;
    }
    
    final protected int getCheckNo() { return checkNo; }

    protected abstract boolean hasTransition();
    
    protected abstract Iterable<Transition<S>> getTransitions();
}
