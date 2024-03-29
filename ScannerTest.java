/*
 * Class:       CS4308 Section 2
 * Term:        Fall 2019
 * Name:        Patrick Sweeney, Christian Byrne, and Sagar Patel
 * Instructor:  Deepa Muralidhar
 * Project:     Deliverable 1 Lexical Scanner - Java
 */

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;

class ScannerTest{

    public static void main(String[] args) throws Exception{
        if (args.length <= 0) {
            throw new Exception("Argument not provided");
        }
        BufferedWriter bw = null; // writes to output file
        try {
            // Create an output file name and buffered writer to write to file
            String outFile = args[0] + "-scanner_trace_file.txt";
            bw = new BufferedWriter((new FileWriter(outFile)));
            // Initialize LexScanner with filename of source code
            LexScanner l = new LexScanner(args[0]);
            // Create headers for output file and console
            String pStr = "".format("\n%-10s%-17s%-15s%-10s%-10s\n", "Lexeme", "Token", "Token Code", "Line", "Position");
            System.out.print(pStr);
            bw.write(pStr);
            // Print token info to console and to file for each lexeme until the next Token is EOF (END OF FILE)
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
