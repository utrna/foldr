/*
 *
 * ELoopHelicesFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

/** This Filter outputs all E loop helices 
 */    



package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 *
 * @author  TEAM MATRIX
 */

public class ELoopHelicesFilter implements Filter {
        
    /** Creates a new instance of ELoopHelicesFilter */
    public ELoopHelicesFilter() {
    }
    
 /** 
  * @param rna The Input RNA
  * @return Modified RNA after application of this filter.    
  */  
  /* 
    public RNA apply(RNA rna) {
        ArrayList sequence = rna.getSequence();
        boolean[][] bp_temp = new boolean[sequence.size()][sequence.size()];            
        boolean[][] bp_master = new boolean[sequence.size()][sequence.size()];                    
        bp_master = rna.getBasePairs(); 
        
        // Creating a temp copy of base-pairs, to be used in the function
        for (int i = 0; i < rna.getBasePairs().length; ++i) {
            System.arraycopy(bp_master[i], 0, bp_temp[i], 0, rna.getBasePairs().length);                        
        }
        
        boolean containsAUG = false, containsAAG = false, isELoop = false, noException;
        String a = " ", b = " ", c = " ", d = " ";        
        HelixStore OldStore = rna.getHelices();        
        HelixStore hstore = new HelixGrid(sequence.size()); 
        //helices = rna.getHelices();
        int helixCount = -1;
        for (int i = 0; i< (sequence.size()-1); ++i){
            for (int j = 0; j < i; ++j){
                 isELoop = false;
                if (bp_temp[i][j] == true){
                    int temp_i = i;
                    int temp_j = j;
                    int helixLength = 0;
                    int startX = i;
                    int startY = j;
                    do{
                        bp_temp[temp_i][temp_j] = false;
                        ++temp_i; --temp_j; 
                        ++helixLength;
                        if (temp_i==sequence.size() || (temp_j<0))
                            break;
                    } while((bp_temp[temp_i][temp_j] == true));                                         
                    
//                    if ( ( sequence (startx to length) contains GAA ) || 
//                        ( sequence (startx to length) contains GAA ) ) 
                    
                noException = true;
                    try {
                    // Asuming startx-length-2 --- startx-length  is  3'----5'                    
                        a = ((String)sequence.get(startX - helixLength)).toLowerCase();
                        b = ((String)sequence.get(startX - helixLength - 1)).toLowerCase();
                        c = ((String)sequence.get(startX - helixLength - 2)).toLowerCase();
                        } catch (Exception e) { noException = false; }
                    
                        if (noException && a.equals("g") && b.equals("a") && c.equals("a") )
                            containsAAG = true;

                    noException = true;
                    try {
                    // Asuming starty+length --- starty+length+3  is  3'----5'
                        a = ((String)sequence.get(startY + helixLength)).toLowerCase();
                        b = ((String)sequence.get(startY + helixLength + 1)).toLowerCase();
                        c = ((String)sequence.get(startY + helixLength + 2)).toLowerCase();
                        } catch (Exception e) { noException = false; }
                        
                    if ( noException && a.equals("a") && b.equals("u") && c.equals("g") )
                            containsAUG = true;

                        
                    if (containsAUG && containsAAG)
                            isELoop = true;
                        
//the other end of the helix
                    containsAUG = false;
                    containsAAG = false;
                    
                    noException = true;
                    try {                    
                            // Asuming startx+1 --- startx+3  is  3'----5'                    
                            a = ((String)sequence.get(startX +1)).toLowerCase();
                            b = ((String)sequence.get(startX +2)).toLowerCase();
                            c = ((String)sequence.get(startX +3)).toLowerCase();
                            } catch (Exception e) { noException = false; }
                            
                    if (noException && a.equals("a") && b.equals("u") && c.equals("g") )
                                containsAUG = true;

                    noException = true;                    
                    try {                    
                            // Asuming starty-3 --- starty-1  is  3'----5'
                            a = ((String)sequence.get(startY - 3)).toLowerCase();
                            b = ((String)sequence.get(startY - 2)).toLowerCase();
                            c = ((String)sequence.get(startY - 1)).toLowerCase();
                            } catch (Exception e) { noException = false; }
                            if (noException && a.equals("a") && b.equals("a") && c.equals("g") )
                                containsAAG = true;                                                    

                    
                if (containsAUG && containsAAG)
                            isELoop = true;

            
                        if (isELoop){
                        Helix h = new Helix(startX, startY, helixLength);
                        if (OldStore.hasHelix(h)) {
                            ++helixCount;
                            hstore.addHelix(h);
                            }
                        }                                       
                    
                } //if
            }//j
        }//i        
        System.out.println("\nTotal Number of Helices: " + helixCount);
        rna.setHelices(hstore);
        System.gc();
        return rna;                
    } // apply
 
*/

    
    public RNA apply(RNA rna) {
        HelixStore hs = rna.getHelices();
        ArrayList sequence = rna.getSequence();        
        HelixGrid hg = new HelixGrid(rna.getSequence().size());
        boolean containsAUG = false, containsAAG = false, isELoop = false, noException;
        String a = " ", b = " ", c = " ", d = " ";              
        
        Iterator itr = hs.iterator();
        while(itr.hasNext()){
            isELoop = false;
            Helix h = (Helix)itr.next();
            HelixInfo hi = new HelixInfo(h, rna);                        
                      
            noException = true;
                    try {
                    // check for AAG goin into 3' start
                        a = ((String)sequence.get(hi.get3PrimeStart() + 1)).toLowerCase();
                        b = ((String)sequence.get(hi.get3PrimeStart() + 2)).toLowerCase();
                        c = ((String)sequence.get(hi.get3PrimeStart() + 3)).toLowerCase();
                        } catch (Exception e) { noException = false; }
                    
                        if (noException && a.equals("g") && b.equals("a") && c.equals("a") )
                            containsAAG = true;

                    noException = true;
                    try {
                    // check for AUG coming out of 5' Start                  
                        a = ((String)sequence.get(hi.get5PrimeStart() - 1)).toLowerCase();
                        b = ((String)sequence.get(hi.get5PrimeStart() - 2)).toLowerCase();
                        c = ((String)sequence.get(hi.get5PrimeStart() - 3)).toLowerCase();
                        } catch (Exception e) { noException = false; }
                        
                    if ( noException && a.equals("a") && b.equals("u") && c.equals("g") )
                            containsAUG = true;

                        
                    if (containsAUG && containsAAG)
                            isELoop = true;
                        
                    
                ////////the other end of the helix/////////////
                    containsAUG = false;
                    containsAAG = false;
                    
                    noException = true;
                    try {                    
                    // check for GAA coming out of 3' end
                            a = ((String)sequence.get(hi.get3PrimeEnd() - 1)).toLowerCase();
                            b = ((String)sequence.get(hi.get3PrimeEnd() - 2)).toLowerCase();
                            c = ((String)sequence.get(hi.get3PrimeEnd() - 3)).toLowerCase();
                            } catch (Exception e) { noException = false; }
                            
                    if (noException && a.equals("g") && b.equals("a") && c.equals("a") )
                                containsAAG = true;

                    noException = true;                    
                    try {                    
                    // check for AUG coming out of 5' end                            
                            a = ((String)sequence.get(hi.get5PrimeEnd() + 1)).toLowerCase();
                            b = ((String)sequence.get(hi.get5PrimeEnd() + 2)).toLowerCase();
                            c = ((String)sequence.get(hi.get5PrimeEnd() + 3)).toLowerCase();
                            } catch (Exception e) { noException = false; }
                            if (noException && a.equals("a") && b.equals("u") && c.equals("g") )
                                containsAUG = true;                                                    

                    
                if (containsAUG && containsAAG)
                            isELoop = true;
                        
            
            if (isELoop){
                hg.addHelix(h);
            }
                                    
        }
        rna.setHelices(hg);
        return rna;
    }
    
   
    
/** Sets Arguments for the Filter
 */    
    public void setArguments(){
    }
}
