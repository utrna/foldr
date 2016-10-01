/*
 * SortedPair.java
 */

package rheat.base;

import java.io.*;
import java.util.*;

/**
 * Construct pairing with canonical ordering.
 */
public class SortedPair implements Serializable {

    public SortedPair() {
        setValues(0, 0);
    }

    public SortedPair(int i, int j) {
        setValues(i, j);
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    public int getLength() {
        return (b - a + 1);
    }

    public void setValues(int i, int j) {
        if (i < j) {
            a = i;
            b = j;
        } else {
            a = j;
            b = i;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof SortedPair) {
            SortedPair sp = (SortedPair)other;
            return ((sp.a == a) && (sp.b == b));
        }
        return super.equals(other);
    }

    @Override
    public int hashCode() {
        return (a | b);
    }

    @Override
    public String toString() {
        return "" + a + " - " + b;
    }

    private int a;
    private int b;
}
