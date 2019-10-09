import java.io.*;
import java.util.*;


class Lexical{
    private SourceArray sa;
    private Dictionary tokenTypes;
    // stores source code in bufferedreader and abstracts process of getting the next lexeme
    class SourceArray{
        private int line; // current line
        private int pos; // current pos
        private String[] sourceLine; //
        private BufferedReader br;
        // Constructor. Init line and pos #'s in source code and creates a buffered reader object with source file
        SourceArray(String source) throws FileNotFoundException{
            line = pos = 0;
            File file = new File(source);
            br = new BufferedReader(new FileReader(file));
        }
        // Retrieves next line in the buffered reader. Returns false if the buffered reader is EOF
        private boolean newLine() throws IOException{
            String s;
            s = br.readLine();
            if(s != null){
                sourceLine = s.split(" ");
                line++;
                pos = 0;
                return true;
            }else{
                return false;
            }
        }
        // Returns current line 1...n in source code
        int currentLine(){
            return line + 1;
        }
        // Returns current position 1...n in current line
        int currentPos(){
            return pos + 1;
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
    Lexical(String source) throws Exception{
        sa = new SourceArray(source);
        //sa.printAll();
    }
    
    void initTokenTypes(){
        String s = "id,literal_integer,assignment_operator,le_operator,lt_operator,ge_operator,gt_operator,eq_operator";
        tokenTypes = new Hashtable();
        tokenTypes.put("id", 1001);
        tokenTypes.put("literal_integer", 1002);
        tokenTypes.put("assignment_operator", 1003);
        tokenTypes.put("le_operator", 1004);
        tokenTypes.put("lt_operator", 1005);
        tokenTypes.put("ge_operator", 1006);
    }
    
    String nextToken() throws IOException{
        String lex = sa.nextLex();
        switch (lex){
            case "EOF": return "EOF"; 
        }
        return lex;
    }
    
    
    public static void main(String[] args) throws Exception{
        Lexical l = new Lexical("hello_julia");
        String s;
        while((s = l.nextToken()) != "EOF")
            System.out.println(s);
    }
    
}