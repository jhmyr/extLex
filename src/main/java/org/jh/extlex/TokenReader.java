/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jh.extlex;

import java.io.IOException;
import java.io.Reader;
/**
 * a reader for scanning tokens from an input stream. 
 * The reader provide enough space for keeping one token in buffer.
 * The buffer will be extended if more space is needed.
 * After one token was readed the reset command must be called for reseting all internal 
 * information and swap out the last token out of buffer.
 * Furthermore the reader provides information of the current position (line, position) inside the stream.
 * @author Jörg
 */
class TokenReader {
    private Reader in = null;
    private int bufSize = 0;
    private int bufPos = 0;
    private int offset = 0;
    private int xpos = 1;
    private int ypos = 1;

    private int match_pos = 0;
    private int match_xpos = 1;
    private int match_ypos = 1;
    private int buf_len = 0;
    private char[] buffer = null;
    
    TokenReader(Reader in, int bufSize) {
        this.in = in;
        this.bufSize = bufSize;
        this.buffer = new char[bufSize];
    }

    TokenReader init() throws IOException {
        this.buf_len = in.read(buffer, 0, bufSize);

        return this;
    }
    
    final protected char[] getBuffer() { return buffer; }
    final protected int getOffset() { return offset; }
    final protected int getPos() { return bufPos; }
    final protected int getBufSize() { return bufSize; }
    
    final protected char read() throws IOException {
        return bufPos == buf_len? 0 : buffer[bufPos++];
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
    
    final protected void tokenAccepted() {
        offset = bufPos;

        mark();
    }
    
    final protected boolean reachedEndOfReader() {
        return bufPos == buf_len;
    }

    final protected String getNonReadString() {
        return bufPos > buf_len ? "" : new String(buffer, bufPos, buf_len - bufPos); 
    }
}