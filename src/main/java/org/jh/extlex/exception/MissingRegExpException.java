package org.jh.extlex.exception;

public class MissingRegExpException extends Exception {

    public MissingRegExpException() {
        super("No regular expression defined!");
    }
    
}
