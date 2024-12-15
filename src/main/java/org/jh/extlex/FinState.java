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
package org.jh.extlex;

import org.jh.extlex.util.Initializer;

public class FinState<T> extends NDState {
    String regexp;
    Initializer init;
    TokenMeth<T> matchToken;
    
    public FinState(String regexp, Initializer init, TokenMeth<T> matchToken) {
        this.regexp = regexp;
        this.init = init;
        this.matchToken = matchToken;
    }
    
    @Override public final boolean isFinalState() { return true; }
}
