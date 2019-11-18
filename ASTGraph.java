import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.LinkedList;

class ASTGraph{
    private Map<String,gVizNode> nodeList = new HashMap<String,gVizNode>();
    private String labels;
    private String edges;
    private String nonTerms;
    private ArrayList<String> subgraphs;
    
    class gVizNode{
        private String gName;
        private String tokenType;
        private String value;
        private ArrayList<gVizNode> children;
        
        gVizNode(SynParser.Node n){
            this.tokenType = n.getNodeType();
            addGName();
            this.value = n.getNodeValue() != null ? n.getNodeValue() : null;
            this.children = new ArrayList<gVizNode>();
        }
        
        private void addGName(){
            int index = 0;
            String tempName = "".format("%s%d", this.tokenType, index);
            while(nodeList.containsKey(tempName)){
                index++;
                tempName = "".format("%s%d", this.tokenType, index);
            }
            this.gName = tempName;
            nodeList.put(this.gName, this);
        }
        
        public void addChild(gVizNode gv){
            this.children.add(gv);
        }
        
        public ArrayList<gVizNode> getChildren() { return this.children; };
        
        public String getGName(){
            return this.gName;
        }
        public String getToken(){
            return this.tokenType;
        }
        public String getValue(){
            return this.value;
        }
        
        public String formatToken(){
            String fToken = "";
            switch(this.tokenType){
                case "assignment_statement":
                    fToken = "assignment_\nstatement";
                    break;
                case "arithmetic_expression":
                    fToken = "arithmetic_\nexpression";
                    break;
                case "binary_expression":
                    fToken = "binary_\nexpression";
                    break;
                case "print_statement":
                    fToken = "print_\nstatement";
                    break;
                default:
                    fToken = this.tokenType;
                    break;
            }
            return fToken;
        }
        
        @Override
        public String toString(){
            String nodeLabel = null;
            String token = formatToken();
            if(value != null){
                nodeLabel = String.format("%s[label=\"%s\\nvalue=\'%s\'\"]",this.gName, token, this.value);
            }else{
                nodeLabel = String.format("%s[label=\"%s\"]",this.gName, token);
            }
            return nodeLabel;
        }
    }
    
    ASTGraph(SynParser.Node n){
        this.labels = this.edges = "";
        this.subgraphs = new ArrayList<String>();
        this.nonTerms = "";
        createGVNodes(n);
        String graph = "digraph x {\n\tgraph [ordering=\"out\"];\n\trankdir=UD;\n\t";
        graph += "".format("node [size = 5 fixedsize = true shape = doublecircle color = red fontsize = 5.5  fontname = \"times-bold\"];\n\t%s\n\t",this.nonTerms);
        graph += "node [size = 5 fixedsize = true shape = circle color = black fontsize = 5.5 fontname = \"times-bold\"];\n";
        System.out.println(graph);
        for(String x : this.subgraphs){
            System.out.println(x);
        }
        System.out.println(this.labels);
        System.out.println(this.edges);
        System.out.println("}");
    }
    
    gVizNode createGVNodes(SynParser.Node n){
        if(n == null){
            return null;
        }else{
            gVizNode gv = new gVizNode(n);
            this.labels += gv + "\n";
            String childGName = "";
            for(SynParser.Node x : n.getChildren()){
                gVizNode child = createGVNodes(x);
                gv.addChild(child);
                this.edges += "".format("%s -> %s\n",gv.getGName(), child.getGName());
            }
            if (gv.getToken().equals("statement")){
                createSubgraphs(gv);
            }
            return gv;
        }
    }
    
    void createSubgraphs(gVizNode gv){
        String sg = "".format("subgraph cluster%d{\n\tsubgraph cluster%d0{\n\t\t", this.subgraphs.size(), this.subgraphs.size());
        String lt = "";
        LinkedList<gVizNode> queue = new LinkedList<gVizNode>();
        queue.add(gv);
        while(queue.size() > 0){
            gVizNode temp = queue.pop();
            sg += temp.getGName() + " ";
            if(temp.getValue() != null){
                lt += temp.getValue() + " ";
                this.nonTerms += temp.getGName() + " ";
            }
            for(gVizNode x : temp.getChildren()){
                queue.add(x);
            }
        }
        sg += "".format("\n\t\tlabel = \"%s\"\n\t\tlabelloc=b;\n\t\tordering=\"out\";\n\t}\n}\n", lt);
        this.subgraphs.add(sg);
    }
    
    
    public static void main(String[] args) throws Exception {
        SynParser function = new SynParser(args[0]);
        SynParser.Node funcNode = function.parse();
        ASTGraph aGraph = new ASTGraph(funcNode);
    }
    
}