package ch.epfl.javaboy.bits;

import ch.epfl.javaboy.Preconditions;

/** Bits
 * Offers static methods to manipulate
 * vectors of bits in the form of an int
 * @author Toufi
 */
public final class Bits {

    private static final int[] reverseTab = new int[] {
            0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60, 0xE0,
            0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0,
            0x08, 0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8,
            0x18, 0x98, 0x58, 0xD8, 0x38, 0xB8, 0x78, 0xF8,
            0x04, 0x84, 0x44, 0xC4, 0x24, 0xA4, 0x64, 0xE4,
            0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74, 0xF4,
            0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC,
            0x1C, 0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC,
            0x02, 0x82, 0x42, 0xC2, 0x22, 0xA2, 0x62, 0xE2,
            0x12, 0x92, 0x52, 0xD2, 0x32, 0xB2, 0x72, 0xF2,
            0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A, 0xEA,
            0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA,
            0x06, 0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6,
            0x16, 0x96, 0x56, 0xD6, 0x36, 0xB6, 0x76, 0xF6,
            0x0E, 0x8E, 0x4E, 0xCE, 0x2E, 0xAE, 0x6E, 0xEE,
            0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E, 0xFE,
            0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1,
            0x11, 0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1,
            0x09, 0x89, 0x49, 0xC9, 0x29, 0xA9, 0x69, 0xE9,
            0x19, 0x99, 0x59, 0xD9, 0x39, 0xB9, 0x79, 0xF9,
            0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65, 0xE5,
            0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5,
            0x0D, 0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED,
            0x1D, 0x9D, 0x5D, 0xDD, 0x3D, 0xBD, 0x7D, 0xFD,
            0x03, 0x83, 0x43, 0xC3, 0x23, 0xA3, 0x63, 0xE3,
            0x13, 0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73, 0xF3,
            0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB,
            0x1B, 0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB,
            0x07, 0x87, 0x47, 0xC7, 0x27, 0xA7, 0x67, 0xE7,
            0x17, 0x97, 0x57, 0xD7, 0x37, 0xB7, 0x77, 0xF7,
            0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F, 0xEF,
            0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF,
    };

    private Bits() { }

    /**
     * Returns a mask composed of size bits,
     * starting from the LSB
     * @param size (int) size of the mask
     * @return (int) mask
     * @throws IllegalArgumentException
     * if size is invalid
     */
    public static int fullmask(int size) {
        if (!(0 <= size && size <= Integer.SIZE))
            throw new IllegalArgumentException();
        return size == Integer.SIZE ? -1 : (1 << size) - 1;
    }

    /**
     * Returns a mask composed of a single 
     * bit at the given index
     * @param index (int) index of the mask
     * @return (int) mask 
     * @throws IndexOutOfBoundsException
     * if index is not valid
     */
    public static int mask(int index) {
        Preconditions.checkIndex(index, Integer.SIZE);
        return 1 << index;
    }

    /**
     * Tests the value of the bit at the given 
     * index in the vector bits
     * @param bits (int) vector of bits
     * @param index (int) index of the bit to test
     * @return (boolean) true if the bit at the
     * given index is 1, false otherwise
     */
    public static boolean test(int bits, int index) {
        Preconditions.checkIndex(index, Integer.SIZE);
        return (bits & mask(index)) != 0;
    }
    /**
     * Tests the value of the given Bit
     * in the vector bits 
     * @param bits (int) vector of bits
     * @param bit (Bit) bit to test
     * @return (boolean) true if Bit is 1,
     * false otherwise
     */
    public static boolean test(int bits, Bit bit) {
        return test(bits, bit.index());
    }

    /**
     * Returns the given vector with newValue
     * at the given index
     * @param bits (int) vector of bits
     * @param index (int) index of the bit to set
     * @param newValue (boolean) value to set
     * @return (int) the result
     */
    public static int set(int bits, int index, boolean newValue) {
        return newValue ? bits | mask(index) : bits & ~mask(index);
    }

