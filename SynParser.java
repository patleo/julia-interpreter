/*
 * Class:       CS4308 Section 2
 * Term:        Fall 2019
 * Name:        Patrick Sweeney, Christian Byrne, and Sagar Patel
 * Instructor:  Deepa Muralidhar
 * Project:     Deliverable 2 Parser - Java
 */

class SynParser {
    private LexScanner lexScanner;

    class Node {
        private Node parentNode;
        private Node leftNode;
        private Node rightNode;
    }

    SynParser(String source) throws Exception {
        lexScanner = new LexScanner(source);
    }
}
