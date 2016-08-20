/*
 * ComplexFilter.java
 *
 * Created on May 1, 2003, 2:44 PM
 */

package rheat.filter;

import rheat.base.*;

import java.util.Iterator;
import java.util.ArrayList;

/**
 * Finds helices within a particular complex distance
 * or simple distance.
 *
 * @author  TEAM MATRIX
 */
public class ComplexFilter
extends rheat.filter.Filter {

    public ComplexFilter() {
        ComplexDistance = Integer.MAX_VALUE;
        SimpleDistance = Integer.MAX_VALUE;
    }

    public int getComplexDistance() {
        return this.ComplexDistance;
    }

    public int getSimpleDistance() {
        return this.SimpleDistance;
    }

    public void applyConstraint(RNA rna) {

        this.r = rna;

///////Creating a Diagonal set of helices        
        DiagonalDistanceFilter bprf = new DiagonalDistanceFilter();
        bprf.setArguments(0, SimpleDistance);
        bprf.applyConstraint(rna);
        
        HelixStore hsDiagonal = rna.getHelices();
        //HelixStore hsSorted = SortHelicesByEnergy(hsDiagonal); //james
        HelixStore hsNonIntersecting = pickNonIntersectingDiagonals(hsDiagonal);
        
///////Creating a Non-Diagonal set of helices
//        RNA rna_non_diagonal = rna;
        // TODO: not clear why all helices are restored here
        // (could the steps above have operated on any RNA, even a copy?)
        rna.resetPredictedHelices();
        
        DiagonalDistanceFilter ndf = new DiagonalDistanceFilter(DiagonalDistanceFilter.Mode.INVERTED);
        ndf.setArguments(0, SimpleDistance);
        ndf.applyConstraint(rna);
//////////        

        HelixStore hsNonDiagonal = rna.getHelices();
        Iterator itr = hsNonDiagonal.iterator();
        String constraintDesc = new String(getComplexDistance() + "," + getSimpleDistance());
        while(itr.hasNext()){
            Helix h = (Helix)itr.next();
            if (complexDistance(h, hsNonIntersecting) <= ComplexDistance){
                h.addTag(Helix.InternalTags.TAG_MATCH_COMPLEX_DISTANCE, constraintDesc);
            } else {
                h.removeTag(Helix.InternalTags.TAG_MATCH_COMPLEX_DISTANCE);
            }
        }
        
        //add the diagonal helices into the store
        Iterator newitr = hsNonIntersecting.iterator();
        while(newitr.hasNext()) {
            Helix h = (Helix)newitr.next();
            h.addTag(Helix.InternalTags.TAG_MATCH_COMPLEX_DISTANCE, null);
        }
        //HelixStore newhg = pickNonIntersectingDiagonals(hg);
    }

    @Override
    public void removeConstraint(RNA rna) {
        removeTagAllPredictedHelices(rna, Helix.InternalTags.TAG_MATCH_COMPLEX_DISTANCE);
    }

    private boolean noninterfering(HelixInfo h1, HelixInfo h2){
        if (h1.get5PrimeStart() >= h2.get5PrimeStart() && h1.get5PrimeStart() <= h2.get5PrimeEnd()){
            return false;
        }
        if (h1.get5PrimeEnd() >= h2.get5PrimeStart() && h1.get5PrimeEnd() <= h2.get5PrimeEnd()){
            return false;
        }
        if (h1.get3PrimeEnd() <= h2.get3PrimeEnd() && h1.get3PrimeEnd() >= h2.get3PrimeStart()){
            return false;
        }
        if (h1.get3PrimeStart() <= h2.get3PrimeEnd() && h1.get3PrimeStart() >= h2.get3PrimeStart()){
            return false;
        }
        return true;
    }

    private HelixStore pickNonIntersectingDiagonals(HelixStore hs){
        HelixGrid result = new HelixGrid(hs.getSequenceLength());
        if (hs.isEmpty()) {
            return result;
        }
        ArrayList<Helix> helixList = new ArrayList<Helix>(hs.getHelixCount());
        Iterator itr = hs.iterator();
        while (itr.hasNext()){
            Helix h = (Helix)itr.next();
            if (h.getEnergy() < 0){
                helixList.add(h);
            }
        }
        java.util.Collections.sort(helixList, new HelixComparator());
        ArrayList<Helix> permanent = new ArrayList<Helix>();
        if (!helixList.isEmpty()) {
            permanent.add(helixList.get(0));
        }
        HelixInfo hinfo_1;
        HelixInfo hinfo_2;
        boolean good = true;
        for (int i = 1; i < helixList.size(); i++){
            hinfo_1 = new HelixInfo((Helix)helixList.get(i), r);
            for (int j = 0; j < permanent.size(); j++){
                hinfo_2 = new HelixInfo((Helix)permanent.get(j), r);
                if (noninterfering(hinfo_1, hinfo_2)){
                    good = true;
                }
                else {
                    good = false;
                    break;
                }
            }
            if (good){
                permanent.add((Helix)helixList.get(i));
            }
            else {
                good = true;
            }
        }
        for (int i = 0; i < permanent.size(); i++){
            result.addHelix((Helix)permanent.get(i));
        }
        return result;
    }

    /** Function to find complex distance
     */
    private int complexDistance(Helix h, HelixStore hsDiagonal) {
        
        HelixStore hsWithin = PopulateHelicesWithin(h, hsDiagonal);
        //At this point we have all helices that are within h. They are in 'hsWithin'
        HelixStore hsMinimal = RemoveOverlappingHelices(hsWithin);
        int CD = CalculateComplexDistance(hsMinimal, h);
        return (CD);
    }

/* Function that returns true if slave is within master
 */
    private HelixStore PopulateHelicesWithin(Helix h, HelixStore hsDiagonal) {
        HelixStore hswithin = new HelixGrid(hsDiagonal.getSequenceLength());  // will store helices within h
        // Why doesn't helixStore have a constructor?
        // answer: helixStore is a INTERFACE... helixGrid implments this interface
        Iterator itr = hsDiagonal.iterator();
        while(itr.hasNext()){
            Helix hDiagonal = (Helix)itr.next();
            
            if ( within(hDiagonal, h) ){
                //add this diagonal helix in the store for further calculations
                hswithin.addHelix(hDiagonal);
            }
        }
        return (hswithin);
    }

/* Function that actually calculates the complex distance given the helices within h
 */
    private int CalculateComplexDistance(HelixStore hs, Helix h) {
        
        HelixInfo hi = new HelixInfo(h, r);
        int distance = ( hi.get5PrimeEnd() - hi.get3PrimeStart() );
        Iterator itrWithin = hs.iterator();
        //find the compex distance using 'hsWithin'
        while(itrWithin.hasNext()){
            Helix hWithin = (Helix)itrWithin.next();
            HelixInfo hiWithin = new HelixInfo(hWithin, r);
            distance = ( distance - ( hiWithin.get3PrimeEnd() - hiWithin.get5PrimeStart() ) );
        }
        return(distance);
    }

/* Function that returns true if slave is within master
 */
    private boolean within(Helix slave, Helix master) {
        HelixInfo hiSlave = new HelixInfo(slave, r);
        HelixInfo hiMaster = new HelixInfo(master, r);
        
        if ( ( hiMaster.get5PrimeEnd() < hiSlave.get5PrimeStart() ) &&
        ( hiMaster.get3PrimeStart() > hiSlave.get3PrimeEnd() ) )
            return true;
        else
            return false;
    }

    /** Removes Overlappin Helices from hs
     * @param HelixStore
     * @return HelixStore
     */
    private HelixStore RemoveOverlappingHelices(HelixStore hs) {
        HelixStore hsminimal = new HelixGrid(hs.getSequenceLength());  // will store helices within h
        Iterator itrOuter = hs.iterator();
        while(itrOuter.hasNext()){
            Helix hO = (Helix)itrOuter.next();
            boolean OuterIsWithinInner = false; //initializing
            Iterator itrInner = hs.iterator();
            while(itrInner.hasNext()){
                Helix hI = (Helix)itrInner.next();
                if ( within(hO, hI) )
                    OuterIsWithinInner = true;
            }
            if (!OuterIsWithinInner)
                hsminimal.addHelix(hO);
        }
        return (hsminimal);
    }

    /**
     * Sets Arguments for the Filter.
     * @param cd complex distance
     * @param sd simple distance
     */
    public void setArguments(int cd, int sd) {
        ComplexDistance = cd;
        SimpleDistance = sd;
    }

    private int ComplexDistance;
    private int SimpleDistance;
    private RNA r;

}


class HelixComparator implements java.util.Comparator<Helix>{
    public HelixComparator(){
    }
    
    public int compare(Helix h, Helix j) {
        if (h.getEnergy() < j.getEnergy()){
            return -1;
        }
        else if (h.getEnergy() > j.getEnergy()){
            return 1;
        }
        else {
            return h.getLength() - j.getLength();
        }
    }
    
}
