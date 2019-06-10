package ch.epfl.javaboy.bits;

import java.util.Arrays;

import ch.epfl.javaboy.Preconditions;

/**
 * Represents a vector of bits which size is 
 * a multiple of 32 (Integer.SIZE)
 * @author Toufi
 */
public final class BitVector {
    
    /**
     * Builder for BitVectors
     * @author Toufi
     */
    public final static class Builder {
        private int[] bits;
        
        /**
         * Creates a new Builder
         * @param size (int) size of the BitVector
         * @throws IllegalArgumentException
         * if size is not divisible by 32
         */
        public Builder(int size) {
            if (!isDivisibleBy32(size))
                throw new IllegalArgumentException();
            bits = new int[size / Integer.SIZE];
        }
        
        /**
         * Sets the value of the byte at the given index
         * @param index (int) index of the byte
         * @param val (int) 8 bits value of the byte
         * @throws IllegalStateException
         * if the BitVector was already built
         */
        public void setByte(int index, int val) {
            if (bits == null)
                throw new IllegalStateException("BitVector already built !");
            if (!(0 <= index && index < bits.length * Integer.BYTES))
                throw new IndexOutOfBoundsException();
            Preconditions.checkBits8(val);

            final int intIndex = index / Integer.BYTES;
            final int bitIndex = (index % Integer.BYTES) * Byte.SIZE;
            bits[intIndex] &= ~(0xFF << bitIndex);
            bits[intIndex] |= val << bitIndex;
        }
        
        /**
         * Builds the BitVector and renders
         * this Builder unusable
         * @return (BitVector)
         * @throws IllegalStateException
         * if the BitVector was already built
         */
        public BitVector build() {
            if (bits == null)
                throw new IllegalStateException("BitVector already built !");
            BitVector vec = new BitVector(bits);
            bits = null;
            return vec;
        }

        @Override
        public String toString() {
            return new BitVector(bits).toString();
        }
    }

    private static boolean isDivisibleBy32(int n) {
        // size % 32 <-> size & 0b0001_1111
        return (n & Bits.fullmask(5)) == 0;
    }

    private final int[] bits;

    /**
     * Creates a new BitVector of the given size
     * filled with the given value
     * @param size (int) size
     * @param value (int) value of the bits
     * @throws IllegalArgumentException
     * if size is not a multiple of 32, or negative
     */
    public BitVector(int size, boolean value) {
        if (!(isDivisibleBy32(size) && size > 0))
            throw new IllegalArgumentException("Taille bits invalide.");

        bits = new int[size / Integer.SIZE];
        if (value)
            Arrays.fill(bits, -1);
    }

    /**
     * Creates a new BitVector of the given size
     * filled with 0s
     * @param size (int) size
     * @throws IllegalArgumentException
     * if size is not a multiple of 32
     */
    public BitVector(int size) {
        this(size, false);
    }

    private BitVector(int[] bits) {
        this.bits = bits;
    }

    /**
     * Returns the size of the BitVector
     * @return (int) number of bits
     */
    public int size() {
        return bits.length * Integer.SIZE;
    }

    /**
     * Test the given bit
     * @param index (int) index of the bit
     * @return (boolean) true if the given bit is 1,
     * false otherwise
     * @throws IndexOutOfBoundsException
     * if index is not valid
     */
    public boolean testBit(int index) {
        Preconditions.checkIndex(index, size());
        int q = index / Integer.SIZE;
        int r = index % Integer.SIZE;

        return Bits.test(bits[q], r);
    }

    /**
     * Extracts a vector starting at start, with the given size,
     * from the zero extension of this BitVector 
     * @param start (int) start of the extraction
     * @param size (int) size of the extraction
     * @return (BitVector) extracted BitVector
     * @throws IllegalArgumentException
     * if size is not divisible by 32, or size is negative
     */
    public BitVector extractZeroExtended(int start, int size) {
        if (!isDivisibleBy32(size) || size < 0)
            throw new IllegalArgumentException();

        int[] extracted = new int[size / Integer.SIZE];

        final int q = Math.floorDiv(start, Integer.SIZE);
        if (isDivisibleBy32(start)) {
            for (int i = 0 ; i < extracted.length ; ++i) {
                if (isInArray(q + i))
                    extracted[i] = bits[q + i];
            }
        } else {
            int r = Math.floorMod(start, Integer.SIZE);

            for (int i = 0 ; i < extracted.length ; ++i) {
                int bits1 = 0, bits2 = 0;
                if (isInArray(q + i))
                    bits1 = Bits.extract(bits[q + i], r, Integer.SIZE - r);
                if (isInArray(q + i + 1))
                    bits2 = Bits.extract(bits[q + i + 1], 0, r);
                extracted[i] = bits1 | (bits2 << (Integer.SIZE - r));
            }
        }
        return new BitVector(extracted);
    }

