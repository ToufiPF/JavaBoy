package ch.epfl.javaboy.component.lcd;

import java.util.Objects;

import ch.epfl.javaboy.bits.BitVector;
import ch.epfl.javaboy.bits.Bits;

public final class LcdImageLine {
    private final static byte IDENTITY = (byte) 0b11100100;
    
    private final BitVector msb, lsb, opacity;
    
    public LcdImageLine(int size) {
        msb = new BitVector(size);
        lsb = new BitVector(size);
        opacity = new BitVector(size);
    }
    private LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
    }
    
    public int size() {
        return msb.size();
    }
    
    public BitVector msb() {
        return msb;
    }
    public BitVector lsb() {
        return lsb;
    }
    public BitVector opacity() {
        return opacity;
    }
    
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance), opacity.shift(distance));
    }
    
    public LcdImageLine extractWrapped(int start, int size) {
        return new LcdImageLine(msb.extractWrapped(start, size), 
                lsb.extractWrapped(start, size), opacity.extractWrapped(start, size));
    }
    
    public LcdImageLine mapColors(byte colors) {
        if (colors == IDENTITY)
            return this;
        
        final int LENGTH = 4;
        BitVector[] bitsByColor = {
                msb.or(lsb).not(),      // 00 
                msb.not().and(lsb),     // 01
                msb.and(lsb.not()),     // 10
                msb.and(lsb)            // 11
        };
        int [] colorMap = new int[LENGTH];
        for (int i = 0 ; i < LENGTH ; ++i)
            colorMap[i] = Bits.extract(colors, 2 * i, 2);
        
        BitVector m[] = new BitVector[LENGTH];
        BitVector l[] =  new BitVector[LENGTH];
        
        return new LcdImageLine(0);
    }
    
    public LcdImageLine below(LcdImageLine above) {
        return below(above, above.opacity);
    }
    public LcdImageLine below(LcdImageLine above, BitVector opacity) {
        BitVector m = opacity.and(above.msb).or(opacity.not().and(msb));
        BitVector l = opacity.and(above.lsb).or(opacity.not().and(lsb));
        return new LcdImageLine(m, l, this.opacity.or(opacity));
    }
    
    public LcdImageLine join(LcdImageLine other, int start) {
        if (!(0 <= start && start < size()))
            throw new IllegalArgumentException();
        BitVector opacity = new BitVector(size(), true);
        opacity = opacity.shift(start);
        return below(other, opacity.not());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LcdImageLine) {
            LcdImageLine that = (LcdImageLine)obj;
            return msb.equals(that.msb) && lsb.equals(that.lsb) && opacity.equals(that.opacity);
        }
        return false;
    }
    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }
}
