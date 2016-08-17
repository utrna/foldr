/*
 * Filter.java
 *
 * Created on April 4, 2003, 2:43 PM
 */

package rheat.filter;

import rheat.base.RNA;

/**
 * Common base class for filter types.
 * @author  guru
 */
public abstract class Filter {

    /**
     * Modify the RNA data appropriately for the filter.
     * @return original object with modifications
     */
    public abstract RNA apply(RNA rna);

}
