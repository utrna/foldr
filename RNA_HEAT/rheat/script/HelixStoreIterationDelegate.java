package rheat.script;

import rheat.base.*;
import rheat.filter.*;

import java.util.*;
import javax.script.*;

/**
 * A delegate for ScriptIteration<ScriptHelix> that reads data
 * from any subclass of HelixStore.  This allows scripts to
 * iterate over a list of helices.
 *
 * @author Kevin Grant
 */
public class HelixStoreIterationDelegate implements IterationDelegate<ScriptHelix> {

    private int left = 0;
    private Iterator<Helix> iter = null;
    private RNA sourceRNA = null;

    public HelixStoreIterationDelegate(HelixStore helices, RNA sourceRNA) {
        if (helices == null) {
            this.left = 0;
            this.iter = null;
        } else {
            this.left = helices.getHelixCount();
            this.iter = helices.iterator();
        }
        this.sourceRNA = sourceRNA;
    }

    @Override
    public int getLeft() {
        return this.left;
    }

    @Override
    public ScriptHelix getNext() {
        if (this.left == 0) {
            return null;
        }
        --(this.left);
        return new ScriptHelix(iter.next(), sourceRNA);
    }

}
