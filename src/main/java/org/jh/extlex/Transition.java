package org.jh.extlex;

import java.util.Objects;
import org.jh.extlex.util.CharRange;

/**
 * One State may have several Transitions
 * @author Jörg
 */
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
