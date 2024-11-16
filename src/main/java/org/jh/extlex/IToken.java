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
public interface IToken {
    public void matchToken(char[] text, int count, int[] start, int[] len) throws Exception;
}
