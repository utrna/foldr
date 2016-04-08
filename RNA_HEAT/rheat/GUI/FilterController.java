/*
 * FilterController.java
 *
 * Created on April 3, 2003, 5:30 PM
 */

package rheat.GUI;

import rheat.test.*;
import javax.swing.JFrame;
import javax.swing.JDialog;
/**
 *
 * @author  jyzhang
 */
public class FilterController {
    
    public static RNA rna;
    public static String description;
    public static boolean success;
    
    public static final int INTERACTIVE = 0;
    public static final int BATCH = 1;
    
    public static final int BASEPAIR = 0;
    public static final int HELIX = 1;
    public static final int DIAGONAL = 2;
    public static final int AA_AG = 3;
    public static final int ELOOP = 4;
    public static final int ENERGY = 5;
    public static final int COMPLEX = 6;
    
    /** Creates a new instance of FilterController */
    public FilterController() {
    }
    
    public static RNA showFilterDialog(RNA rna, RheatApp parent, int filter){
        JDialog dialog = null;
        success = false;
        description = "This filter does not have any user configurable options.";
        int mode = INTERACTIVE;
        switch (filter){
            case BASEPAIR:
                dialog = new BasepairFilterDialog(rna, mode , parent);
                break;
            case HELIX:
                dialog = new HelixFilterDialog(rna, mode, parent);
                break;
            case DIAGONAL:
                dialog = new DiagonalFilterDialog(rna, mode, parent);
                break;
            case AA_AG:
                dialog = new AAandAGFilterDialog(rna, mode, parent);
                break;
            case ELOOP:
                dialog = new ELoopFilterDialog(rna, mode, parent);
                break;
            case ENERGY:
                dialog = new EnergyFilterDialog(rna, mode, parent);
                break;
            case COMPLEX:
                dialog = new ComplexFilterDialog(rna, mode, parent);
                break;
        }
        if (dialog != null){
            java.awt.Point origin = parent.getCenteredOrigin(dialog);
            dialog.setLocation(origin);
            dialog.show();
        }
        return rna;
    }
    
    public static RNA showBatchFilterDialog(RNA rna, JFrame parent, int filter){
        return null;
    }
    
}
