/*
 *
 * BasepairType.java
 *
 * Created on April 11, 2003, 4:42 PM
 */

package rheat.test;

import java.util.BitSet;
/**
 *
 * @author  jyzhang
 */
public class BasepairType {
    
    public final static int AA = 10;
    public final static int AC = 1;
    public final static int AG = 2;
    public final static int AU = 3;
    public final static int CC = 4;
    public final static int CG = 5;
    public final static int CU = 6;
    public final static int GG = 7;
    public final static int GU = 8;
    public final static int UU = 9;
    
    public static int getBasepairType(char a, char b){
        return (getBasepairType("" + a, "" + b));
    }
    
    public static BitSet returnBasepairTypes(int[] array){
        BitSet bs = new BitSet();
        for (int i = 0; i < array.length; i++){
            if (array[i] > 0 && array[i] <= 10){
                bs.set(array[i]);
            }
        }
        return bs;
    }
    
    public static int getBasepairType(String a, String b){
        a = a.toLowerCase();
        b = b.toLowerCase();
        if (a.equals("a")){
            if (b.equals("a")){
                return AA;
            }
            if (b.equals("c")){
                return AC;
            }
            if (b.equals("g")){
                return AG;
            }
            if (b.equals("u")){
                return AU;
            }
            else {
                return -1;
            }
        }
        else if (a.equals("c")){
            if (b.equals("a")){
                return AC;
            }
            if (b.equals("c")){
                return CC;
            }
            if (b.equals("g")){
                return CG;
            }
            if (b.equals("u")){
                return CU;
            }
            else {
                return -1;
            }
        }
        else if (a.equals("g")){
            if (b.equals("a")){
                return AG;
            }
            if (b.equals("c")){
                return CG;
            }
            if (b.equals("g")){
                return GG;
            }
            if (b.equals("u")){
                return GU;
            }
            else {
                return -1;
            }
        }
        else if (a.equals("u")){
            if (b.equals("a")){
                return AU;
            }
            if (b.equals("c")){
                return CU;
            }
            if (b.equals("g")){
                return GU;
            }
            if (b.equals("u")){
                return UU;
            }
            else {
                return -1;
            }
        }
        else {
            return -1;
        }
    }
    
    public static void main(String args[]){
        
    }
    
}
