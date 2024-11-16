/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex.util;

/**
 *
 * @author Jörg
 * @param <T>
 */
public class Pair<T> {
    protected T value;
    protected Pair<T> next;
    
    public Pair(T value, Pair<T> next) {
        this.value = value;
        this.next = next;
    }
    public Pair(T value) {
        this(value, null);
    }
    
    public T car() {
        return value;
    }
    
    public Pair<T> cdr() {
        return next;
    }
    
    public void add(T value) {
        Pair<T> act = this;
        
        while (act.next != null) {
            act = act.next;
        }
        
        act.next = new Pair<>(value);
    } 
}
