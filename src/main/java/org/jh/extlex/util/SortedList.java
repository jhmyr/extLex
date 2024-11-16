/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jh.extlex.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Jörg
 * @param <E> the hashCode of E must be ordered!
 */
public class SortedList<E extends SortKey> implements Iterable<E> {
    private final List<E> elems = new ArrayList<>();
  
    public SortedList() {
    }
    
    public SortedList(E entry) {
        elems.add(entry); 
    }
    
    public final void add(E entry) {
        int mid = 0;
        int low = 0;
        int size = elems.size();
        int high = size - 1;
        int key = entry.sortKey();
        
        while (low <= high) {
            mid = low + (high - low) /2;
            
            int ekey = elems.get(mid).sortKey();
            
            if (key < ekey) {
                high = mid - 1;
            } else {
                if (key > ekey) {
                    mid = mid + 1;
                    low = mid;
                } else {
                    return;
                } 
            }
        }

        elems.add(mid, entry);
    }
    
    public E find(int key) {
        int mid = 0;
        int low = 0;
        int high = elems.size() - 1;
        
        while (low <= high) {
            mid = low + (high - low) /2;
            
            E elem = elems.get(mid);
            int ekey = elem.sortKey();
            
            if (key < ekey) {
                high = mid - 1;
            } else {
                if (key > ekey) {
                    mid = mid + 1;
                    low = mid;
                } else {
                    return elem;
                } 
            }
        }
        
        return null;
    }
    
    public void clear() {
        elems.clear();
    }
    
    public boolean isEmpty() {
        return elems.isEmpty();
    }
    
    public int size() { return elems.size(); }
    
    public E get(int pos) { return elems.get(pos); }
    
    @Override
    public String toString() {
        StringBuilder sbuf = new StringBuilder();
        String space = "";
        
        for (E elem : elems) {
            sbuf.append(space).append(elem.sortKey());
            space = " ";
        }
        
        return sbuf.toString();        
    }    

    @Override
    public Iterator<E> iterator() {
        return elems.iterator();
    }
}
