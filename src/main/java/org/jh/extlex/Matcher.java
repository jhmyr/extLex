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

import java.io.IOException;
import org.jh.extlex.exception.UnknownTokenException;

public class Matcher<T> extends Scanner<T> {
    Matcher(DRootState root, TokenReader tr) {
        super(root, tr);
    }

    @SuppressWarnings("unchecked")
    public boolean hasNext() throws UnknownTokenException, IOException {
        if (tr.reachedEndOfReader()) {
            return false;
        }

        DState act = rootState;
        int ppos = tr.getPos();

        clear();

        do {
            for (int ch = tr.read(); ch != -1; ch = tr.read()) {
                DTransition<DState> trans = act.getTransition(ch);

                if (trans != null) {
                    act = trans.st;

                    trans.saveGroupPos(ppos);

                    if (act instanceof DStateFin) {
                        finState = (DStateFin<T>) act;

                        tr.mark();
                    }

                    ppos = tr.getPos();
                } else {
                    if (finState == null) {
                        ppos = tr.resetToTokenStart();

                        tr.read();
                        tr.accepted();
                    } else {
                        ppos = tr.reset();

                        if (stackState.isEmpty()) {
                            break;
                        }

                        finState.saveGroupPos(ppos);

                        finState = null;
                        act = stackState.pop();
                    }
                }
            }
            if (handleFinStates(tr.reset())) {
                return true;
            } else {
                ppos = tr.resetToTokenStart();

                tr.read();
                tr.accepted();
            }
        } while (!tr.reachedEndOfReader());

        return false;
    }
}
