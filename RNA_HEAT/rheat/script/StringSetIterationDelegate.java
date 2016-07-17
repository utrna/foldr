package rheat.script;

import rheat.base.*;

import java.util.*;
import javax.script.*;

/**
 * A delegate for ScriptIteration<String> that reads data
 * from a Set<String>.  This allows scripts to iterate
 * over a list of strings in an unspecified order.
 *
 * @author Kevin Grant
 */
public class StringSetIterationDelegate implements IterationDelegate<String> {

    private int left = 0;
    private Iterator<String> iter = null;

    public StringSetIterationDelegate(Set<String> strings) {
        if (strings == null) {
            // when given nothing, act like it was empty
            // (avoids needless runtime exceptions)
            strings = new HashSet<String>();
        }
        this.left = strings.size();
        this.iter = strings.iterator();
    }

    @Override
    public int getLeft() {
        return this.left;
    }

    @Override
    public String getNext() {
        if (this.left == 0) {
            return null;
        }
        --(this.left);
        return iter.next();
    }

}
