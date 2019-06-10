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
    public static int checkBits8(int v) {
        if (!(0 <= v && v <= 0xFF))
            throw new IllegalArgumentException("Non 8 bits : 0x" + Integer.toHexString(v));
        return v;
    }
    
    /**
     * Check that the given vector
     * is contained in 16 bits
     * @param v (int) vector to check
     * @throws IllegalArgumentException 
     * if v takes more than 16 bits
     */
    public static int checkBits16(int v) {
        if (!(0 <= v && v <= 0xFFFF))
            throw new IllegalArgumentException("Non 16 bits : 0x" + Integer.toHexString(v));
        return v;
    }
    
    /**
     * Checks if the index is within the bounds of 
     * the range from 0 (inclusive) to length (exclusive).
     * @param index (int) index to test
     * @param length (int) bound
     */
    public static void checkIndex(int index, int length) {
        if (!(0 <= index && index < length))
            throw new IllegalArgumentException("Invalid index (range:[0," + length + "]) : " + index);
    }
    
    /**
     * Checks if the sub-range from fromIndex (inclusive)
     *  to fromIndex + size (exclusive) is within the 
     *  bounds of range from 0 (inclusive) to length (exclusive).
     * @param fromIndex (int) start of the subrange
     * @param size (int) size of the subrange
     * @param length (int) bound
     */
    public static void checkFromIndexSize(int fromIndex, int size, int length) {
        if (!(0 <= fromIndex && fromIndex + size < length && 0 <= size))
            throw new IllegalArgumentException("Invalid range : index=" + fromIndex
                    + ", size=" + size + ", length=" + length);
    }
}
