# ExtLex

Splitting texts into tokens is a central task for many text-processing programs.
This process also known as lexical analysis can be implemented by different 
established methods. Using java tokenizer or RegExp, writing self-written scanners 
or use libraries such as lex, flex, jflex, etc. 

extLex simplifies the creation of a scanner by specifying everything in Java.
Using lambda expressions, recognized tokens and each recognized group are processed. 
Furthermore extLex supports recursive expressions and expressions repeated by a separator e.g. ','. 

## Installation

Prerequisite is jdk8. For building mvn is required and only the library Junit5 is needed for testing.

## Definition of tokens

The tokens to be recognized are specified on the basis of regular expressions. 
However, they offer an extension to recognize recursive expressions '(?R)' known from Python and repetitive expressions using {:,} (new). This allows mathematical expressions and comma-separated lists to be processed, for example.

The following EBNF describes the structure of the tokens.

    re ::= re '|' re
         | re re
         | '(' re ')'
         | re ('*'|'+'|'?')
         | re '(?R)'           (* recursive Expression *)
         | re '{:' Char+ '}'   (* re is repeated by the one of the specified character *) 
         | re '{:(' Char+ ')}' (* re is repeated by the one of the specified character
                                   	 and grouped *)
         | CharClass

    CharClass ::= Char | '[' ['^'] CharSet+ ']'

    Char ::= ?Every wnicode char without Special?
           | '\|' | '\(' | '\)' | '\{' | '\}' | '\[' | '\]' | '\<' | \'>'
           | '\\' | '\.' | '\*' | '\+' | '\?' | '\^' | '\$' | '\.' | '\"'

    Special ::= '|' | '(' | ')' | '{' | '}' | '[' | ']' | '<' | '>'
              | '\' | '.' | '*' | '+' | '?' | '^' | '$' | '.' | '"'

    CharSet ::= Char | Char'-'Char 

## Groups
Each group found is processed by a lambda function.

## Example

### First exmple

A lexer is created that returns tokens of the string type.
The pattern “a+” is added to this, which returns a string with the recognized word as a token when it is recognized. 
The scan method is used to build the tokenizer that the scanner uses to read the text and getNextToken returns the next token.

    @Test
    public void test1() throws Exception {
        String token = new Lexer<String>()
            .addPattern("a+", (char[] text, int start, int length) -> new String(text, start, length))
            .scan("aa")
            .getNextToken();
        
        assertEquals("aa", token);
    }

### Second example

Another pattern for a comma without a token is added here. The scanner processes all tokens via the while loop and zero is returned for the comma.
This allows a simple list to be read in by adding all recognized tokens to a list in the getAllTokens method.

    @Test
    public void test2() throws Exception {
        List<String> tokens = new ArrayList<>();

        Scanner<String> scanner = new Lexer<String>()
            .addPattern("[a-z]+", (char[] text, int start, int length) -> new String(text, start, length))
            .addPattern(",")
            .scan("ab,cd");
        
        while (scanner.hasNext()) {
            String t = scanner.getToken();
            
            if (t != null) {
                tokens.add(t);
            }
        }

        assertArrayEquals(new String[]{"ab", "cd"}, tokens.toArray());
    }
     
### Second example simplified
The while loop can be simplified using getAllTokens.

    @Test
    public void test2Simplified() throws Exception {
        List<String> tokens = new ArrayList<>();

        new Lexer<String>()
            .addPattern("[a-z]+", (char[] text, int start, int length) -> new String(text, start, length))
            .addPattern(",")
            .scan("ab,cd")
            .getAllToken((t) -> tokens.add(t));

        assertArrayEquals(new String[]{"ab", "cd"}, tokens.toArray());
    }

### Third example extended greedy quantifier for repeated pattern splitted by a character
To reduce regular expressions, the greedy quantifier has been extended, 
so that the previous expression can be repeated with {: and one or more characters. 
can be repeated. Ideal for lists or mathematical expressions. 

In this example, addPattern has 4 parameters:
  1. the pattern itself
  2. a lambda method to initialize the token
  3. a lambda method to return the token
  4. a lambda method for processing the 1st group.
     Further method can be added for each additional group.

    @Test
    public void test3() throws Exception {
        List<String> tokens = new ArrayList<>();
        String[] emptyStr = new String[0];
        
        String[] result = new Lexer<String[]>()
            .addPattern("([a-z]+){:,}",
                () -> tokens.clear(),
                (t,s,l) -> tokens.toArray(emptyStr),
                (text, start, length) -> tokens.add(new String(text, start, length)))
            .scan("ab,cd,ef")
            .getNextToken();
        assertArrayEquals(new String[]{"ab", "cd", "ef"}, result);
    }
    
### Fourth example of recursive expressions
Recursion '(?R)' can also be used to process more complex expressions such as nested lists or mathematical expressions.

    class Counter {
        int hits;
        
        void inc() { hits++; }
        
        void init() { hits = 0; }
        
        Integer getHits() { return hits; }
    };
    
    @Test
    public void test4() throws Exception {
        Counter con = new Counter();
        
        Tokenizer<Integer> to = new Lexer<Integer>()
            .addPattern("(<(?R)>)*",
                con::init,
                (t, s, l) -> con.getHits(),
                (t, s, l) -> con.inc())
            .createTokenizer();
       
        assertEquals(1, to.scan("<>").getNextToken());
        assertEquals(2, to.scan("<><>").getNextToken());
        assertEquals(2, to.scan("<<>>").getNextToken());        
        assertEquals(3, to.scan("<<><>>").getNextToken());
        assertEquals(4, to.scan("<<><>><>").getNextToken());
    }

### Fifth example with reading the positon of a token

    @Test
    // using getXPos and getYPos of TokenReader for retrieving token start position
    // using getXEndPos and getYEndPos of TokenReader for retrieving token end position
    public void test5() throws Exception {
        String str = "ab cd\n ef\nhj\n";
        List<String> tokens = new ArrayList<>();
        TokenReader tr = new TokenReader(new StringReader(str));
        
        new Lexer<String>()
                .addPattern("[a-z]+", 
                        (t,s, l) -> Integer.toString(tr.getYPos()) + ":" + tr.getXPos() + " " + new String(t, s, l))
                .addPattern("[ \n\r]+")
                .scan(tr)
                .getAllTokens((s) -> tokens.add(s));

        assertArrayEquals(new String[]{"1:1 ab", "1:4 cd", "2:2 ef", "3:1 hj"}, tokens.toArray());
    }

### Fifth example with match
For only matching tokens use the match method.

    @Test
    public void test5WithMatcher() throws Exception {
        String str = "ab cd\n ef\nhj123\n";
        List<String> tokens = new ArrayList<>();
        TokenReader tr = new TokenReader(new StringReader(str));
        
        new Lexer<String>()
                .addPattern("[a-z]+", 
                        (t,s, l) -> Integer.toString(tr.getYPos()) + ":" + tr.getXPos() + " " + new String(t, s, l))
                .match(tr)
                .getAllTokens((s) -> tokens.add(s));

        assertArrayEquals(new String[]{"1:1 ab", "1:4 cd", "2:2 ef", "3:1 hj"}, tokens.toArray());
    }

### Further examples

See test folder e.g. for mathematical expressions.
