/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jh.extlex;

import org.jh.extlex.util.Initializer;

/**
 *
 * @author Jörg
 */
public class FinState<T> extends NDState {
    String regexp;
    Initializer init;
    TokenMeth<T> matchToken;
    
    public FinState(String regexp, Initializer init, TokenMeth<T> matchToken) {
        this.regexp = regexp;
        this.init = init;
        this.matchToken = matchToken;
    }
    
    @Override public final boolean isFinalState() { return true; }
}
