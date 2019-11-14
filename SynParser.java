/*
 * Class:       CS4308 Section 2
 * Term:        Fall 2019
 * Name:        Patrick Sweeney, Christian Byrne, and Sagar Patel
 * Instructor:  Deepa Muralidhar
 * Project:     Deliverable 2 Parser - Java
 */

import java.io.IOException;
import java.util.Queue;
import java.util.Stack;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class SynParser {
    private LexScanner lexScanner;
    private Map<String,Integer> symbolTable = new HashMap<String, Integer>();
    private Map<String,String> grammar = new HashMap<String,String>();
    
    class Node {
        private Node parentNode;
        private ArrayList<Node> nodeList;
        private String nodeType;
        private String value;

        private boolean errorFlag;      // set by parser when found erroneous
        private int     errorCode;      // 0-normal; 1-child; 2-current; 3-other; 
        private int     errorLine;      // line in which the error occured
        private int     errorPos;       // cursor position which the error occured
        private String  errorExpected;  // expected token value
        
        Node() {
            nodeList = new ArrayList<Node>();

            this.nodeType = null;
            this.value = null;

            this.errorFlag = false;
            this.errorCode = 0;
            this.errorLine = 0;
            this.errorPos  = 0;
        }

        Node(String nodeType, String value, Node ... nodes) {
            nodeList = new ArrayList<Node>();

            this.nodeType = nodeType;
            this.value = value;

            this.errorFlag = false;
            this.errorCode = 0;
            this.errorLine = 0;
            this.errorPos  = 0;

            addChild(nodes);
        }

        // add a single child to a list
        void addChild(Node child) {
            if (child != null) {
                child.setParent(this);
                nodeList.add(child);

                // if error code isn't already set, set it to code for
                // errors caused by children
                if (this.errorCode == 0 && child.getError() != 0) {
                    this.errorCode = 1;
                }
            }
        }

        // add an array of children to the list.
        // array is expected to be in left-to-right order.
        void addChild(Node ... children) {
            for (Node x : children) { addChild(x); }
        }
        // returns an array of the node's children
        ArrayList<Node> getChildren() { return nodeList; }
        
        // set the parent reference of the node
        void setParent(Node parent) { this.parentNode = parent; }

        // get the parent reference of the node
        Node getParent() { return parentNode; }

        // get the token the node is associated with
        String getNodeType() {
            return this.nodeType;
        }
        
        // get the lexeme the node is associated with
        String getNodeValue() {
            return this.value;
        }
        
        boolean isFlagged()     { return this.errorFlag; }
        int getError()          { return this.errorCode; }
        int getErrorLine()      { return this.errorLine; }
        int getErrorPos()       { return this.errorPos; }
        String getErrorToken()  { return this.errorExpected; }

        // flag the node as being erroneous
        void setFlag() { this.errorFlag = true; }

        // marks the node with an error statement to be raised when the print output reaches it 
        void raiseError(String expToken, LexScanner scanner) { 
            //Checks to make sure they don't match and an error hasn't already been raised for that node
            if(!(expToken.equals(scanner.getToken())) & this.errorCode < 2){
                this.errorCode      = 2;
                this.errorLine      = scanner.getLine();
                this.errorPos       = scanner.getPosition();
                this.errorExpected  = expToken;
            }
        }
        
        void raiseError(String expToken, LexScanner scanner, int customCode) { 
            //Checks to make sure they don't match and an error hasn't already been raised for that node
            if(!(expToken.equals(scanner.getToken())) & this.errorCode < 2){
                this.errorCode      = customCode;
                this.errorLine      = scanner.getLine();
                this.errorPos       = scanner.getPosition();
                this.errorExpected  = expToken;
            }
        }
    }


    /** (deprecated) Node creation functions */
    Node createNode(String nodeType, Node ... nodes) {
        return new Node(nodeType, null, nodes);
    }
    // creates a terminal node
    Node createLeaf(String nodeType, String value) {
        return new Node(nodeType, value);
    }


    // test that a node contains a keyword or a selection of literals
    boolean isKeyword(Node node) {
        String nodeType = node.getNodeType();
        return (nodeType.matches("(.*(_kw|(paren|colon)_lt))"));
    }
    
    // map current token code to a range of possible token categories
    String getOpClass() {
        String opClass = null;
        int code = Integer.parseInt(lexScanner.getTokenCode());

        if(code >= 2002 && code <= 2007){
            opClass = "relative_op";
        }else if(code >= 2008 && code <= 2015){
            opClass = "arithmetic_op";
        }else if(code == 2001){
            opClass = "assignment_op";
        }else{
            opClass = "other";
        }

        return opClass;
    }

    // parser constructor
    SynParser(String source) throws Exception {
        lexScanner = new LexScanner(source);
        buildGrammar();
        parse();
    }
    
    void buildGrammar(){
        grammar.put("program", "function id ( ) <block> end");
        grammar.put("block", "<statement> | <statement> <block>");
        grammar.put("statement", "<if_statement> | <assignment_statement> | <while_statement> | <print_statement> | <for_statement>");
        grammar.put("if_statement", "if <boolean_expression>  <block> else <block> end");
        grammar.put("while_statement", "while <boolean_expression> <block> end");
        grammar.put("assignment_statement", "id <assignment_operator> <arithmetic_expression>");
        grammar.put("for_statement", "for id = <iter> <block> end");
        grammar.put("print_statement", "print ( <arithmetic_expression> )");
        grammar.put("iter", "<arithmetic_expression> : <arithmetic_expression>");
        grammar.put("boolean_expression", "<relative_op> <arithmetic_expression> <arithmetic_expression>");
        grammar.put("relative_op", "le_operator | lt_operator | ge_operator | gt_operator | eq_operator | ne_operator");
        grammar.put("arithmetic_expression", "<id> | <literal_integer> | <binary_expression");
        grammar.put("binary_expression", "<arithmetic_op> <arithmetic_expression> <arithmetic_expression>");
        grammar.put("arithmetic_op", "add_operator | sub_operator | mul_operator | div_operator | mod_operator | exp_operator | rev_div_operator");
    }
    
    // entry point for the parser
    void parse() throws IOException, Exception {
        
        Node result;
        lexScanner.nextToken();

        // construct a parse tree of the source file and print the grammar
        // rules associated with each sentence
        result = program();

        //printTree(result);

        try {
            printOutput(result);
        } catch (Exception e) {
            throw e;
        }
    }

    // returns a parse tree that represents the syntactic structure of the input
    Node program() throws IOException {
        // <program> -> ...
        Node result = new Node("program", null);
        
        if(!lexScanner.getToken().equals("function_kw")) {
            result.raiseError("function_kw", lexScanner);
            return result;
        }

        // ... function
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("identifier")) {
            result.raiseError("identifier", lexScanner);
            return result;
        }

        // ... id
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("open_paren_lt")) {
            result.raiseError("open_paren_lt", lexScanner);
            return result;
        }

        // ... (
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("close_paren_lt")) {
            result.raiseError("close_paren_lt", lexScanner);
            return result;
        }

        // ... )
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        // ... <block>
        result.addChild(block());
        // call to nextToken() shouldn't occur here as it is called at the end of block

        if (!lexScanner.getToken().equals("end_kw")) {
            result.raiseError("end_kw", lexScanner, 3);
            return result;
        }

        // ... end
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));

        return result;
    }

    Node block() throws IOException {
        // <block> -> ...
        Node result = new Node("block", null);

        // <statement> ...
        result.addChild(statement());
        lexScanner.nextToken();

        // continue to expand the block if scanner isn't at the 
        // end of file, or on any terminating keywords
        if (result.getError() == 0 && !lexScanner.getToken().matches("(EOF)|((else|end)_kw)")) {
            // ... <block>
            result.addChild(block());
            // call to nextToken() shouldn't occur here as it is called at the end of block
        }

        return result;
    }

    //Statement 
    Node statement() throws IOException{
        Node statement = new Node("statement", null);
        Node result = null;

        // test each possible handle that can apply for a statement,
        // and throw an error if the current token isn't a handle
        switch (lexScanner.getToken()) {
            case "if_kw":
            result = ifStatement();
            break;

            case "identifier":
            result = assignStatement();
            break;

            case "while_kw":
            result = whileStatement();
            break;

            case "print_kw":
            result = printStatement();
            break;

            case "for_kw":
            result = forStatement();
            break;

            default:
            statement.raiseError("unknown", lexScanner);
            break;
        }

        // add the tree of the handle to the statement
        statement.addChild(result);
        return statement;
    }

    // Boolean expression
    Node boolExp() throws IOException{
        // <boolean_expression> -> ...
        Node node = new Node("boolean_expression", null);

        //Check if relative operator
        if(!getOpClass().equals("relative_op")){
            node.raiseError("relative_op", lexScanner);
        }

        // ... <relative_op>
        node.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        // ... <arithmetic_expression>
        node.addChild(arithExp());
        lexScanner.nextToken();

        // ... <arithmetic_expression>
        node.addChild(arithExp());

        return node;
    }

    // Arithmetic expression
    Node arithExp() throws IOException{
        // <arithmetic_expression> -> ...
        Node result = new Node("arithmetic_expression", null);
        Node node;
        if(lexScanner.getToken().equals("identifier") | lexScanner.getToken().equals("integer_lt")){
            // ... id
            node = createLeaf(lexScanner.getToken(), lexScanner.getLexeme());
        }else{
            // ... <binary_expression>
            node = binExp();
        }
        result.addChild(node);
        return result;
    }

    // Binary Expression
    Node binExp() throws IOException{
        // <binary_expression>
        Node result = new Node("binary_expression", null);

        // -> <arithmetic_op> ...
        if(getOpClass().equals("arithmetic_op")){
            String opString;
            Node left, right;
            
            result.addChild(new Node("arithmetic_op", lexScanner.getLexeme()));
            lexScanner.nextToken();

            // ... <arithmetic_expression>
            result.addChild(arithExp());
            lexScanner.nextToken();

            // ... <arithmetic_expression>
            result.addChild(arithExp());
        }else{
            result.raiseError("arithmetic_op", lexScanner);
        }

        return result;
    }
    
    Node assignStatement() throws IOException{
        // <assignment_statement> -> ...
        Node result = new Node("assignment_statement", null);

        // ... id
        Node idNode = createLeaf("identifier", lexScanner.getLexeme());
        
        //store variable name and get next token
        String varName = lexScanner.getLexeme();
        lexScanner.nextToken();

        if(!getOpClass().equals("assignment_op")) {
            result.raiseError("assignment_op", lexScanner);
        }

        lexScanner.nextToken();
        Node arithExp = arithExp();
        
        //get variable value
        int varValue = calculateValue(arithExp);
        symbolTable.put(varName, varValue);
        
        // ... <assignment_op>
        Node assignLit = createLeaf("assignment_op", "=");

        // ... <arithmetic_expression>
        result.addChild(idNode, assignLit, arithExp()); 

        return result;
    }

    // iteration expression
    Node iter() throws IOException {
        // <iter> -> ...
        Node result = new Node("iter", null);

        // ... <arithmetic_expression>
        result.addChild(arithExp());
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("colon_lt")) {
            error("colon_lt", lexScanner.getToken());
        }

        // ... :
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        // ... <arithmetic_expression>
        result.addChild(arithExp());

        return result;
    }

    // if statement
    Node ifStatement() throws IOException{
        // <if_statement> -> ...
        Node result = new Node("if_statement", null);

        if(!lexScanner.getToken().equals("if_kw")) {
            result.raiseError("if_kw", lexScanner);
        }

        // ... if
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        // ... <boolean_expression>
        result.addChild(boolExp());
        lexScanner.nextToken();

        // ... <block>
        result.addChild(block());
        // call to nextToken() shouldn't occur here as it is called at the end of block

        if(lexScanner.getToken().equals("else_kw")) {
            // ... else
            result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
            lexScanner.nextToken();

            // ... <block>
            result.addChild(block());
            // call to nextToken() shouldn't occur here as it is called at the end of block
        }

        
        if(!lexScanner.getToken().equals("end_kw")) {
            result.raiseError("end_kw", lexScanner, 3);
        }
        
        // ... end
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));

        return result;
    }

    Node whileStatement() throws IOException{
        // <while_statement> -> ...
        Node result = new Node("while_statement", null);

        if(!lexScanner.getToken().equals("while_kw")) {
            result.raiseError("while_kw", lexScanner);
        }

        // ... while
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        // ... <boolean_expression>
        result.addChild(boolExp());
        lexScanner.nextToken();

        // ... <block>
        result.addChild(block());
        // call to nextToken() shouldn't occur here as it is called at the end of block

        if(!lexScanner.getToken().equals("end_kw")) {
            result.raiseError("end_kw", lexScanner, 3);
        }

        // ... end
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));

        return result;
    }
    
    Node forStatement() throws IOException{
        // <for_statement> -> ...
        Node result = new Node("for_statement", null);

        if(!lexScanner.getToken().equals("for_kw")) {
            result.raiseError("for_kw", lexScanner);
        }

        // ... for
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("identifier")) {
            result.raiseError("identifier", lexScanner);
        }

        // ... id
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("assign_op")) {
            result.raiseError("assign_op", lexScanner);
        }

        // ... <assignment_op>
        result.addChild(new Node("assignment_op", lexScanner.getLexeme()));
        lexScanner.nextToken();

        // ... <iter>
        result.addChild(iter());
        lexScanner.nextToken();

        // ... <block>
        result.addChild(block());
        // call to nextToken() shouldn't occur here as it is called at the end of block

        if(!lexScanner.getToken().equals("end_kw")) {
            result.raiseError("end_kw", lexScanner, 3);
        }

        // ... end
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));

        return result;
    }

    Node printStatement() throws IOException{
        // <print_statement> -> ...
        Node result = new Node("print_statement", null);
        Node step = null;

        if(!lexScanner.getToken().equals("print_kw")) {
            result.raiseError("print_kw", lexScanner);
        }

        // ... print
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("open_paren_lt"))
            result.raiseError("open_paren_lt", lexScanner);

        // ... (
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        // ... <arithmetic_expression>
        result.addChild(arithExp());
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("close_paren_lt"))
            result.raiseError("close_paren_lt", lexScanner);

        // ... )
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));

        return result;
    }

    int calculateValue(Node n){
        
        Queue<Node> q = new LinkedList<>();
        ArrayList<Node> children;
        int[] values = new int[2];
        int index = 0;
        int result;
        Node node;
        String opType = null;
        
        q.add(n);
        while(q.size() >0){
            node = q.remove();
            //inspect node
            if(node.getNodeType().equals("arithmetic_op")){
                opType = node.getNodeValue();
            }else if(node.getNodeType().equals("identifier")){
                values[index++] = symbolTable.get(node.getNodeValue());
            }else if(node.getNodeType().equals("integer_lt")){
                values[index++] = Integer.parseInt(node.getNodeValue());
            }
            //add children
            children = node.getChildren();
            if (children.size() > 0) {
                for (Node x : children) {
                    q.add(x);
                }
            }
        }
        if(opType != null){
            result = performCalc(opType, values, index);
        }else{
            result = values[0];
        }
        
        return result;
    }
    
    int performCalc(String operator, int[] operand, int index){
        int result = 0;
        
        switch (operator) {
            case "+":
            if (index == 1){
                result = operand[0];
            }else{
                result = operand[0] + operand[1];
            }
            break;
            
            case "-":
            if(index == 1){
                result = - operand[0];
            }else{
                result = operand[0] - operand[1];
            }
            break;
                
            case "*":
            result = operand[0] * operand[1];
            break;
                
            case "/":
            result = operand[0] / operand[1];
            break;
                
            case "%":
            result = operand[0] % operand[1];
            break;
                
            case "\\":
            result = operand[1] / operand[0];
            break;
                
            case "^":
            result = (int) Math.pow(operand[0], operand[1]);
            break;
        }
        
        return result;
    }
        
    // Formats error and throws exception
    void error(String expToken, String actToken){
        if(!(expToken.equals(actToken))){
            System.out.printf("Expecting %s, received %s\n", expToken, actToken);
        }
    }
    
    // print a tree starting at the root (depth zero) <for testing purposes>
    void printTree(Node n) {
        printTree(n, 0);
    }

    // print tree <for testing purposes>
    void printTree(Node n, int depth) {
        ++depth;

        StringBuilder whitespace = new StringBuilder();

        if(n != null){
            for (int i = depth; i > 0; i--) {
                whitespace.append('-');
            }

            System.out.println(whitespace.toString() + n.getNodeType());
            String value;

            if((value = n.getNodeValue()) != null){
                System.out.println(whitespace.toString() + "> '" + value + "'");
            }else{
                //System.out.println();
                for (Node x : n.getChildren()) printTree(x, depth);
            }
        }
    }
    
    // prints the syntactic rules of the node, from left-to-right, depth-first order
    void printOutput(Node n) throws Exception {
        StringBuilder out;
        Stack<Node> q = new Stack<>();
        Stack<Node> i = new Stack<>();
        q.add(n);
        String printLits = "";

        ArrayList<Node> children;
        ArrayList<String> typeArray;
        Node node;

        Exception ex = null;

        while(q.size() > 0) {
            node = q.pop();
            
            if (node != null) {
                out = new StringBuilder();
                children = node.getChildren();
                // If not a keyword or string literal ie. if or ( wrap output in <> for non terminal node ie <statement>
                if (!isKeyword(node)) {
                    out.append("<" + node.getNodeType() + "> -> ");
                    //perform same output modifying for children check for error and add children to stack
                    if (children.size() > 0) {
                        for (Node x : children) {
                            if (isKeyword(x)) {
                                out.append(x.getNodeValue() + ' ');
                            } else if (x.getNodeType().equals("identifier")) {
                                out.append("id ");
                            } else {
                                out.append("<" + x.getNodeType() + "> ");
                            }

                            // if the node is the source of an error then raise exception
                            if (ex == null && x.getError() == 2) {
                                System.out.println(x.getErrorToken());
                                ex = new Exception("Expected " + x.getErrorToken() + " at line " + x.getErrorLine() + " token " + x.getErrorPos() + ".\nCorrect grammar for " + x.getNodeType() + " is " + grammar.get(x.getNodeType()) + ".\n");
                                x.setFlag();
                            }

                            i.add(x);
                        }
                        
                        out.append("\b\n");
                        printLits += out.toString();

                        while (i.size() > 0) {
                            q.add(i.pop());
                        }
                    }
                }

                // if the node is the source of an error then raise exception
                if (ex == null && node.getNodeType().equals("program") && node.getError() == 2) {
                    ex = new Exception("Expected " + node.getErrorToken() + " at line " + node.getErrorLine() + " token " + node.getErrorPos() + ".\nCorrect grammar for " + node.getNodeType() + " is " + grammar.get(node.getNodeType()) + ".\n");
                }

                // if there's a terminating node with a value such as an integer literal or an identifier add to end of output string
                if(node.getNodeValue() != null && !isKeyword(node)){
                    if (node.getNodeType().equals("identifier")) {
                        printLits += "".format("%s -> %s\n", node.getNodeValue(), "id");
                    } else {
                        printLits += "".format("%s -> <%s>\n", node.getNodeValue(), node.getNodeType());
                    }
                }
            }

            if (ex != null && node.isFlagged()) {
                System.out.println(printLits);
                throw ex;
            }
        }

        System.out.println(printLits);
        //if (ex != null) throw ex;
    }
}
