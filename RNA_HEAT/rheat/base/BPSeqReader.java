/*
 * BPSeqReader.java
 *
 * Created on February 14, 2003, 2:40 PM
 */

package rheat.base;

import java.io.*;
import java.util.*;

/**
 * Reads a file in ".bpseq" format and creates an RNA object with the data.
 * @author Team MatriX
 */
public class BPSeqReader {

    /**
     * Convenience method to create a file-reader and then
     * call the other parse() method with that reader.
     * @param absPath the ".bpseq" file to open
     */
    static public RNA parse(String absPath) throws IOException {
        return parse(new BufferedReader(new FileReader(absPath)));
    }

    /**
     * Constructs RNA data from the given ".bpseq" file.
     * @param dataSource the source of data in ".bpseq" format
     * @return object representing data in file, or null on failure
     */
    static public RNA parse(BufferedReader dataSource) {
        RNA result = null;
        final ArrayList<String> alist = new ArrayList<String>();
        final ArrayList<SortedPair> realBP = new ArrayList<SortedPair>();
        final BufferedReader threadDataSource = dataSource;
        try {
            StringTokenizer token;
            int pos, align;
            String temp, nucleotide;
            // read the unique identifier string for this RNA.
            temp = dataSource.readLine();
            token = new StringTokenizer(temp, ":");
            token.nextToken();
            String uid = token.nextToken().substring(1);
            uid = uid.substring(0, uid.length() - 6);
            //System.out.println(uid);
            // read the organism for this RNA.
            temp = dataSource.readLine();
            token = new StringTokenizer(temp, ":");
            token.nextToken();
            String org = token.nextToken().substring(1);
            //System.out.println(org);
            // read the Accession number for this RNA
            temp = dataSource.readLine();
            token = new StringTokenizer(temp, ":");
            token.nextToken();
            String acc = token.nextToken().substring(1);
            //System.out.println(acc);
            // read misc information for this RNA
            String otherInfo = dataSource.readLine();
            Thread t = new Thread() {
                public void run() {
                    try {
                        String temp = threadDataSource.readLine();
                        while (temp != "" && temp != null) {
                            StringTokenizer token = new StringTokenizer(temp);
                            int pos = Integer.parseInt(token.nextToken());
                            String nucleotide = token.nextToken();
                            int align = Integer.parseInt(token.nextToken());
                            // 0 here indicates a "lack of pairing"; note
                            // that later, index 1 becomes 0 (zero-based)
                            if (align != 0) {
                                SortedPair p = new SortedPair(pos, align);
                                realBP.add(p);
                            }
                            alist.add(nucleotide);
                            temp = threadDataSource.readLine();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
            t.join();
            result = new RNA(uid, org, acc, otherInfo, alist);
            boolean[][] actual = new boolean[alist.size()][alist.size()];
            Iterator itr = realBP.iterator();
            while (itr.hasNext()) {
                SortedPair p = (SortedPair)itr.next();
                // convert to zero-based (to serve as array indices)
                int i = (p.getA() - 1);
                int j = (p.getB() - 1);
                //actual[i][j] = true;
                actual[j][i] = true;
            }
            result.setActual(actual);
        } catch (Exception e) {
            e.printStackTrace();
            result = null;
        }
        return result;
    }
}
