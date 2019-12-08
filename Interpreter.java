/*
 * Class:       CS4308 Section 2
 * Term:        Fall 2019
 * Name:        Patrick Sweeney, Christian Byrne, and Sagar Patel
 * Instructor:  Deepa Muralidhar
 * Project:     Deliverable 3 Interpreter - Java
 */

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

import java.util.Queue;
import java.util.Stack;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Interpreter{
    
    private Map<String,Integer> symbolTable = new HashMap<String, Integer>();
    
    Interpreter(String source) throws Exception {
        Map<String,Integer> symbolTable = new HashMap<String, Integer>();

        try {
            SynParser parser = new SynParser(source);
            SynParser.Node program = parser.program();
            interpret(program);
        } catch(Exception e) {
            throw e;
        }

    }

    void interpret(SynParser.Node program) {
        // in an error-free AST, only block nodes should be
        // present as children of the program, so no need to check

        ArrayList<SynParser.Node> children = program.getChildren();
        block(children.get(4));
    }

    void block(SynParser.Node block) {
        // in an error-free AST, only statement nodes, possibly followed 
        // by a block, should be present as children of the block so no 
        // need to check

        ArrayList<SynParser.Node> children = block.getChildren();
        statement(children.get(0));
        if (children.size() > 1) block(children.get(1));
    }

    void statement(SynParser.Node statement) {
        SynParser.Node child = statement.getChildren().get(0); 
        switch (child.getNodeType()) {
            case "print_statement":
                printStatement(child);
                break;
            case "assignment_statement":
                assignStatement(child);
                break;
            default:
                System.out.println(child.getNodeType() + " not currently implemented.");
                break;
        }
    }

    void printStatement(SynParser.Node print) {
        System.out.println(calculateValue(print.getChildren().get(2)));
    }
    
    void assignStatement(SynParser.Node assign){
        //Get variable name
        String var_name = assign.getChildren().get(0).getNodeValue();
        //Get value of arithmetic expression
        int value = calculateValue(assign.getChildren().get(2));
        //Update symbol table
        symbolTable.put(var_name, value);
    }

    //Calculates new values for assignment statements
    int calculateValue(SynParser.Node n){
        Queue<SynParser.Node> q = new LinkedList<>();
        ArrayList<SynParser.Node> children;
        int[] values = new int[2];
        int index = 0;
        int result;
        SynParser.Node node;
        String opType = null;
        
        //add children of the arithmetic node
        if (n.getNodeType().equals("arithmetic_expression")) {
            children = n.getChildren();
            if (children.size() > 0) {
                for (SynParser.Node x : children) {
                    q.add(x);
                }
            }
        }

        while(q.size() > 0){
            node = q.remove();

            // inspect node contents
            if(node.getNodeType().equals("arithmetic_op")){
                opType = node.getNodeValue();
            }else if(node.getNodeType().equals("identifier")){
                values[index++] = symbolTable.get(node.getNodeValue());
            }else if(node.getNodeType().equals("integer_lt")){
                values[index++] = Integer.parseInt(node.getNodeValue());
            // if node is a binary expression, add its children to the queue
            } else if(node.getNodeType().equals("binary_expression")) {
                children = node.getChildren();
                if (children.size() > 0) {
                    for (SynParser.Node x : children) {
                        q.add(x);
                    }
                }
            // this only leaves the possibility of the node being the root of 
            // an assignment statement. proceed to calculate its value.
            } else {
                values[index++] = calculateValue(node);
            }
        }

        if(opType != null){
            result = performCalc(opType, values, index);
        }else{
            result = values[0];
        }
        
        return result;
    }

    // Performs the appropriate arrithmetic calculation
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

    public static void main(String[] args) throws Exception {
        if (args.length <= 0) {
            throw new Exception("Argument not provided");
        }

        BufferedWriter bw = null; // writes to output file

        try {
            // Create an output file name and buffered writer to write to file
            String outFile = args[0] + "-julia_trace_file.txt";
            bw = new BufferedWriter((new FileWriter(outFile)));

            // Initialize Interpreter with filename of source code
            Interpreter interpreter = new Interpreter(args[0]);

            System.out.println();
            bw.close();
        } catch(FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage().split(" ")[0]);
        } catch(IOException i) {
            System.out.println("IO Exception: " + i.getMessage().split(" ")[0]);
            if(bw != null) {
                bw.close();
            }
        }
    }
}
