package ch.epfl.javaboy;

/** Preconditions
 * An interface for various checks
 * of arguments
 * @author Toufi
 */
public interface Preconditions {
    
    /**
     * Check the given argument
     * @param b (boolean) argument to check
     * @throws IllegalArgumentException if b is false
     */
    public static void checkArgument(boolean b) {
        if (!b)
            throw new IllegalArgumentException();
    }
    
    /**
     * Check that the given vector
     * is contained in 8 bits
     * @param v (int) vector to check
     * @throws IllegalArgumentException 
     * if v takes more than 8 bits
     */
    public static void checkBits8(int v) {
        if (!(0 <= v && v <= 0xFF))
            throw new IllegalArgumentException("Non 8 bits : 0x" + Integer.toHexString(v));
    }
    
    /**
     * Check that the given vector
     * is contained in 16 bits
     * @param v (int) vector to check
     * @throws IllegalArgumentException 
     * if v takes more than 16 bits
     */
    public static void checkBits16(int v) {
        if (!(0 <= v && v <= 0xFFFF))
            throw new IllegalArgumentException("Non 16 bits : 0x" + Integer.toHexString(v));
    }
}
