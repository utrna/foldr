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

public class ExactLengthHelicesFilter
extends rheat.filter.Filter {

    private int length = 0;    

    /**
     * Creates a new instance of the filter.
     */
    public ExactLengthHelicesFilter() {
    }

    /**
     * @param rna the input RNA
     * @return modified RNA after application of this filter
     */  
    public RNA apply(RNA rna) {
        MaxMinFilter mmf = new MaxMinFilter();
        mmf.setArguments(this.length, this.length);
        rna = mmf.apply(rna);              
        return rna;
    }

    /**
     * Sets arguments for the filter.
     * @param length the length of the helix
     */
    public void setArguments(int length) {
        this.length = length;
    }
}
