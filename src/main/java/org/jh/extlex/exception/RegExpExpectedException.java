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
public class RegExpExpectedException extends Exception {
    public RegExpExpectedException(int pos, String regexp, String message) {
        super(regexp.substring(0, pos-1) + "#" + regexp.substring(pos-1) + " : " + message);
    }
}
