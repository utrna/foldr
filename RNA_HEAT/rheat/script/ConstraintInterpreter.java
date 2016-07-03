package rheat.script;

import rheat.base.*;
import rheat.filter.*;

import java.util.*;
import javax.script.*;

/**
 * Given a recognized type of object, this API can generate the
 * scripting command that would be equivalent.  This is useful
 * for producing a history list, for example.
 *
 * @author Kevin Grant
 */
public class ConstraintInterpreter {

    /**
     * Creates a scripting command in terms of the "rheat" object for
     * adding a new filter.  The choice of method and set of arguments
     * will be consistent with the given filter object.
     *
     * @return a JavaScript command string
     * @throws RuntimeException if the filter cannot be interpreted
     */
    static public String getScriptCommandFor(Filter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("rheat.");
        if (filter instanceof AAandAGHelicesFilter) {
            AAandAGHelicesFilter castFilter = (AAandAGHelicesFilter)filter;
            sb.append("addAAandAGFilter(");
            // no arguments defined
            sb.append(")");
        } else if (filter instanceof BasePairRangeHelicesFilter) {
            BasePairRangeHelicesFilter castFilter = (BasePairRangeHelicesFilter)filter;
            sb.append("addDiagonalDistanceFilter(");
            sb.append(castFilter.getRangeMax());
            sb.append(", ");
            sb.append(castFilter.getRangeMin());
            sb.append(")");
        } else if (filter instanceof BPFilter) {
            BPFilter castFilter = (BPFilter)filter;
            BitSet enabledBP = castFilter.getBasePairs();
            sb.append("addBasePairFilter(");
            Vector<String> pairs = new Vector<String>();
            if (enabledBP.get(BasepairType.AA)) {
                pairs.add("AA");
            }
            if (enabledBP.get(BasepairType.AC)) {
                pairs.add("AC");
            }
            if (enabledBP.get(BasepairType.AG)) {
                pairs.add("AG");
            }
            if (enabledBP.get(BasepairType.AU)) {
                pairs.add("AU");
            }
            if (enabledBP.get(BasepairType.CC)) {
                pairs.add("CC");
            }
            if (enabledBP.get(BasepairType.CG)) {
                pairs.add("CG");
            }
            if (enabledBP.get(BasepairType.CU)) {
                pairs.add("CU");
            }
            if (enabledBP.get(BasepairType.GG)) {
                pairs.add("GG");
            }
            if (enabledBP.get(BasepairType.GU)) {
                pairs.add("GU");
            }
            if (enabledBP.get(BasepairType.UU)) {
                pairs.add("UU");
            }
            int i = 0;
            for (String s : pairs) {
                sb.append("'");
                sb.append(s);
                sb.append("'");
                ++i;
                if (i != pairs.size()) {
                    sb.append(", ");
                }
            }
            sb.append(")");
        } else if (filter instanceof ComplexFilter) {
            ComplexFilter castFilter = (ComplexFilter)filter;
            sb.append("addComplexFilter(");
            sb.append(castFilter.getComplexDistance());
            sb.append(", ");
            sb.append(castFilter.getSimpleDistance());
            sb.append(")");
        } else if (filter instanceof ELoopHelicesFilter) {
            ELoopHelicesFilter castFilter = (ELoopHelicesFilter)filter;
            sb.append("addELoopFilter(");
            // no arguments defined
            sb.append(")");
        } else if (filter instanceof EnergyMaxMinFilter) {
            EnergyMaxMinFilter castFilter = (EnergyMaxMinFilter)filter;
            sb.append("addEnergyFilter(");
            sb.append(castFilter.getMaxEnergy());
            sb.append(", ");
            sb.append(castFilter.getMinEnergy());
            sb.append(")");
        } else if (filter instanceof MaxMinFilter) {
            MaxMinFilter castFilter = (MaxMinFilter)filter;
            sb.append("addHelixLengthFilter(");
            sb.append(castFilter.getMaxLength());
            sb.append(", ");
            sb.append(castFilter.getMinLength());
            sb.append(")");
        } else {
            throw new RuntimeException("unrecognized filter class: " + filter.getClass().getName());
        }
        return sb.toString();
    }

}
