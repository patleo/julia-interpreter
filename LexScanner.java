/*
 * Class:       CS4308 Section 2
 * Term:        Fall 2019
 * Name:        Patrick Sweeney, Christian Byrne, and Sagar Patel
 * Instructor:  Deepa Muralidhar
 * Project:     Deliverable 1 Lexical Scanner - Java
 */

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

// Lexical Scanner class
class LexScanner{
    private SourceArray sa; // stores source code for easy retrieval
    private Map<String,String> tokType; // keyword table
    private String curLex; // current lexeme
    private String curTok; // current token
    private String curTokCode; // current token code
    // stores source code in bufferedreader and abstracts process of getting the next lexeme
    class SourceArray{
        private int line; // current line
        private int pos; // current pos
        private String[] sourceLine; //array of the current line of lexemes
        private BufferedReader br; // buffers source code for line by line retrieval
        
        // Constructor. Init line and pos #'s in source code and creates a buffered reader object with source file
        SourceArray(String source) throws FileNotFoundException{
            line = pos = 0;
            File file = new File(source);
            br = new BufferedReader(new FileReader(file));
        }

        // Retrieves next line in the buffered reader. Returns false if the buffered reader is EOF
        private boolean newLine() throws IOException{
            String s;
            // reads next line until it finds a non-commented line of code or end of file
            do{
            s = br.readLine();
            }while(s != null && (s.length() < 1 || s.trim().charAt(0) == '#'));
            
            if(s != null){
                // handles case of multiple spaces or leading spaces
                sourceLine = s.trim().split(" +");
                line++;
                pos = 0;
                return true;
            }else{
                return false;
            }
        }

        // Returns current line 1...n in source code
        int currentLine(){
            return line;
        }

        // Returns current position 1...n in current line
        int currentPos(){
            return pos;
        }

        // Returns next lexeme in source file. Returns "EOF" if at end of file.
        String nextLex() throws IOException{
            if((line == 0 && pos == 0)||(pos == sourceLine.length)){
                if(this.newLine()){
                    return sourceLine[pos++];
                }else{
                    return "EOF";
                }
            }else{
                return sourceLine[pos++];
            }
        }

        // Prints all lexemes in the source file
        void printAll() throws IOException{
            String s;
            while((s = this.nextLex()) != "EOF")
                System.out.println(s);
        }
    }
  
    // Constructor: Feeds source code to SourceArray 
    LexScanner(String source) throws Exception{
        sa = new SourceArray(source); //initialize data structure to store source code
        tokType = initTokenTypes(); //initialize keyword/operator table
    }

    // Initializes the keyword table for tokens that have a single literal representation ex "while", ")", "+" not <integer> or <id>
    Map<String, String> initTokenTypes(){
        Map<String,String> tokType = new HashMap<String, String>();
        // Keywords
        tokType.put("function", "function_kw:1001");
        tokType.put("end",      "end_kw:1002");
        tokType.put("if",       "if_kw:1003");
        tokType.put("then",     "then_kw:1004");
        tokType.put("else",     "else_kw:1005");
        tokType.put("while",    "while_kw:1006");
        tokType.put("do",       "do_kw:1007");
        tokType.put("repeat",   "repeat_kw:1008");
        tokType.put("until",    "until_kw:1009");
        tokType.put("print",    "print_kw:1010");
        tokType.put("for",      "for_kw:1011");
        // Operators
        tokType.put("=",        "assign_op:2001");
        tokType.put("<=",       "le_op:2002");
        tokType.put("<",        "lt_op:2003");
        tokType.put(">=",       "ge_op:2004");
        tokType.put(">",        "gt_op:2005");
        tokType.put("==",       "eq_op:2006");
        tokType.put("!=",       "ne_op:2007");
        tokType.put("+",        "add_op:2008");
        tokType.put("-",        "sub_op:2009");
        tokType.put("*",        "mult_op:2010");
        tokType.put("/",        "div_op:2011");
        tokType.put("%",        "mod_op:2012");
        tokType.put("\\",       "rev_div_op:2013");  
        tokType.put("^",        "exp_operator:2014");
        tokType.put("!",        "neg_operator:2015");
        // String literals
        tokType.put("(",        "open_paren_lt:3003");
        tokType.put(")",        "close_paren_lt:3004");
        tokType.put(":",        "colon_lt:3005");
        // For reference but not stored in this data structure
        // id, "identifier:3001"
        // integer, "integer_lt:3002"
        // EOF: "EOF:4001"
        // DNE: "DNE:5001"
        return tokType;
    }
    
    // Returns the token type and token code in one string ex integer_lt:3002
    String getTokenType(String lexeme){
        // Check if lexeme in keyword table, else determine if one of other possible options
        if(tokType.containsKey(lexeme)){
            return tokType.get(lexeme);
        }else if(isInteger(lexeme)){
            return "integer_lt:3002";
        }else if(isID(lexeme)){
            return "identifier:3001";
        }else if(lexeme.equals("EOF")){
            return "EOF:4001";
        }else{
            return "DNE"; // Does not exist
        }
    }
    
    // Splits 2 part token string into String array of token name and token code
    String[] splitTokenString(String tokenString){
        String[] splitString = tokenString.split(":");
        return splitString;
    }

    // Returns next token
    String nextToken() throws IOException{
        this.curLex = sa.nextLex();
        String tokenType = this.getTokenType(this.curLex);
        // If lemexe in token table assign values to class variables
        if(tokenType != "DNE"){
            String[] splitToken = this.splitTokenString(tokenType);
            this.curTok = splitToken[0];
            this.curTokCode = splitToken[1];
        }else{
            this.curTok = "DNE";
            this.curTokCode = "5001";   
        }
        return this.curTok;
    }

   // Determines if lexeme is an integer
    boolean isInteger(String lexeme){
        try{
            int test = Integer.parseInt(lexeme);
        }catch(NumberFormatException nfe){
            return false;
        }
        return true;
    }

    // Determines if lexeme is an identifier
    boolean isID(String lexeme){
        // Checks if one character letter
        if(lexeme.length() == 1 && Character.isLetter(lexeme.charAt(0))){
            return true;
        }else{
            return false;
        }
    }

    // Returns current token - slightly different than next token
    String getToken(){
        return this.curTok;
    }

    // Returns current token code 
    String getTokenCode(){
        return this.curTokCode;
    }

    // Returns current lexeme
    String getLexeme(){
        return this.curLex;
    }

    // Returns line number of current lexeme 1...n
    int getLine(){
        return sa.currentLine();   
    }

    // Returns position (lexeme count) of current lexeme in line 1...n
    int getPosition(){
        return sa.currentPos();
    }
}
