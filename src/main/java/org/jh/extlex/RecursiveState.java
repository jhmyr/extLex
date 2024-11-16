/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex;

import java.util.List;

/**
 *
 * @author Jörg
 */
class RecursiveState extends NDState {
    private final NDState internState;
    
    RecursiveState(List<Transition<NDState>> acttrans) throws Exception {      
        this.internState = new NDState(acttrans);
    }
    
    final protected NDState getInternState() {
        return internState;
    }
    
    @Override
    public String toString() {
        return "{" + stateNo + "}";
    }
}
