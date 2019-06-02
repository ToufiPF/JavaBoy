package ch.epfl.javaboy.component;

/** Clocked
 * Represents a Component piloted by
 * the system's clock
 * @author Toufi
 */
public interface Clocked {
    /**
     * Asks the component to run all
     * operations it is meant to run
     * during the cycle of the given index
     * @param cycle (long) index of the cycle
     */
    void cycle(long cycle);
}
