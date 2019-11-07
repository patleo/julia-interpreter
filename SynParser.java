/*
 * Class:       CS4308 Section 2
 * Term:        Fall 2019
 * Name:        Patrick Sweeney, Christian Byrne, and Sagar Patel
 * Instructor:  Deepa Muralidhar
 * Project:     Deliverable 2 Parser - Java
 */

import java.io.IOException;
import java.util.Queue;
import java.util.LinkedList; 

class SynParser {
    private LexScanner lexScanner;

    class Node {
        private Node parentNode;
        private Node leftNode;
        private Node rightNode;
        private String nodeType;
        private String value;
        
        Node(){
            this.leftNode = null;
            this.rightNode = null;
            this.nodeType = null;
            this.value = null;
        }
        Node(String nodeType, Node leftNode, Node rightNode, String value){
            this.nodeType = nodeType;
            this.leftNode = leftNode;
            this.rightNode = rightNode;
            this.value = value;
        }
        
        String getNodeType(){
            return this.nodeType;
        }
        
        String getNodeValue(){
            return this.value;
        }
        
        Node getLeftNode(){
            return this.leftNode;
        }
        
        Node getRightNode(){
            return this.rightNode;
        }
        
    }
    // Node creating functions
    Node createNode(String nodeType, Node leftNode, Node rightNode){
        return new Node(nodeType, leftNode, rightNode, null);
    }
        
    Node createNode(String nodeType, Node leftNode){
        return new Node(nodeType, leftNode, null, null);
    }

    Node createLeaf(String nodeType, String value){
        return new Node(nodeType, null, null, value);
    }
    
    String getOpClass(){
        String opClass = null;
        int code = Integer.parseInt(lexScanner.getTokenCode());
        if(code >= 2002 & code <= 2007){
            opClass = "relative_op";
        }else if(code >= 2008 & code <= 2015){
            opClass = "arithmetic_op";
        }else{
            opClass = "other";
        }
        return opClass;
    }
    

    SynParser(String source) throws Exception {
        lexScanner = new LexScanner(source);
        parse();
    }
    //Drives the parsing process
    void parse() throws IOException{
        Node result, printNode;
        while(!(lexScanner.nextToken()).equals("EOF")){
            result = statement();
            printNode = createNode("block", result);
            printTree(printNode);
            printOutput(printNode);
        }
    }
    //Statement 
    Node statement() throws IOException{
        
        Node result = null;
        if(lexScanner.getToken().equals("if_kw")){
            Node bool;
            lexScanner.nextToken();
            result = boolExp();
        }else if(getOpClass().equals("relative_op")){
            Node bool;
            result = boolExp();
        }
        Node stmt = createNode("statement", result);
        return createNode("statement", result);
    }
    // Boolean expression
    Node boolExp() throws IOException{
        Node node = null;
        //Check if relative operator
        if(getOpClass().equals("relative_op")){
            String opString;
            Node left;
            Node right;
            opString = lexScanner.getToken();
            lexScanner.nextToken();
            left = arithExp();
            lexScanner.nextToken();
            right = arithExp();
            node = createNode(opString,left,right);
        }else{
            //throw error
            error("Relative Operator", lexScanner.getToken());
        }
        return createNode("boolean_expression", node);
    }
    // Arithmetic expression
    Node arithExp() throws IOException{
        Node node;
        if(lexScanner.getToken().equals("id") | lexScanner.getToken().equals("integer_lt")){
            //make leaf node with token type and value
            node = createLeaf(lexScanner.getToken(), lexScanner.getLexeme());
        }else{
            //has to be binary expression
            node = binExp();
        }
        return createNode("arithmetic_expression", node);
    }
    // Binary Expression
    Node binExp() throws IOException{
        Node result = null;
        if(getOpClass().equals("arithmetic_op")){
            String opString;
            Node left, right;
            
            opString = lexScanner.getToken();
            lexScanner.nextToken();
            left = arithExp();
            lexScanner.nextToken();
            right = arithExp();
            result = createNode(opString, left, right);
        }else{
            error("Arrithmetic Operator", lexScanner.getToken());
        }
        return createNode("binary_expression", result);
    }
    
    
    // Formats error and throws exception
    void error(String expToken, String actToken){
        if(!(expToken.equals(actToken))){
            System.out.printf("Expecting %s received %s\n", expToken, actToken);
        }
    }
    // temporary print function
    void printTree(Node n){
        if(n != null){
            System.out.println(n.getNodeType());
            String value;
            if((value = n.getNodeValue()) != null){
                System.out.println(" " + value);
            }else{
                System.out.println();
                printTree(n.getLeftNode());
                printTree(n.getRightNode());
            }
        }
    }
    
    void printOutput(Node n){
        Queue<Node> q = new LinkedList<>();
        q.add(n);
        while(q.size() > 0){
            Node left, right;
            Node node = q.remove();
            left = node.getLeftNode();
            right = node.getRightNode();
            
            if(right != null){
                System.out.printf("<%s> -> <%s> <%s>\n", node.getNodeType(),left.getNodeType(),right.getNodeType());
                q.add(left);
                q.add(right);
            }else if(left != null){
                System.out.printf("<%s> -> <%s>\n",node.getNodeType() ,left.getNodeType());
                q.add(left);
            }
        }
        
    }
    
    
}
