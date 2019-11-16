import java.util.Queue;
import java.util.Stack;
import java.util.LinkedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Interpreter{
    
    private Map<String,Integer> symbolTable = new HashMap<String, Integer>();
    
    Interpreter(){
        Map<String,Integer> symbolTable = new HashMap<String, Integer>();
    }
    
    
    
    
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
}