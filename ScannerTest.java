import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.io.FileOutputStream;

class ScannerTest{

    public static void main(String[] args) throws Exception{
        if (args.length <= 0) {
            throw new Exception("Argument not provided");
        }
        BufferedWriter bw = null; // writes to output file
        try {
            // Create an output file name and buffered writer to write to file
            String outFile = args[0] + "-trace_file.txt";
            bw = new BufferedWriter((new FileWriter(outFile)));
            // Initialize LexScanner with filename of source code
            LexScanner l = new LexScanner(args[0]);

        
            System.out.printf("\n%-10s%-17s%-15s%-10s%-10s\n", "Lexeme", "Token", "Token Code", "Line", "Position");
            String pStr = "";
            while(!(l.nextToken()).equals("EOF")){
                pStr = pStr.format("%-10s%-17s%-15s%-10s%-10s\n", l.getLexeme(), l.getToken(), l.getTokenCode(), l.getLine(), l.getPosition());
                System.out.print(pStr);
                bw.write(pStr);
            }
            System.out.println();
            bw.close();
        } catch(FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage().split(" ")[0]);
        } catch(IOException i){
            System.out.println("IO Exception: " + i.getMessage().split(" ")[0]);
            if(bw != null){
                bw.close();
            }
        }
    }
}