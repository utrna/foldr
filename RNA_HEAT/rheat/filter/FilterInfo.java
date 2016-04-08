/*
 * FilterInfo.java
 *
 * Created on May 2, 2003, 4:50 PM
 */

package rheat.filter;

/**
 *
 * @author  jyzhang
 */
public class FilterInfo {
    
    private String name;
    private String description;
    
    /** Creates a new instance of FilterInfo */
    public FilterInfo(String n, String d) {
        name = n;
        description = d;
    }
    
    public String toString(){
        return name;
    }
    
    public String getDescription(){
        return description;
    }
}
