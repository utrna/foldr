package rheat.script;

import rheat.base.*;
import rheat.filter.*;

import java.util.*;
import javax.script.*;

/**
 * This object allows simple iteration from JavaScript for good
 * performance (as opposed to returning an array or list, for
 * example).  Although objects are only returned on demand, you
 * must still know the total length of the iteration in
 * advance.
 *
 * A typical approach is to loop using an integer variable from
 * 0 to the length(), calling next() to retrieve each value.
 *
 * @author Kevin Grant
 */
public class ScriptIteration<ScriptT> {

    private IterationDelegate<ScriptT> delegate = null;
    private int length = 0;



    /**
     * Creates a scripting interface for iterating over data defined
     * by the delegate class.  This approach ensures that all
     * iterations have a common scripting interface, even if the
     * method of translating data into scripting objects can vary.
     */
    public ScriptIteration(IterationDelegate<ScriptT> delegate) {
        assert(delegate != null);
        this.delegate = delegate;
        this.length = delegate.getLeft(); // capture initial count
    }

    /**
     * Use this immediately after construction to find the length of
     * the range, and use in a loop with calls to next() to perform
     * the iteration.
     */
    public int left() {
        return delegate.getLeft();
    }

    /**
     * Returns the number of values in the range.  Equivalent to
     * capturing the value of left() prior to any calls to next().
     */
    public final int length() {
        return this.length;
    }

    /**
     * Returns non-null if anything is left.
     */
    public ScriptT next() throws ScriptException {
        ScriptT result = null;
        if (left() > 0) {
            try {
                result = delegate.getNext();
            } catch (Exception e) {
                ScriptMain.rethrowAsScriptException(e);
            }
        }
        return result;
    }

}
