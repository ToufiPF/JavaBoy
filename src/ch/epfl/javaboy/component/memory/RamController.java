package ch.epfl.javaboy.component.memory;

import java.util.Objects;

import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.component.Component;

/** RamController
 * Represents a Component controlling RAM access
 * @author Toufi
 */
public final class RamController implements Component {
    
    private final Ram ram;
    private final int start, end;
    
    /**
     * Constructs a RamController for the given RAM,
     * accessible between startAddress (included) and 
     * endAddress (excluded)
     * @param ram (Ram) linked RAM
     * @param startAddress (int) 16 bit start address
     * @param endAddress (int) 16 bit end address
     * @throws NullPointerException
     * if ram is null
     * @throws IllegalArgumentException
     * if the start/end addresses are invalid,
     * or if the given interval is not valid
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        Objects.requireNonNull(ram);
        Preconditions.checkBits16(startAddress);
        Preconditions.checkBits16(endAddress);
        Objects.checkFromToIndex(startAddress, endAddress, ram.size());
        
        this.ram = ram;
        start = startAddress;
        end = endAddress;
    }
    
    /**
     * Constructs a RamController for the given RAM,
     * accessible from startAddress (included) to
     * the end of the RAM
     * @param ram (Ram) linked RAM
     * @param startAddress (int) 16 bit start address
     * @throws NullPointerException
     * if ram is null
     * @throws IllegalArgumentException
     * if the start address is invalid
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, ram.size() - startAddress);
    }
    
    @Override
    public int read(int address) {
        return isInBounds(address) ? ram.read(address) : NO_DATA;
    }
    
    @Override
    public void write(int address, int value) {
        if (isInBounds(address))
            ram.write(address, value);
    }
    
    private boolean isInBounds(int address) {
        return start <= address && address < end;
    }
}
