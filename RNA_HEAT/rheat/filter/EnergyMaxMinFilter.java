/*
 * EnergyMaxMinFilter.java
 *
 * Created on April 11, 2003, 2:57 PM
 */

package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;
import java.lang.Object;


/**
 * Calculates helix energy values.
 *
 * @author  Team MatriX
 */
public class EnergyMaxMinFilter
extends rheat.filter.Filter {

    double minEnergy;
    double maxEnergy;
    double[][] EnergyData;
    double INF;
  
    public EnergyMaxMinFilter() {
        maxEnergy = Double.POSITIVE_INFINITY;
        minEnergy = Double.NEGATIVE_INFINITY;
        
        INF = 10.0;
        
        double[][] DefaultEnergyData = {
           // AA     AC     AG     AU     CA     CC     CG     CU     GA     GC     GG     GU     UA     UC     UG     UU
   /*AA*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF},
   /*AC*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF},
   /*AG*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF},
   /*AU*/   { INF ,  INF ,  INF , -1.10,  INF ,  INF , -2.10,  INF ,  INF , -2.20,  INF , -1.40, -0.90,  INF , -0.60,  INF},
   /*CA*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF},
   /*CC*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF},
   /*CG*/   { INF ,  INF ,  INF , -2.10,  INF ,  INF , -2.40,  INF ,  INF , -3.30,  INF , -2.10, -2.10,  INF , -1.40,  INF},
   /*CU*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF},
   /*GA*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF},
   /*GC*/   { INF ,  INF ,  INF , -2.20,  INF ,  INF , -3.30,  INF ,  INF , -3.40,  INF , -2.50, -2.40,  INF , -1.50,  INF},
   /*GG*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,   INF , INF},
   /*GU*/   { INF ,  INF ,  INF , -1.40,  INF ,  INF , -2.10,  INF ,  INF , -2.50,  INF ,  1.30, -1.30,  INF , -0.50,  INF},
   /*UA*/   { INF ,  INF ,  INF , -0.90,  INF ,  INF , -2.10,  INF ,  INF , -2.40,  INF , -1.30, -1.30,  INF , -1.00,  INF},
   /*UC*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF},
   /*UG*/   { INF ,  INF ,  INF , -0.60,  INF ,  INF , -1.40,  INF ,  INF , -1.50,  INF , -0.50, -1.00,  INF ,  0.30,  INF},
   /*UU*/   { INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF ,  INF},
        };
        
        EnergyData = DefaultEnergyData;
    }    

    public double getMinEnergy() {
        return this.minEnergy;
    }

    public double getMaxEnergy() {
        return this.maxEnergy;
    }

    /**
     * Short-cut to see if a helix has matched ANY energy constraint
     * (that is, if its energy is in the maximum/minimum range of
     * some constraint).
     */
    static public boolean appliedToHelix(Helix h) {
        return h.hasTag(Helix.InternalTags.TAG_MATCH_ENERGY);
    }

    /** for each helix calculate energy and see if in range, if yes keep **/
    @Override
    public void applyConstraint(RNA rna) {
        String constraintDesc = new String(getMaxEnergy() + ":" + getMinEnergy());
        ArrayList sequence = rna.getSequence();
        Iterator i = rna.getHelices().iterator();
        while(i.hasNext())
        {
            Helix h = (Helix)i.next();
            double HelixEnergy = findEnergy(h, rna);
            h.setEnergy(HelixEnergy);
            final int numBins = 30; // arbitrary (FIXME: make customizable)
            int binNumber = AppMain.selectBin(HelixEnergy, numBins, minEnergy, maxEnergy);
            if (binNumber != -1) {
                h.addTag(Helix.InternalTags.TAG_MATCH_ENERGY, constraintDesc);
                h.setBinNumber(binNumber); // may be -1 (no bin)
            } else {
                h.removeTag(Helix.InternalTags.TAG_MATCH_ENERGY);
            }
        }
    }

    @Override
    public void removeConstraint(RNA rna) {
        removeTagAllPredictedHelices(rna, Helix.InternalTags.TAG_MATCH_ENERGY);
    }

    private double getValueAt(int row, int col) {
        return EnergyData[row][col];
    }   

    private int getBasepairIndex(char first, char second) {
        String basepair = ("" + first + second).toLowerCase();
        
        String BasePairs[] = { "aa", "ac", "ag", "au", "ca", "cc", "cg", "cu", 
                               "ga", "gc", "gg", "gu", "ua", "uc", "ug", "uu"} ;
                               
        int i = 0;
        
        // i shouldn't go above 15
        while ( !(basepair.equals( BasePairs[i]) ) )
            i++;

        return i;
    }

    private double findEnergy(Helix h, RNA rna)
    {
        HelixInfo hinfo = new HelixInfo(h, rna);
        
        String ThreePrimeSequence;
        String FivePrimeSequence;
        int length;
        int i = 0;
        double HelixEnergy = 0;
        
        FivePrimeSequence = hinfo.get5PrimeSequence();
        ThreePrimeSequence = hinfo.get3PrimeSequence();
        length = hinfo.getLength();
        
        while( i < (length-1) )
        {   
            char first;     //1st letter of basepair
            char second;    //2nd letter of basepair
            
            int rowIndex;
            int colIndex;
            
            //Find row index
            first = FivePrimeSequence.charAt(i);
            second = ThreePrimeSequence.charAt(i);
            
            rowIndex = getBasepairIndex(first, second);

            //Find column index
            first = ThreePrimeSequence.charAt(i+1);
            second = FivePrimeSequence.charAt(i+1);
            colIndex = getBasepairIndex(first, second);
            
            //Get energy at (row, column), add to total
            HelixEnergy += getValueAt(rowIndex, colIndex);            
            i++;
        }        
        return HelixEnergy;        
    }

/** Sets Arguments for the Filter
 * @param min The Minimum Energy of Helix
 * @param max The Maximum Energy of Helix
 */    
    public void setArguments(double max, double min) {
        maxEnergy = max;
        minEnergy = min;
    }    

    /** Sets Arguments for the Filter
 * @param min The Minimum Energy of Helix
 * @param max The Maximum Energy of Helix
 * @param UserEnergyData Custom Energy Data
 */    
    public void setArguments(double max, double min, double[][] UserEnergyData) {
        maxEnergy = max;
        minEnergy = min;
        
        EnergyData = UserEnergyData;
    }    

}
