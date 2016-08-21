/*
 * DiagonalDistanceFilter.java
 *
 * Created on April 4, 2003, 2:44 PM
 */

package rheat.filter;

import rheat.base.*;

import java.util.Iterator;

/**
 * Finds helices whose line distance from the diagonal is
 * in the given range (or NOT in the range, if INVERTED).
 * @author  TEAM MATRIX
 */
public class DiagonalDistanceFilter
extends rheat.filter.Filter {

    public enum Mode {
        NORMAL, // helices match if their diagonal distance is in the range
        INVERTED // helices match if their diagonal distance is NOT in the range
    }

    public DiagonalDistanceFilter() {
        this(Mode.NORMAL);
    }

    public DiagonalDistanceFilter(Mode mode) {
        isInverted = (mode == Mode.INVERTED);
        rangeMax = Integer.MAX_VALUE;
        rangeMin = 0;
    }

    public int getRangeMax() {
        return rangeMax;
    }

    public int getRangeMin() {
        return rangeMin;
    }

    @Override
    public void applyConstraint(RNA rna) {
        String constraintDesc = String.format("%.2f:%.2f", getRangeMax(), getRangeMin());
        Iterator itr = rna.getHelices().iterator();
        while (itr.hasNext()) {
            Helix h = (Helix)itr.next();
            final boolean inRange = (((h.getStartX() - h.getStartY()) <= rangeMax) &&
                                     ((h.getStartX() - h.getStartY()) >= rangeMin));
            if (isInverted) {
                // matches if NOT in the given range
                if (inRange) {
                    h.addTag(Helix.InternalTags.TAG_MATCH_NON_DIAGONAL_DISTANCE, constraintDesc);
                } else {
                    h.removeTag(Helix.InternalTags.TAG_MATCH_NON_DIAGONAL_DISTANCE);
                }
            } else {
                // matches if in the given range
                if (inRange) {
                    h.addTag(Helix.InternalTags.TAG_MATCH_DIAGONAL_DISTANCE, constraintDesc);
                } else {
                    h.removeTag(Helix.InternalTags.TAG_MATCH_DIAGONAL_DISTANCE);
                }
            }
        }
    }    

    @Override
    public void removeConstraint(RNA rna) {
        if (isInverted) {
            removeTagAllPredictedHelices(rna, Helix.InternalTags.TAG_MATCH_NON_DIAGONAL_DISTANCE);
        } else {
            removeTagAllPredictedHelices(rna, Helix.InternalTags.TAG_MATCH_DIAGONAL_DISTANCE);
        }
    }

    /**
     * Sets arguments for the filter.
     * @param rangeMax The maximum distance (range) between both
     * positions that are Base Paired.
     * @param rangeMin The minimum distance.
     */
    public void setArguments(int rangeMax, int rangeMin) {
        this.rangeMax = rangeMax;
        this.rangeMin = rangeMin;
    }

    private boolean isInverted;
    private int rangeMin;
    private int rangeMax;

}
