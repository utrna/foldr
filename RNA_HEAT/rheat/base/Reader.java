/*
 * Reader.java
 *
 * Created on February 14, 2003, 2:40 PM
 */

package rheat.base;

import java.io.FileNotFoundException;
import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import java.util.NoSuchElementException;
import java.util.ArrayList;
import java.util.Iterator;

/** Reader class is responsible for reading in a file of the correct format and then
 * outputing an RNA object.  It is the only recommended way to create an RNA
 * object.
 * @author Team MatriX
 */
public class Reader {

    public Reader(String absPath) throws IOException {
        FileInputStream fis;
        fis = new FileInputStream(absPath);
        reader = new BufferedReader(new InputStreamReader(fis));
    }

    /**
     * Constructs RNA data from a ".bpseq" file.
     * @return object representing data in file, or null on failure
     */
    public RNA readBPSEQ() {
        RNA result = null;
        final ArrayList<String> alist = new ArrayList<String>();
        final ArrayList<Pair> realBP = new ArrayList<Pair>();
        try {
            StringTokenizer token;
            int pos, align;
            String temp, nucleotide;
            // read the unique identifier string for this RNA.
            temp = reader.readLine();
            token = new StringTokenizer(temp, ":");
            token.nextToken();
            String uid = token.nextToken().substring(1);
            uid = uid.substring(0, uid.length() - 6);
            System.out.println(uid);
            // read the organism for this RNA.
            temp = reader.readLine();
            token = new StringTokenizer(temp, ":");
            token.nextToken();
            String org = token.nextToken().substring(1);
            System.out.println(org);
            // read the Accession number for this RNA
            temp = reader.readLine();
            token = new StringTokenizer(temp, ":");
            token.nextToken();
            String acc = token.nextToken().substring(1);
            System.out.println(acc);
            // read misc information for this RNA
            String otherInfo = reader.readLine();
            Thread t = new Thread() {
                public void run() {
                    try {
                        String temp = reader.readLine();
                        while (temp != "" && temp != null) {
                            StringTokenizer token = new StringTokenizer(temp);
                            int pos = Integer.parseInt(token.nextToken());
                            String nucleotide = token.nextToken();
                            int align = Integer.parseInt(token.nextToken());
                            // 0 here indicates a "lack of pairing"; note
                            // that later, index 1 becomes 0 (zero-based)
                            if (align != 0) {
                                Pair p = new Pair(pos, align);
                                realBP.add(p);
                            }
                            alist.add(nucleotide);
                            temp = reader.readLine();
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
                Pair p = (Pair)itr.next();
                // convert to zero-based (to serve as array indices)
                int i = (p.a - 1);
                int j = (p.b - 1);
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

    private BufferedReader reader;
}

/**
 * Construct pairing with canonical ordering.
 */
class Pair {

    public Pair(int i, int j) {
        setValues(i, j);
    }

    public void setValues(int i, int j) {
        if (i < j) {
            a = i;
            b = j;
        } else {
            a = j;
            b = i;
        }
    }

    public String toString() {
        return "" + a + " - " + b;
    }

    public int a;
    public int b;
}
