package ch.epfl.javaboy.component;

import ch.epfl.javaboy.Bus;

/** Component
 * Basic interface of a JavaBoy Component
 * @author Toufi
 */
public interface Component {
    
    /** Return value when read cannot return its value **/
    static final int NO_DATA = 0x100;
    
    /**
     * Returns the value at the given address
     * @param address (int) index to read
     * @return (int) 8 bits vector, or
     * NO_DATA if reading is not possible
     */
    int read(int address);
    
    /**
     * Sets the value at the given adrress
     * @param address (int) address to write
     * @param value (int) 8 bits vector
     */
    void write(int address, int value);
    
    /**
     * Attachs the Component to the given
     * Bus, to allow communications on it
     * @param bus (Bus) the bus to attach onto
     */
    default void attachTo(Bus bus) {
        bus.attach(this);
    }
}
