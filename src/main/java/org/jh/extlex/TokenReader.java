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
import java.io.Reader;
/**
 * a reader for scanning tokens from an input stream. 
 * The reader provide enough space for keeping one token in the buffer.
 * The buffer will be extended if more space is needed.
 * After one token has been read the accepted command must be called for 
 * aligning all internal informations.
 * The reader provides information of the current position (line, position) inside the stream.
 * The strategy of the reader is to read as many tokens into the buffer.
 * The buffer will be resized if there exists no accepted buffer and more space is needed.
 * When accepted buffer exists the non accepted range will be moved to the start of the buffer.
 * Therefore the information of shifting the buffer will be saved in the delta variable.
 */
class TokenReader {
    private Reader in = null;
    private int bufSize;
    private final int origBufSize;
    private int bufPos = 0;
    private int delta = 0;
    private int offset = 0;
    private int bufLen = 0;
    private char[] buffer = null;
    private int xpos = 0;
    private int ypos = 0;

    private int match_pos = 0;
    private int match_xpos = 0;
    private int match_ypos = 0;
    
    TokenReader(Reader in, int bufSize) {
        this.in = in;
        this.bufSize = bufSize;
        this.origBufSize = bufSize;
        this.buffer = new char[bufSize];
    }

    TokenReader init() throws IOException {
        this.bufLen = in.read(buffer, 0, bufSize);

        return this;
    }
    
    final protected char[] getBuffer() { return buffer; }
    final protected int getOffset() { return offset; }
    final protected int getPos() { return bufPos + delta; }
    final protected int getDelta() { return delta; }
    final protected int getBufSize() { return bufSize; }
    
    final protected char read() throws IOException {
        if (bufPos == bufLen) { // increase buffer
            if (bufPos < bufSize) {
                return 0; // no more characters are available
            } else {
                if (offset == 0) {
                    int newBufSize = bufSize + origBufSize;
                    char[] newBuffer = new char[newBufSize];
                
                    System.arraycopy(buffer, 0, newBuffer, 0, bufPos);

                    buffer = newBuffer;
                    bufSize = newBufSize;
                
                    int noOfReadChars = in.read(buffer, bufPos, bufSize - bufPos);
                
                    bufLen += noOfReadChars < 0 ? 0: noOfReadChars;
                } else {
                    System.arraycopy(buffer, offset, buffer, 0, bufSize - offset);
                    bufPos -= offset;
                    delta = offset;
                    offset = 0;
                    
                    int noOfReadChars = in.read(buffer, bufPos, bufSize - bufPos);
                    
                    bufLen = noOfReadChars < 0 ? 0: bufPos + noOfReadChars;
                }
            }
        }
        return bufPos == bufLen? 0 : buffer[bufPos++];
    }
    
    final protected void mark() {
        match_pos = bufPos;
        match_xpos = xpos;
        match_ypos = ypos;
    }
    
    final protected int reset() {
        bufPos = match_pos;
        xpos = match_xpos;
        ypos = match_ypos;
        
        return bufPos;
    }
    
    final protected void accepted() {
        offset = bufPos;

        mark();
    }
    
    final protected boolean reachedEndOfReader() {
        return bufPos == bufLen;
    }

    final protected String getNonReadString() {
        return bufPos > bufLen ? "" : new String(buffer, bufPos, bufLen - bufPos); 
    }
}