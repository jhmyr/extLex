/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex.util;

/**
 *
 * @author Jörg
 */

public class CharRange implements Range {
    public int lowChar;
    public int highChar;
    
    public CharRange() {
        lowChar = highChar = java.lang.Integer.MAX_VALUE;
    }
    
    public CharRange(int lowChar, int highChar) {
        this.lowChar = lowChar;
        this.highChar = highChar;
    }
    
    @Override
    public int lowKey() {
        return lowChar;
    }

    @Override
    public int highKey() {
        return highChar;
    } 
    
    @Override
    public String toString() {
        if (lowChar == highChar) {
            return Character.toString((char)lowChar);
        } else {
            return Character.toString((char)lowChar) + ".." + Character.toString((char)highChar);
        }
    }
}