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

public class RangeSet<E extends Range> implements Iterable<E> {
     List<E> elems = new ArrayList<>();
     
    public E add(E entry) {
        int mid = 0;
        int low = 0;
        int size = elems.size();
        int high = size - 1;
        
        while (low <= high) {
            mid = low + (high - low) /2;
            
            E key = elems.get(mid);
            
            if (entry.highKey() < key.lowKey()) {
                high = mid - 1;
            } else {
                if (entry.lowKey() > key.highKey()) {
                    low = mid = mid + 1;
                } else {
                    return key;
                } 
            }
        }

        elems.add(mid, entry);
        
        return null;
    }
    
    public void append(E entry) {
        elems.add(entry);
    }
    
    public E find(int key) {
        int mid = 0;
        int low = 0;
        int size = elems.size();
        int high = size - 1;
        
        while (low <= high) {
            mid = low + (high - low) /2;
            
            E entry = elems.get(mid);
            
            if (key < entry.lowKey()) {
                high = mid - 1;
            } else {
                if (key > entry.highKey()) {
                    mid = mid + 1;
                    low = mid;
                } else {
                    return entry;
                } 
            }
        }

        return null;
    }
    
    public E find(Range key) {
        int mid = 0;
        int low = 0;
        int size = elems.size();
        int high = size - 1;
        
        while (low <= high) {
            mid = low + (high - low) /2;
            
            E entry = elems.get(mid);
            
            if (key.highKey() < entry.lowKey()) {
                high = mid - 1;
            } else {
                if (key.lowKey() > entry.highKey()) {
                    mid = mid + 1;
                    low = mid;
                } else {
                    return entry;
                } 
            }
        }

        return null;        
    } 
     
     @Override
    public String toString() {
        StringBuilder sBuf = new StringBuilder();
        String sep = "";
        
        for (E elem : this) {
            if (elem.lowKey()==elem.highKey()) {
                sBuf.append(sep).append((char)elem.lowKey());
            } else {
                sBuf.append(sep).append((char)elem.lowKey()).append('-').append((char)elem.highKey());
            }
            sep=";";
        } 
            
        return sBuf.toString();
    }
    
    public void clear() {
        elems.clear();
    }
    
    @Override
    public Iterator<E> iterator() {
        return elems.iterator();
    }

    public boolean isEmpty() {
        return elems.isEmpty();
    }
    
    public int size() {
        return elems.size();
    }
    
    public E get(int pos) {
        return elems.get(pos);
    }
}
