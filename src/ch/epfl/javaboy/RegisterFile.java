package ch.epfl.javaboy;

import ch.epfl.javaboy.bits.Bit;
import ch.epfl.javaboy.bits.Bits;

/** RegisterFile
 * Represents a register file 
 * (length of the registers = 8)
 *
 * @param <E> Enum implementing Register
 * 
 * @author Toufi
 */
public class RegisterFile<E extends Register> {
    
    private final int[] registerFile;
    
    /**
     * Constructs a new RegisterFile,
     * with the same length that the given array
     * (Meant to be called with Enum.values())
     * @param allRegs (E[]) array of the enum's values
     */
    public RegisterFile(E[] allRegs) {
        registerFile = new int[allRegs.length];
    }
    
    /**
     * Returns the value of the given
     * register
     * @param reg (E) register to read
     * @return (int) 8 bits vector
     */
    public int get(E reg) {
        return registerFile[reg.index()];
    }
    
    /**
     * Sets the value of the given register
     * @param reg (E) register to set
     * @param newValue (int) value to set
     */
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        registerFile[reg.index()] = newValue;
    }
    
    /**
     * Tests the given bit in reg
     * @param reg (E) register
     * @param bit (Bit) bit to test
     * @return (boolean) true if bit = 1, false otherwise
     */
    public boolean testBit(E reg, Bit bit) {
        return Bits.test(get(reg), bit);
    }
    
    /**
     * Sets the given bit in reg to newValue
     * @param reg (E) register
     * @param bit (Bit) bit to set
     * @param newValue (boolean) value to set
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        set(reg, Bits.set(get(reg), bit.index(), newValue));
    }
}
