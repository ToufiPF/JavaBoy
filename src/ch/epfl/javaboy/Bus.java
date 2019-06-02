package ch.epfl.javaboy;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import ch.epfl.javaboy.component.Component;

/** Bus
 * Represents a bus and is in charge of
 * the communication between the attached
 * Components
 * @author Toufi
 */
public final class Bus {
    private final List<Component> attached = new LinkedList<>();
    
    /**
     * Attaches the given component to the bus
     * @param component (Component) to attach
     * @throws NullPointerException
     * if the component is null
     */
    public void attach(Component component) {
        Objects.requireNonNull(component);
        attached.add(component);
    }
    
    /**
     * Attemps to read the given address among
     * the attached components, or 0xFF if all
     * components return NO_DATA
     * @param address (int) 16 bits address
     * @return (int) 8 bits value, or 0xFF
     * @throws IllegalArgumentException 
     * if address is not valid
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        for (Component c : attached)
            if (c.read(address) != Component.NO_DATA)
                return c.read(address);
        return 0xFF;
    }
    
    /**
     * Attempts to write the given value at the
     * given address for all the attached components
     * @param address (int) 16 bits address
     * @param value (int) 8 bits value
     * @throws IllegalArgumentException
     * if address or value are not valid
     */
    public void write(int address, int value) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);
        
        for (Component c : attached)
            c.write(address, value);
    }
}