    /**
     * Extracts a vector starting at start, with the given size,
     * from the wrapped extension of this BitVector 
     * @param start (int) start of the extraction
     * @param size (int) size of the extraction
     * @return (BitVector) extracted BitVector
     * @throws IllegalArgumentException
     * if size is not divisible by 32, or size is negative
     */
    public BitVector extractWrapped(int start, int size) {
        if (!isDivisibleBy32(size) || size < 0)
            throw new IllegalArgumentException();

        int[] extracted = new int[size / Integer.SIZE];

        final int q = Math.floorDiv(start, Integer.SIZE);
        if (isDivisibleBy32(start)) {
            for (int i = 0 ; i < extracted.length ; ++i) {
                extracted[i] = bits[Math.floorMod(q + i, bits.length)];
            }
        } else {
            int r = Math.floorMod(start, Integer.SIZE);

            for (int i = 0 ; i < extracted.length ; ++i) {
                int bits1 = Bits.extract(bits[Math.floorMod(q + i, bits.length)], r, Integer.SIZE - r);
                int bits2 = Bits.extract(bits[Math.floorMod(q + i + 1, bits.length)],  0, r);
                extracted[i] = bits1 | (bits2 << (Integer.SIZE - r));
            }
        }
        return new BitVector(extracted);
    }

    /**
     * Shifts this BitVector of the given distance
     * (left if distance is positive, right otherwise)
     * @param distance (int) distance of the shift
     * @return (BitVector) shifted BitVector
     */
    public BitVector shift(int distance) {
        if (distance == 0)
            return this;
        return extractZeroExtended(distance, size());
    }

    /**
     * Returns the complement of this BitVector
     * @return (BitVector) complement
     */
    public BitVector not() {
        int[] nots = new int[bits.length];
        for (int i = 0 ; i < nots.length ; ++i)
            nots[i] = ~bits[i];
        return new BitVector(nots);
    }

    /**
     * Returns the conjunction (logic and)
     * between the given BitVector and this one
     * @param other (BitVector) 2nd vector
     * @return (BitVector) conjunction
     * @throws IllegalArgumentException
     * if the two vectors don't have the same size
     */
    public BitVector and(BitVector other) {
        if (this.bits.length != other.bits.length)
            throw new IllegalArgumentException();
        int ands[] = new int[bits.length];
        for (int i = 0 ; i < ands.length ; ++i)
            ands[i] = this.bits[i] & other.bits[i];
        return new BitVector(ands);
    }
    /**
     * Returns the disjunction (logic or)
     * between the given BitVector and this one
     * @param other (BitVector) 2nd vector
     * @return (BitVector) disjunction
     * @throws IllegalArgumentException
     * if the two vectors don't have the same size
     */
    public BitVector or(BitVector other) {
        if (this.bits.length != other.bits.length)
            throw new IllegalArgumentException();
        int ors[] = new int[bits.length];
        for (int i = 0 ; i < ors.length ; ++i)
            ors[i] = this.bits[i] | other.bits[i];
        return new BitVector(ors);
    }
    /**
     * Returns the exclusive or between
     * the given BitVector and this one
     * @param other (BitVector) 2nd vector
     * @return (BitVector) exclusive or
     * @throws IllegalArgumentException
     * if the two vectors don't have the same size
     */
    public BitVector xor(BitVector other) {
        if (this.bits.length != other.bits.length)
            throw new IllegalArgumentException();
        int xors[] = new int[bits.length];
        for (int i = 0 ; i < xors.length ; ++i)
            xors[i] = this.bits[i] ^ other.bits[i];
        return new BitVector(xors);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof BitVector && Arrays.equals(this.bits, ((BitVector) obj).bits);
    }
    @Override
    public int hashCode() {
        return Arrays.hashCode(bits);
    }
    @Override
    public String toString() {
        StringBuilder build = new StringBuilder(size());
        for (int i = size() - 1 ; i >= 0 ; --i)
            build.append(testBit(i) ? '1' : '0');
        return build.toString();
    }

    private boolean isInArray(int index) {
        return 0 <= index && index < bits.length;
    }
}
