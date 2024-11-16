/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex;

/**
 *
 * @author Jörg
 */
public class Logger {
    final static boolean DEBUG = false;
    
    final static void DEBUG(String msg) {
        if (DEBUG) {
            System.out.println(msg);
        } 
    }
}
