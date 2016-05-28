/*
 * Tester.java
 *
 * Created on February 28, 2003, 2:21 PM
 */

package rheat.test;

import rheat.base.*;
import rheat.filter.*;

import java.io.*;
import java.util.Date;

/** This is a class used for debugging and running tests only.  It will not appear
 * in the finished product.
 * @author Team MatriX
 */
public class Tester {
    
    /** Creates a new instance of Tester */
    public Tester() {
        
    }
    
    /** Runs the test.
     * @param args No arguments are required
     */
    public static void main(String[] args) {
        System.gc(); // garbage collection
        PrintStream bpdebug = System.out;
        PrintStream hxdebug = System.out;
        try {
            bpdebug = new PrintStream(new FileOutputStream("j:\\BPout.txt"));
            hxdebug = new PrintStream(new FileOutputStream("j:\\HXout.txt"));
        }
        catch (FileNotFoundException ex){
            ex.printStackTrace();
            System.exit(1);
        }
        
        rheat.base.Reader r = null;
        try {
            //r = new rheat.base.Reader("d.233.b.A.calcoaceticus.bpseq");
            r = new rheat.base.Reader("d.5.b.A.tumefaciens.bpseq");
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        RNA rna = r.readBPSEQ();
        
        //System.out.println(rna.toString());
        BPFilter basic = new BPFilter();
        Date t1 = new Date();
        long timeStampBeforeBasicFilter = t1.getTime();
        rna = basic.apply(rna);            
        Date t2 = new Date();
        long timeStampAfterBasicFilter = t2.getTime();
        long timeInBasicFilter = timeStampAfterBasicFilter - timeStampBeforeBasicFilter; 
        rna.outputBP(bpdebug);
        System.out.println("Time for Application of BASIC Filter: " + timeInBasicFilter + " milliseconds");

        Date t3 = new Date();
        long timeStampBeforeHelixFilter = t3.getTime();
        
/****************** Uncomment one of the following for application of the corresponding filter **********/

        AllHelicesFilter af = new AllHelicesFilter();
        af.setArguments();
        rna = af.apply(rna);              
        int NumOfHelices1 = rna.outputHelices(System.out);
                
        
        MaxMinFilter mmf = new MaxMinFilter();
        mmf.setArguments(2, 10);
        rna = mmf.apply(rna);
        
        EnergyMaxMinFilter emmf = new EnergyMaxMinFilter();
        emmf.setArguments(-50.00, 30.00);
        rna = emmf.apply(rna);
/**
        ExactLengthHelicesFilter elf = new ExactLengthHelicesFilter();
        elf.setArguments(3);
        rna = elf.apply(rna);       
        int NumOfHelices1 = rna.outputHelices(System.out);
**/                

        
/**
        BasePairRangeHelicesFilter bprf = new BasePairRangeHelicesFilter();
        bprf.setArguments(4);
        rna = bprf.apply(rna);       
**/ 
        

/**
        ExactLengthHelicesFilter elf = new ExactLengthHelicesFilter();
        elf.setArguments(3);
        rna = elf.apply(rna);       
**/                

/**
        AAandAGHelicesFilter aaagf = new AAandAGHelicesFilter();
        aaagf.setArguments();
        rna = aaagf.apply(rna);       
**/                


/**
        ELoopHelicesFilter eloopf = new ELoopHelicesFilter();
        eloopf.setArguments();
        rna = eloopf.apply(rna);       
**/                

/******************************************************************************************/        
        
        Date t4 = new Date();
        long timeStampAfterHelixFilter = t4.getTime();
        long timeInHelixFilter = timeStampAfterHelixFilter - timeStampBeforeHelixFilter; 
        int NumOfHelices = rna.outputHelices(System.out);
        System.out.println("Number of Helices Found under Given Conditions : " + NumOfHelices);
        System.out.println("Time for Application of HELIX Filter: " + timeInHelixFilter + " milliseconds");
        //iterator i = helixstore.iterator;    
    }   
}
