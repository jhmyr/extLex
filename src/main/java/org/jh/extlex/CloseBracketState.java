/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jh.extlex;

/**
 *
 * @author Jörg
 */
public class CloseBracketState extends OpenBracketState {
    
    CloseBracketState(int bracketNo, BracketInfo brinfo) {
        super(bracketNo, brinfo);
    }
 
    @Override 
    public String toString() {
        return bracketNo + ")";
    }
}
