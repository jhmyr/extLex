/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex;

/**
 *
 * @author Jörg
 */
class MutableRecursiveState {
    private RecursiveState internalState = null;
    
    final protected void clear() {
        internalState = null;
    }
    
    final protected boolean exists() {
        return internalState != null;
    }
    
    final protected void setState(RecursiveState recState) throws Exception {
        if (internalState != null) {
            throw new Exception("No two recursive states allowed!");
        }
        internalState = recState;
    }
    
    final protected RecursiveState getState() {
        return internalState;
    }
}
