package rheat.script;

/**
* Allows access to some sequence of values from a script.  See
* the ScriptIteration class for more.
*
* @author Kevin Grant
*/
public interface IterationDelegate<ScriptT> {

    /**
     * Used to control looping over the iteration.
     * @return the number of items left to iterate over
     */
    int getLeft();

    /**
     * Pops off a new item, decreasing the value that would be
     * returned by a call to getLeft().
     * @return the next item, or null if none are left
     */
    ScriptT getNext();

};
