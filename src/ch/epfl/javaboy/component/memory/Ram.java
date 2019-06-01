package ch.epfl.javaboy.component.memory;

import ch.epfl.javaboy.Preconditions;

/** Ram
 * Represents a Random Access Memory
 * @author Toufi
 */
public final class Ram {    
    private final byte[] data;
    
    /**
     * Creates a new Ram of the given size
     * (size in bytes)
     * @param size (int) size of the RAM
     * @throws IllegalArgumentException
     * if size is negative
     */
    public Ram(int size) {
        Preconditions.checkArgument(0 <= size);
        data = new byte[size];
    }
    
    /**
     * Returns the size of the RAM
     * @return size of the RAM
     */
    public int size() {
        return data.length;
    }
    
    /**
     * Returns the value of the RAM at 
     * the given index
     * @param index (int) index to read
     * @return (int) 8 bits value at index
     * @throws IndexOutOfBoundsException
     * if index is not valid
     */
    public int read(int index) {
        return Byte.toUnsignedInt(data[index]);
    }
    
    /**
     * Writes the given value at the given index
     * @param index (int) address to write into
     * @param value (int) 8 bits vector
     * @throws IllegalArgumentException
     * if value is not a valid 8 bits vector
     */
    public void write(int index, int value) {
        Preconditions.checkBits8(value);
        data[index] = (byte) value;
    }
}