    /**
     * Returns a vector constitued of the
     * size LSBs
     * @param size (int) size of the clip
     * @param bits (int) vector to clip
     * @return (int) the clipped vector
     */
    public static int clip(int size, int bits) {
        return bits & fullmask(size);
    }

    /**
     * Extract a vector from the given one,
     * starting from start and of the given size
     * @param bits (int) vector to extract from
     * @param start (int) start of the extraction
     * @param size (int) size of the extraction
     * @return (int) the extracted vector
     */
    public static int extract(int bits, int start, int size) {
        Preconditions.checkFromIndexSize(start, size, Integer.SIZE);
        return (bits >>> start) & fullmask(size);
    }

    /**
     * Extract a vector from the given one,
     * starting from start and of the given size
     * @param bits (long) vector to extract from
     * @param start (int) start of the extraction
     * @param size (int) size of the extraction
     * @return (long) the extracted vector
     */
    public static long extract(long bits, int start, int size) {
        Preconditions.checkFromIndexSize(start, size, Long.SIZE);
        long mask = size == Long.SIZE ? -1 : (1L << size) - 1;
        return (bits >>> start) & mask;
    }

    /**
     * Returns a vector whose size LSBs are the ones
     * of bits with a rotation of distance.
     * if distance is positive, rotates to the left
     * else, rotates to the right
     * @param size (int) size of the vector
     * @param bits (int) vector to rotate
     * @param distance (int) distance of the rotation
     * @return (int) the rotated vector
     */
    public static int rotate(int size, int bits, int distance) {
        if (size <= 0)
            throw new IllegalArgumentException();
        int d = Math.floorMod(distance, size);
        return fullmask(size) & ((bits << d) | (bits >>> size - d));
    }
    
    /**
     * Returns a vector with bit 7 of b
     * extended from bit 8 to 31,
     * to "extend" the sign of b
     * @param b (int) 8 bits vector
     * @return (int) sign-extended vector
     * @throws IllegalArgumentException
     * if b is not a valid 8 bits vector
     */
    public static int signExtend8(int b) {
        Preconditions.checkBits8(b);
        return (byte)b;
    }
    
    /**
     * Returns the reverse of b, ie. a vector
     * with its 8th bit = 1st of b,
     * 7th bit = 2nd of b, 6th bit = 3rd of b...
     * @param b (int) 8 bits vector
     * @return (int) reversed vector
     * @throws IllegalArgumentException
     * if b is not a valid 8 bits vector
     */
    public static int reverse8(int b) {
        Preconditions.checkBits8(b);
        return reverseTab[b];
    }
    
    /**
     * Returns the complement of b
     * (in a 8 bits vector)
     * @param b (int) 8 bits vector
     * @return (int) complement of b
     * @throws IllegalArgumentException
     * if b is not a valid 8 bits vector
     */
    public static int complement8(int b) {
        Preconditions.checkBits8(b);
        return b ^ 0xFF;
    }
    
    /**
     * Returns a 16 bits vector constitued
     * of the concatenation of the given 8 bits ones
     * @param highB (int) 8 bits vector
     * @param lowB (int) 8 bits vector
     * @return (int) 16 bits merged
     * @throws IllegalArgumentException
     * if highB or lowB are invalid 8 bits vectors
     */
    public static int make16(int highB, int lowB) {
        Preconditions.checkBits8(lowB);
        Preconditions.checkBits8(highB);
        return (highB << 8) | lowB;
    }

    public static byte[] decomposeInteger(int value) {
        byte[] res = new byte[Integer.BYTES];
        for (int i = 0 ; i < Integer.BYTES ; ++i)
            res[i] = (byte) extract(value, i * Byte.SIZE, Byte.SIZE);
        return res;
    }
    public static int recomposeInteger(byte[] bytes) {
        Preconditions.checkArgument(bytes.length == Integer.BYTES);
        int res = 0;
        for (int i = 0 ; i < Integer.BYTES ; ++i)
            res |= Byte.toUnsignedInt(bytes[i]) << (i * Byte.SIZE);
        return res;
    }
}
