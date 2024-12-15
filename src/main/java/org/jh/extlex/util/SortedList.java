/*
 * Copyright 2024 jhmyr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jh.extlex.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
