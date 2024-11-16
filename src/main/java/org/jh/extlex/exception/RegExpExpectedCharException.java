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
public class RegExpExpectedCharException extends Exception {
    public RegExpExpectedCharException(char ch, int pos) {
        super("Expect '" + ch + "' at position " + pos + "!");
    }
}
