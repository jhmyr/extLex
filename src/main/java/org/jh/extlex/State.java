/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex;

/**
 *
 * @author Jörg
 * @param <S>
 */
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
