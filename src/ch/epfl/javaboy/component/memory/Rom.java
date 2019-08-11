package ch.epfl.javaboy.component.memory;

import java.util.Arrays;

/** Rom
 * Represents a Read Only Memory
 * @author Toufi
 */
public final class Rom {
    private final byte[] data;
    
    /**
     * Constructs a ROM from the given data
     * @param data (byte[]) data in bytes
     * @throws NullPointerException
     * if data is null
     */
    public Rom(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }
    
    /**
     * Returns the size of the ROM
     * @return (int) size of the ROM
     */
    public int size() {
        return data.length;
    }
    
    /**
     * Returns the value at the given index
     * @param index (int) index to read
     * @return (int) 8 bits value
     * @throws IllegalArgumentException
     * if index is not valid
     */
    public int read(int index) {
        return Byte.toUnsignedInt(data[index]);
    };
}
