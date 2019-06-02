package ch.epfl.javaboy;

/** Register
 * Interface meant to be implemented by Enums
 * representing registers from a file register
 * @author Toufi
 */
public interface Register {

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
}
