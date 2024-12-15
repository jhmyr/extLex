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