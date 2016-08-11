/*
 * HelixInfo.java
 *
 * Created on April 11, 2003, 4:25 PM
 */

package rheat.base;

import java.util.*;

/**
 *
 * @author  jyzhang
 */
public class HelixInfo {

    private String fiveP = "";
    private String threeP = "";
    private int length;
    private int fivePrimeStart;
    private int threePrimeStart;
    private int fivePrimeEnd;
    private int threePrimeEnd;
    private double energy;

    public HelixInfo(Helix h, RNA rna) {
        int l = h.getLength();
        int x = h.getStartX();
        int y = h.getStartY();
        length = h.getLength();
        String seq = "";
        energy = h.getEnergy();
        fivePrimeEnd = y;
        for (int i = 0; i < l; i++) {
            fiveP = rna.getSequence().get(y) + fiveP;
            y--;
        }
        fivePrimeStart = y + 1;
        threePrimeStart = x;
        for (int i = 0; i < l; i++) {
            threeP = rna.getSequence().get(x) + threeP;
            x++;
        }
        threePrimeEnd = x - 1;
    }

    public String get5PrimeSequence() {
        return this.fiveP;
    }

    public String get3PrimeSequence() {
        return this.threeP;
    }

    public int getLength() {
        return this.length;
    }

    public int get5PrimeStart() {
        return this.fivePrimeStart;
    }

    public int get5PrimeEnd() {
        return this.fivePrimeEnd;
    }

    public int get3PrimeStart() {
        return this.threePrimeStart;
    }

    public int get3PrimeEnd() {
        return this.threePrimeEnd;
    }

    public double getEnergy() {
        return energy;
    }
}
