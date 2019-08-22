package ch.epfl.javaboy.component.memory;

import java.util.Arrays;
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
        int range = endAddress - startAddress;
        if (range < 0 || range > ram.size())
            throw new IllegalArgumentException();
        
        this.ram = ram;
        start = startAddress;
        end = endAddress;
    }
    
    @Override
    public int read(int address) {
        return isInBounds(address) ? ram.read(address - start) : NO_DATA;
    }
    
    @Override
    public void write(int address, int value) {
        Preconditions.checkBits8(value);
        if (isInBounds(address))
            ram.write(address - start, value);
    }

    @Override
    public byte[] saveState() {
        return Arrays.copyOf(ram.getData(), ram.getData().length);
    }

    @Override
    public void loadState(byte[] state) {
        if (state.length != ram.size())
            throw new IllegalStateException("Invalid state.");
        System.arraycopy(state, 0, ram.getData(), 0, ram.size());
    }

    private boolean isInBounds(int address) {
        Preconditions.checkBits16(address);
        return start <= address && address < end;
    }
}
