/*
 * Filter.java
 *
 * Created on April 4, 2003, 2:43 PM
 */

package rheat.filter;

import rheat.base.RNA;

/**
 *
 * @author  guru
 */
public interface Filter {
    
    public RNA apply(RNA rna);
}
