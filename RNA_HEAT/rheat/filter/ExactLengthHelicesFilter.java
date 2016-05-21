/*
 * ExactLengthHelicesFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

/** This Filter outputs helices of the length specified
  */    

     


package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 *
 * @author  TEAM MATRIX
 */

public class ExactLengthHelicesFilter implements Filter {
    
    private int Length;    
    /** Creates a new instance of ExactLengthHelicesFilter */
    public ExactLengthHelicesFilter() {
    }
    
 /** 
  * @param rna The Input RNA
  * @return Modified RNA after application of this filter.    
  */  
    public RNA apply(RNA rna) {
        MaxMinFilter mmf = new MaxMinFilter();
        mmf.setArguments(Length, Length);
        rna = mmf.apply(rna);              
        return rna;
    } // apply
 
    
/** Sets Arguments for the Filter
 * @param Length The Length of Helix
 */    
    public void setArguments(int length){
        Length = length;
    }
}
