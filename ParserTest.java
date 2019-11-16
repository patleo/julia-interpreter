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

class ParserTest {
    public static void main(String[] args) throws Exception {
        if (args.length <= 0) {
            throw new Exception("Argument not provided");
        }

        BufferedWriter bw = null; // writes to output file

        try {
            // Create an output file name and buffered writer to write to file
            String outFile = args[0] + "-parser_trace_file.txt";
            bw = new BufferedWriter((new FileWriter(outFile)));
            // Initialize SynParser with filename of source code
            SynParser parser = new SynParser(args[0]);
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
