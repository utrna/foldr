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
    static double INF = 10.0; // from original implementation; appears to be arbitrary
  
    public EnergyMaxMinFilter() {
        maxEnergy = Double.POSITIVE_INFINITY;
        minEnergy = Double.NEGATIVE_INFINITY;
        
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

    static public boolean isInfinite(double energy) {
        return (energy >= INF);
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

    @Override
    public void applyConstraint(RNA rna) {
        Iterator<Helix> i = rna.getPredictedHelices().iterator();
        while (i.hasNext()) {
            Helix h = i.next();
            h.setEnergy(findEnergy(h, rna));
        }
        rna.setBinMaxValue(maxEnergy);
        rna.setBinMinValue(minEnergy);
        rna.setBinCount(30); // arbitrary
        rna.setBinTag(Helix.InternalTags.TAG_MATCH_ENERGY); // also sets this tag on binned helices
        rna.processHelixBins();
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
        String threePrimeSequence;
        String fivePrimeSequence;
        int length;
        int i = 0;
        double result = 0;
        
        SortedPair range = new SortedPair();
        h.get5PrimeRange(range);
        fivePrimeSequence = rna.getSequenceInRange(range);
        h.get3PrimeRange(range);
        threePrimeSequence = new StringBuilder(rna.getSequenceInRange(range)).reverse().toString();
        length = h.getLength();
       
        while (i < (length-1)) {
            char first;     //1st letter of basepair
            char second;    //2nd letter of basepair
            
            int rowIndex;
            int colIndex;
            
            //Find row index
            first = fivePrimeSequence.charAt(i);
            second = threePrimeSequence.charAt(i);
            rowIndex = getBasepairIndex(first, second);
            
            //Find column index
            first = threePrimeSequence.charAt(i + 1);
            second = fivePrimeSequence.charAt(i + 1);
            colIndex = getBasepairIndex(first, second);
            
            //Get energy at (row, column), add to total
            result += getValueAt(rowIndex, colIndex);
            i++;
        }
        return result;
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
