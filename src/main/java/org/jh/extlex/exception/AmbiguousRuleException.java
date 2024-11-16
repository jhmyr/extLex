/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jh.extlex.exception;

/**
 *
 * @author Jörg
 */
public class AmbiguousRuleException extends Exception {
    private final String message;
    
    public AmbiguousRuleException(String regexp1, String regexp2) {
        message = "Ambiguous rules '" + regexp1 + "' and '" + regexp2 + "'!";
    }
    
    @Override
    public String getMessage() {
        return message;
    }
}
