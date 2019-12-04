/*
 * Class:       CS4308 Section 2
 * Term:        Fall 2019
 * Name:        Patrick Sweeney, Christian Byrne, and Sagar Patel
 * Instructor:  Deepa Muralidhar
 * Project:     Deliverable 2 Parser - Java
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
        // in an error-free AST, only statement nodes should be
        // present as children of the program, so no need to check

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
        
        q.add(n);
        while(q.size() > 0){
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
                for (SynParser.Node x : children) {
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
