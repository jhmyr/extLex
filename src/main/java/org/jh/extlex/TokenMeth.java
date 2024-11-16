/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex;

/**
 *
 * @author Jörg
 * @param <T>
 */
public interface TokenMeth<T> {
    public T apply(char[] buffer, int start, int len) throws Exception;
}
