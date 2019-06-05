package ch.epfl.javaboy.bits;

import java.util.Arrays;
import java.util.Objects;

public final class BitVector {
    
    private final int[] bits;
    
    public BitVector(int size, boolean value) {
        if (!(isDivisibleBy32(size) && size > 0))
            throw new IllegalArgumentException("Taille bits invalide.");
        
        bits = new int[size / Integer.SIZE];
        if (value)
            Arrays.fill(bits, -1);
    }
    public BitVector(int size) {
        this(size, false);
    }
    private BitVector(int[] bits) {
        this.bits = bits;
    }
    
    public int size() {
        return bits.length * Integer.SIZE;
    }
    
    public boolean testBit(int index) {
        Objects.checkIndex(index, size());
        int q = index / Integer.SIZE;
        int r = index % Integer.SIZE;
        
        return Bits.test(bits[q], r);
    }
    
    public BitVector extractZeroExtended(int start, int size) {
        if (!isDivisibleBy32(size))
            throw new IllegalArgumentException();

        int[] extracted = new int[size / Integer.SIZE];
        
        if (isDivisibleBy32(start)) {
            int q = Math.floorDiv(start, Integer.SIZE);
            for (int i = 0 ; i < extracted.length ; ++i) {
                if (0 <= q + i && q + i < bits.length)
                    extracted[i] = bits[q + i];
            }
            return new BitVector(extracted);
        } else {
            int q = Math.floorDiv(start, Integer.SIZE);
            int r = Math.floorMod(start, Integer.SIZE);
            
            for (int i = 0 ; i < extracted.length ; ++i) {
                
            }
        }
    }
    public BitVector extractWrapped(int start, int size) {
        
    }
    public BitVector shift(int distance) {
        if (distance == 0)
            return this;
        return extractZeroExtended(distance, size());
    }
    
    public BitVector not() {
        int[] nots = new int[bits.length];
        for (int i = 0 ; i < nots.length ; ++i)
            nots[i] = ~bits[i];
        return new BitVector(nots);
    }
    
    public BitVector and(BitVector other) {
        if (this.bits.length != other.bits.length)
            throw new IllegalArgumentException();
        int ands[] = new int[bits.length];
        for (int i = 0 ; i < ands.length ; ++i)
            ands[i] = this.bits[i] & other.bits[i];
        return new BitVector(ands);
    }
    public BitVector or(BitVector other) {
        if (this.bits.length != other.bits.length)
            throw new IllegalArgumentException();
        int ors[] = new int[bits.length];
        for (int i = 0 ; i < ors.length ; ++i)
            ors[i] = this.bits[i] | other.bits[i];
        return new BitVector(ors);
    }
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
        return  Arrays.hashCode(bits);
    }
    @Override
    public String toString() {
        StringBuilder build = new StringBuilder(size());
        for (int i = 0 ; i < size() ; ++i)
            build.append(testBit(i) ? '1' : '0');
        return build.toString();
    }
    
    private boolean isDivisibleBy32(int n) {
        // size % 32 <-> size & 0b0001_1111
        return (n & Bits.fullmask(5)) == 0;
    }
}
