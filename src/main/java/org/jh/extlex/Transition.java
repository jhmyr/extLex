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

import java.util.Objects;
import org.jh.extlex.util.CharRange;

public class Transition<S> extends CharRange {
    
    protected S st = null;
    private boolean forward = true;

    Transition() {
        super();
    }
    
    Transition(char ch) {
        super(ch, ch);
    }
    
    Transition(char ch, boolean forward, S next) {
        super(ch, ch);
        
        this.forward = forward;
        this.st = next;
    }
    
    Transition(int lch, int hch) {
        super(lch, hch);
    }
    
    Transition(CharRange range, S state) {
        super((char)range.lowChar, (char)range.highChar);
        
        this.st = state;
    }
    
    Transition(S state) {
        super();
        this.st = state;
    }
    
    Transition(S state, boolean goRight) {
        super();
        st = state;
        forward = goRight;
    }
    
    public boolean isEmptyTransaction() {
        return lowChar == Integer.MAX_VALUE;
    }
    
    public boolean isCharTransaction() {
        return lowChar != Integer.MAX_VALUE;
    }
    
    public boolean isForwardTransition() {
        return forward;
    }

    public S getTargetState() {
        return st;
    }
    
    @Override
    public int hashCode() {
        return lowChar;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Transition<?> other = (Transition<?>) obj;
        return Objects.equals(this.st, other.st);
    }
    
    // Precondition cr >= this.
    public boolean couldBeExtendedBy(CharRange cr) {
        if (highChar + 1 == cr.lowChar) {
            highChar = cr.highChar;
            return true;
        } else {
            return false;
        }
    }
}
