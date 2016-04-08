/*
 * HelixFilter.java
 *
 * Created on February 24, 2003, 3:01 PM
 */

package rheat.test;
import java.util.ArrayList;
/** This filter adds Helices to an RNA.  Currently this Filter only finds all
 * helices (length 1 or more), in antiparallel orientation only.
 * @author Team MatriX
 */
public class HelixFilter {

    private RNA rna;
    private int FilterType;
//    private int[] FilterParameters;

    
    /** Creates a new instance of HelixFilter given an RNA.
     * @param inputRna The RNA object to apply the Filter on.
     */
    public HelixFilter(RNA inputRna) {
        rna = inputRna;
    }
    
    /** Sets the type of HelixFilter to be applied on the given NA.
     * @param filtertype The Type (Name) of Filter to be applied. 
     */
    public void SetFilterType(int filtertype) {
        FilterType = filtertype;     
    }
        
                
} //class
    
    
/****************** GARBAGE -- BUT DON't DELETE THIS. MIGHT BE USEFUL LATER ************/
/************* CHECKS FOR NEUCLEOTIDE PATTERN INSIDE HELICES ***************************/
        
    /** This method outputs all internal loop helices 
     *  Incorrect implementation -- But can be useful later.
     * @return Modified RNA after application of this filter.
     */

/*    public RNA InternalLoopHelicesFilterBackup(){
        ArrayList sequence = rna.getSequence();
        boolean[][] bp_temp = new boolean[sequence.size()][sequence.size()];
        bp_temp = rna.getBasePairs(); //not sure if handle or actual
        HelixStore hstore = new HelixGrid(sequence.size()); 
        //helices = rna.getHelices();
        int helixCount = -1;
        for (int i = 0; i< (sequence.size()-1); ++i){
            for (int j = 0; j < i; ++j){
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
                
                    // Asuming startx --- startx+length  is  5'----3'                    
                    boolean containsGAA = false;
                    for (int k = startX; k < (startX + helixLength - 3) ; ++k) {
                        String a = ((String)sequence.get(i)).toLowerCase();
                        String b = ((String)sequence.get(i+1)).toLowerCase();
                        String c = ((String)sequence.get(i+2)).toLowerCase();
                        if ( a.equals("g") && b.equals("a") && c.equals("a") )
                            containsGAA = true;
                    }

                    // Asuming startx --- starty+length  is  5'----3'
                    for (int k = startY; k < (startY + helixLength - 3) ; ++k) {
                        String a = ((String)sequence.get(i)).toLowerCase();
                        String b = ((String)sequence.get(i+1)).toLowerCase();
                        String c = ((String)sequence.get(i+2)).toLowerCase();
                        if ( a.equals("g") && b.equals("a") && c.equals("a") )
                            containsGAA = true;
                    }
                                     
                    if (containsGAA == true)
                    {
                        ++helixCount;
                        hstore.addHelix(new Helix(startX, startY, helixLength));
                    }
                }
            }//j
        }//i        
        System.out.println("\nTotal Number of Helices: " + helixCount);
        rna.setHelices(hstore);
        System.gc();
        return rna;                
    }// method

*/

        
    /** This method outputs all multistem loop helices 
     * @return Modified RNA after application of this filter.
     */
 
