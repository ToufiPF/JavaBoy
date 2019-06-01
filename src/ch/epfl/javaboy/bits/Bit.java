package ch.epfl.javaboy.bits;

/** Bit
 * Interface created to be implemented by
 * enumerated types representing a bit set
 * @author Toufi
 */
public interface Bit {
    /**
     * @return (int) the ordinal (index)
     */
    int ordinal();
    
    /**
     * Equals ordinal()
     * @return (int) the ordinal (index)
     */
    default int index() {
        return ordinal();
    }
    
    /**
     * Returns the mask corresponding
     * to the ordinal, ie. an int
     * with only a 1 at the index
     * @return (int) the mask 
     * corresponding to the ordinal
     */
    default int mask() {
        return 1 << ordinal();
    }
}
