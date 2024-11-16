/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex.util;

/**
 *
 * @author Jörg
 */
public interface ConsumerException<T> {
    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws java.lang.Exception
     */
    void accept(T t) throws Exception;
    
}
