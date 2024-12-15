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
