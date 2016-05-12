/*
 * Reader.java
 *
 * Created on February 14, 2003, 2:40 PM
 */

package rheat.base;

import rheat.test.RNA;

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
    
    /** Constructor for a Reader.
     * @param filename The name of the input file.
     */
    public Reader(String filename){
        FileInputStream fis;
        try {
            //fis = new FileInputStream("d:\\school\\bio337j\\rheat\\test\\" + filename);
            fis = new FileInputStream("j:\\Java Source\\rheat\\test\\" + filename);
            reader = new BufferedReader(new InputStreamReader(fis));
        }
        catch (FileNotFoundException ex){
            System.out.println("Error: File not found.");
        }
    }
    
    public Reader(File f){
        try {
            FileInputStream fis = new FileInputStream(f);
            reader = new BufferedReader(new InputStreamReader(fis));
        }
        catch (FileNotFoundException ex){
            throw new RuntimeException("Error: File not found.");
        }
    }
    
    /** Reads a BPSEQ file and outputs the RNA read.  It also reads some other
     * information contained in the BPSEQ file headers.
     * @return An RNA object from the BPSEQ file read.
     */
    public RNA readBPSEQ(){
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
            Thread t = new Thread(){
                public void run(){
                    try {
                        String temp = reader.readLine();
                        while(temp != "" && temp != null){
                            //System.out.println(temp);
                            StringTokenizer token = new StringTokenizer(temp);
                            int pos = Integer.parseInt(token.nextToken());
                            String nucleotide = token.nextToken();
                            int align = Integer.parseInt(token.nextToken());
                            if (align != 0){
                                Pair p = new Pair(pos, align);
                                realBP.add(p);
                                //System.out.println(p.toString());
                            }
                            //System.out.print(nucleotide);
                            alist.add(nucleotide);
                            temp = reader.readLine();
                        }
                    }
                    catch(IOException e){
                        //e.printStackTrace();
                        System.out.println();
                        System.out.println("End of File");
                    }
                }
            };
            t.start();
            t.join();
            RNA rna = new RNA(uid, org, acc, otherInfo, alist);
            boolean[][] actual = new boolean[alist.size()][alist.size()];
            Iterator itr = realBP.iterator();
            while (itr.hasNext()){
                Pair p = (Pair)itr.next();
                //System.out.println(p.toString());
                int i = p.getA();
                int j = p.getB();
                actual[i][j] = true;
            }
            rna.setActual(actual);
            return rna;
        }
        catch (IOException e){
            
        }
        catch(InterruptedException e){
            throw new RuntimeException("Error: Read thread is interrupted.");
        }
        catch(NumberFormatException e){
            throw new RuntimeException("Error: Wrong file format.");
        }
        catch(NoSuchElementException e){
            throw new RuntimeException("Error: Wrong file format.");
        }
        return null;
    }
    
    BufferedReader reader;
}

class Pair{
    
    public Pair(int i, int j){
        if (i < j){
            a = i;
            b = j;
        }
        else {
            a = j;
            b = i;
        }
    }
    
    public int getA(){
        return a;
    }
    
    public int getB(){
        return b;
    }
    
    public String toString(){
        return "" + a + " - " + b;
    }
    
    private int a;
    private int b;
}
