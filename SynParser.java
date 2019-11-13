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

class SynParser {
    private LexScanner lexScanner;

    class Node {
        private Node parentNode;
        private ArrayList<Node> nodeList;
        private String nodeType;
        private String value;

        private int     errorCode;
        private int     errorLine;
        private int     errorPos;
        private String  errorExpected;
        
        Node() {
            nodeList = new ArrayList<Node>();

            this.nodeType = null;
            this.value = null;

            this.errorCode = 0;
            this.errorLine = 0;
            this.errorPos  = 0;
        }

        Node(String nodeType, String value, Node ... nodes) {
            nodeList = new ArrayList<Node>();

            this.nodeType = nodeType;
            this.value = value;

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
                if (child.getError() != 0) { this.errorCode = 1; }
            }
        }

        // add an array of children to the list.
        // array is expected to be in left-to-right order.
        void addChild(Node ... children) {
            for (Node x : children) { addChild(x); }
        }

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
        
        int getError() { return this.errorCode; }

        int getErrorLine()      { return this.errorLine; }
        int getErrorPos()       { return this.errorPos; }
        String getErrorToken()  { return this.errorExpected; }

        void raiseError(String expToken, LexScanner scanner) { 
            if(!(expToken.equals(scanner.getToken()))){
                System.out.printf("Expecting %s, received %s\n", expToken, scanner.getToken());
                this.errorCode      = 2;
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
        parse();
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
            result.raiseError("end_kw", lexScanner);
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
        if(getOpClass().equals("relative_op")){
            // ... <relative_op>
            node.addChild(new Node("relative_op", lexScanner.getLexeme()));
            lexScanner.nextToken();

            // ... <arithmetic_expression>
            node.addChild(arithExp());
            lexScanner.nextToken();

            // ... <arithmetic_expression>
            node.addChild(arithExp());
        } else {
            //throw error
            error("relative_op", lexScanner.getToken());
        }
        return node;
    }

    // Arithmetic expression
    Node arithExp() throws IOException{
        // <arithmetic_expression> -> ...

        Node node;
        if(lexScanner.getToken().equals("identifier") | lexScanner.getToken().equals("integer_lt")){
            // ... id
            node = createLeaf(lexScanner.getToken(), lexScanner.getLexeme());
        }else{
            // ... <binary_expression>
            node = binExp();
        }

        return createNode("arithmetic_expression", node);
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
            error("arithmetic_op", lexScanner.getToken());
        }

        return result;
    }
    
    Node assignStatement() throws IOException{
        // <assignment_statement> -> ...
        Node result = new Node("assignment_statement", null);

        // ... id
        Node idNode = createLeaf("identifier", lexScanner.getLexeme());

        lexScanner.nextToken();

        if(!getOpClass().equals("assignment_op")) {
            result.raiseError("assignment_op", lexScanner);
        }

        lexScanner.nextToken();

        // ... <assignment_op>
        Node assignLit = createLeaf("assignment_op", "=");

        // ... <arithmetic_expression>
        result.addChild(idNode, assignLit, arithExp()); 

        return result;
    }

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
            error("end_kw", lexScanner.getToken());
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
            result.raiseError("end_kw", lexScanner);
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
            result.raiseError("end_kw", lexScanner);
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
            error("open_paren_lt", lexScanner.getToken());

        // ... (
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));
        lexScanner.nextToken();

        // ... <arithmetic_expression>
        result.addChild(arithExp());
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("close_paren_lt"))
            error("close_paren_lt", lexScanner.getToken());

        // ... )
        result.addChild(new Node(lexScanner.getToken(), lexScanner.getLexeme()));

        return result;
    }

    // Formats error and throws exception
    void error(String expToken, String actToken){
        if(!(expToken.equals(actToken))){
            System.out.printf("Expecting %s, received %s\n", expToken, actToken);
        }
    }
    
    // print a tree starting at the root (depth zero)
    void printTree(Node n) {
        printTree(n, 0);
    }

    // temporary print function
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

                if (!isKeyword(node)) {
                    out.append("<" + node.getNodeType() + "> -> ");

                    if (children.size() > 0) {
                        for (Node x : children) {
                            if (isKeyword(x)) {
                                out.append(x.getNodeValue() + ' ');
                            } else if (x.getNodeType().equals("identifier")) {
                                out.append("id ");
                            } else {
                                out.append("<" + x.getNodeType() + "> ");
                            }

                            if (x.getError() == 2) {
                                ex = new Exception("Expected " + x.getErrorToken() + " at line " + x.getErrorLine() + " column " + x.getErrorPos() + ".\n");
                            }

                            i.add(x);
                        }
                        
                        out.append("\b\n");
                        printLits += out.toString();
                    }

                    while (i.size() > 0) {
                        q.add(i.pop());
                    }
                }
            }
            
            if(node.getNodeValue() != null && !isKeyword(node)){
                if (node.getNodeType().equals("identifier")) {
                    printLits += "".format("%s -> %s\n", node.getNodeValue(), "id");
                } else {
                    printLits += "".format("%s -> <%s>\n", node.getNodeValue(), node.getNodeType());
                }
            }
        }

        System.out.println(printLits);
        if (ex != null) throw ex;
    }
}
