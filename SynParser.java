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
import java.util.ArrayList; 

class SynParser {
    private LexScanner lexScanner;

    class Node {
        private Node parentNode;
        private ArrayList<Node> nodeList;
        private String nodeType;
        private String value;
        
        Node() {
            nodeList = new ArrayList<Node>();
            this.nodeType = null;
            this.value = null;
        }

        Node(String nodeType, String value, Node ... nodes) {
            nodeList = new ArrayList<Node>();
            this.nodeType = nodeType;
            this.value = value;

            addChild(nodes);
        }

        // add a single child to a list
        void addChild(Node child) {
            if (child != null) {
                child.setParent(this);
                nodeList.add(child);
            }
        }

        // add an array of children to the list.
        // array is expected to be in left-to-right order.
        void addChild(Node[] children) {
            for (Node x : children) { addChild(x); }
        }

        ArrayList<Node> getChildren() { return nodeList; }
        
        // set the parent reference of the node
        void setParent(Node parent) { this.parentNode = parent; }

        // get the parent reference of the node
        Node getParent() { return parentNode; }

        String getNodeType() {
            return this.nodeType;
        }
        
        String getNodeValue() {
            return this.value;
        }
        
        Node getLeftNode() {
            if (nodeList.size() > 0)
                return nodeList.get(0);
            else return null;
        }
        
        Node getRightNode() {
            if (nodeList.size() > 1)
                return nodeList.get(1);
            else return null;
        }
        
    }

    // Node creating functions
    Node createNode(String nodeType, Node ... nodes) {
        return new Node(nodeType, null, nodes);
    }
        
    Node createLeaf(String nodeType, String value) {
        return new Node(nodeType, value);
    }
    
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
    

    SynParser(String source) throws Exception {
        lexScanner = new LexScanner(source);
        parse();
    }
    
    //Drives the parsing process
    void parse() throws IOException {
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

/*
        if(lexScanner.getToken().equals("if_kw")) {
            Node bool;
            lexScanner.nextToken();
            result = boolExp();
        } else if(getOpClass().equals("relative_op")) {
            result = boolExp();
        } else if(lexScanner.getToken().equals("identifier")) {
            result = assignStatement();
        }
*/

        switch (lexScanner.getToken()) {
            case "if_kw":
            lexScanner.nextToken();
            result = boolExp();
            break;

            case "identifier":
            result = assignStatement();
            break;

            case "while_kw":
            lexScanner.nextToken();
            result = whileStatement();
            break;

            case "print_kw":
            lexScanner.nextToken();
            result = printStatement();
            break;

            case "for_kw":
            lexScanner.nextToken();
            result = forStatement();
            break;

            default:
            error("unknown", lexScanner.getToken());
            lexScanner.nextToken();
            break;
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
        } else {
            //throw error
            error("relative_op", lexScanner.getToken());
        }
        return createNode("boolean_expression", node);
    }

    // Arithmetic expression
    Node arithExp() throws IOException{
        Node node;
        if(lexScanner.getToken().equals("identifier") | lexScanner.getToken().equals("integer_lt")){
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
            error("arithmetic_op", lexScanner.getToken());
        }

        return createNode("binary_expression", result);
    }
    
    Node assignStatement() throws IOException{
        String idValue = lexScanner.getLexeme();
        Node idNode = createLeaf("identifier", idValue);
        lexScanner.nextToken();

        if(!getOpClass().equals("assignment_op")) {
            error("assignment_op", lexScanner.getToken());
        }

        lexScanner.nextToken();
        Node arithNode = arithExp();
        Node assignLit = createLeaf("assignment_operator", "=");
        return new Node("assignment_statement", null, idNode, assignLit, arithNode);
    }
    
    Node ifStatement() throws IOException{
        return boolExp();
    }

    Node whileStatement() throws IOException{
        Node bool = boolExp();
        lexScanner.nextToken();
        Node statement = statement();
        Node result = new Node("while_kw", "while", bool, statement);

        return createNode("while_statement", result);
    }
    
    Node forStatement() throws IOException{
        Node step = null;
        return createNode("for_statement", step);
    }

    Node printStatement() throws IOException{
        Node result = new Node("print_statement", null);
        Node step = null;

        result.addChild(new Node("print_kw", "print"));

        if (!lexScanner.getToken().equals("open_paren_lt"))
            error("open_paren_lt", lexScanner.getToken());

        result.addChild(new Node("open_paren_lt", "("));
        lexScanner.nextToken();
        step = arithExp();
        result.addChild(step);
        lexScanner.nextToken();

        if (!lexScanner.getToken().equals("close_paren_lt"))
            error("close_paren_lt", lexScanner.getToken());

        result.addChild(new Node("close_paren_lt", ")"));

        return result;
    }

    // Formats error and throws exception
    void error(String expToken, String actToken){
        if(!(expToken.equals(actToken))){
            System.out.printf("Expecting %s, received %s\n", expToken, actToken);
        }
    }
    
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
                System.out.println(whitespace.toString() + '+' + value);
            }else{
                //System.out.println();
                for (Node x : n.getChildren()) printTree(x, depth);
            }
        }
    }
    
    void printOutput(Node n){
        Queue<Node> q = new LinkedList<>();
        q.add(n);
        String printLits = "";

        ArrayList<Node> children;
        Node node;

        while(q.size() > 0) {
            node = q.remove();
            
            if (node != null) {
                children = node.getChildren();
            }
            
            if(node.getNodeValue() != null){
                printLits += "".format("<%s> -> %s\n", node.getNodeType(), node.getNodeValue());
            }
        }

        /*
        Node left, right;
        Node node;

        while(q.size() > 0){
            left = null;
            right = null;
            node = q.remove();

            if(node != null){
                left = node.getLeftNode();
                right = node.getRightNode();

                if(right != null){
                    if(node.getNodeType().equals("assignment_operator")) {
                        System.out.println("<assignment_operator> -> <eq_operator>");
                    } else {
                        System.out.printf("<%s> -> <%s> <%s>\n", node.getNodeType(),left.getNodeType(),right.getNodeType());
                    }

                    q.add(left);
                    q.add(right);
                } else if(left != null) {
                    if(node.getNodeType().equals("assignment_statement")) {
                        System.out.printf("<%s> -> %s <%s> <%s>\n", node.getNodeType(), left.getLeftNode().getNodeType(),left.getNodeType(), left.getRightNode().getNodeType());
                        q.add(left);
                        q.add(right);
                    } else {
                        System.out.printf("<%s> -> <%s>\n",node.getNodeType() ,left.getNodeType());
                        q.add(left);
                    }
                }

                if(node.getNodeValue() != null){
                    printLits += "".format("%s -> <%s>\n", node.getNodeValue(), node.getNodeType());
                }
            }
        }
        */

        System.out.println(printLits);
    }
}
