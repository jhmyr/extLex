/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package org.jh.extlex;

import java.io.StringReader;

/**
 *
 * @author Jörg
 */
public class Tokenizer<T> {
    private final DRootState root;

    Tokenizer(DRootState dRoot) {
        this.root = dRoot;
    }

    public Scanner<T> scan(String input) throws Exception {
        StringReader sr = new StringReader(input);
        TokenReader tr = new TokenReader(sr, input.length()).init();
        
        return new Scanner<>(root, tr);
    }
}