/*    public RNA MultiStemLoopHelicesFilter(){
        ArrayList sequence = rna.getSequence();
        boolean[][] bp_temp = new boolean[sequence.size()][sequence.size()];
        bp_temp = rna.getBasePairs(); //not sure if handle or actual
        HelixStore hstore = new HelixGrid(sequence.size()); 
        //helices = rna.getHelices();
        int helixCount = -1;
        for (int i = 0; i< (sequence.size()-1); ++i){
            for (int j = 0; j < i; ++j){
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
                

                    try {
                    boolean containsAUG = false;
                    // Asuming startx-3 --- startx-1  is  3'----5'                    
                        String a = ((String)sequence.get(startX - 1)).toLowerCase();
                        String b = ((String)sequence.get(startX - 2)).toLowerCase();
                        String c = ((String)sequence.get(startX - 3)).toLowerCase();
                        if ( a.equals("a") && b.equals("u") && c.equals("g") )
                            containsAUG = true;

                    // Asuming starty --- starty+3  is  3'----5'
                        a = ((String)sequence.get(startY + 1)).toLowerCase();
                        b = ((String)sequence.get(startY + 2)).toLowerCase();
                        c = ((String)sequence.get(startY + 3)).toLowerCase();
                        if ( a.equals("a") && b.equals("u") && c.equals("g") )
                            containsAUG = true;

//the other end of the helix

                        if ( (startX + helixLength + 2) <= (startY - helixLength) ) {                         
                            // Asuming startx + helixLength --- startx + helixLength +2  is  3'----5'                    
                            a = ((String)sequence.get(startX + helixLength)).toLowerCase();
                            b = ((String)sequence.get(startX + helixLength + 1)).toLowerCase();
                            c = ((String)sequence.get(startX + helixLength + 2)).toLowerCase();
                            if ( a.equals("a") && b.equals("u") && c.equals("g") )
                                containsAUG = true;

                            // Asuming starty-helixLength-2 --- starty-helixLength  is  3'----5'
                            a = ((String)sequence.get(startY - helixLength - 2)).toLowerCase();
                            b = ((String)sequence.get(startY - helixLength - 1)).toLowerCase();
                            c = ((String)sequence.get(startY - helixLength)).toLowerCase();
                            if ( a.equals("a") && b.equals("u") && c.equals("g") )
                                containsAUG = true;
                            }                        
                        
                        if (containsAUG == true){
                            ++helixCount;
                            hstore.addHelix(new Helix(startX, startY, helixLength));
                        }
                    } catch (Exception e) { }
                    
                }
            }//j
        }//i        
        System.out.println("\nTotal Number of Helices: " + helixCount);
        rna.setHelices(hstore);
        System.gc();
        return rna;                
    }// method
*/
    
        
    /** This method outputs all Internal loop helices 
     * @return Modified RNA after application of this filter.
     */
/*    public RNA InternalLoopHelicesFilter(){
        ArrayList sequence = rna.getSequence();
        boolean[][] bp_temp = new boolean[sequence.size()][sequence.size()];
        bp_temp = rna.getBasePairs(); //not sure if handle or actual
        HelixStore hstore = new HelixGrid(sequence.size()); 
        //helices = rna.getHelices();
        int helixCount = -1;
        for (int i = 0; i< (sequence.size()-1); ++i){
            for (int j = 0; j < i; ++j){
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
                

                    try {
                    boolean containsAAG = false;
                    // Asuming startx-3 --- startx-1  is  3'----5'                    
                        String a = ((String)sequence.get(startX - 1)).toLowerCase();
                        String b = ((String)sequence.get(startX - 2)).toLowerCase();
                        String c = ((String)sequence.get(startX - 3)).toLowerCase();
                        if ( a.equals("a") && b.equals("a") && c.equals("g") )
                            containsAAG = true;

                    // Asuming starty --- starty+3  is  3'----5'
                        a = ((String)sequence.get(startY + 1)).toLowerCase();
                        b = ((String)sequence.get(startY + 2)).toLowerCase();
                        c = ((String)sequence.get(startY + 3)).toLowerCase();
                        if ( a.equals("a") && b.equals("a") && c.equals("g") )
                            containsAAG = true;

//the other end of the helix

                        if ( (startX + helixLength + 2) <= (startY - helixLength) ) {                         
                            // Asuming startx + helixLength --- startx + helixLength +2  is  3'----5'                    
                            a = ((String)sequence.get(startX + helixLength)).toLowerCase();
                            b = ((String)sequence.get(startX + helixLength + 1)).toLowerCase();
                            c = ((String)sequence.get(startX + helixLength + 2)).toLowerCase();
                            if ( a.equals("a") && b.equals("a") && c.equals("g") )
                                containsAAG = true;

                            // Asuming starty-helixLength-2 --- starty-helixLength  is  3'----5'
                            a = ((String)sequence.get(startY - helixLength - 2)).toLowerCase();
                            b = ((String)sequence.get(startY - helixLength - 1)).toLowerCase();
                            c = ((String)sequence.get(startY - helixLength)).toLowerCase();
                            if ( a.equals("a") && b.equals("a") && c.equals("g") )
                                containsAAG = true;
                            }                        
                        
                        if (containsAAG == true){
                            ++helixCount;
                            hstore.addHelix(new Helix(startX, startY, helixLength));
                        }
                    } catch (Exception e) { }
                    
                }
            }//j
        }//i        
        System.out.println("\nTotal Number of Helices: " + helixCount);
        rna.setHelices(hstore);
        System.gc();
        return rna;                
    }// method

*/        


