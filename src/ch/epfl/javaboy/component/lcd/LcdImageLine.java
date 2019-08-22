package ch.epfl.javaboy.component.lcd;

import ch.epfl.javaboy.bits.BitVector;
import ch.epfl.javaboy.bits.Bits;

import java.util.Objects;

/**
 * Represents a line of an LcdImage
 * @author Toufi
 */
@SuppressWarnings("WeakerAccess")
public final class LcdImageLine {
    
    /**
     * Builder for LcdImageLine
     * @author Toufi
     */
    @SuppressWarnings("WeakerAccess")
    public static final class Builder {
        private final BitVector.Builder msb, lsb;
        
        /**
         * Construct a new LcdImageLine.Builder
         * @param size (int) width of the LcdImageLine
         */
        public Builder(int size) {
            msb = new BitVector.Builder(size);
            lsb = new BitVector.Builder(size);
        }
        
        /**
         * Sets the two bytes of the LcdImageLine
         * at the given index
         * @param index (int) index of the bytes to set
         * @param ms (int) 8-bits vector msb
         * @param ls (int) 8-bits vector lsb
         * @return (Builder) this builder
         */
        public Builder setBytes(int index, int ms, int ls) {
            msb.setByte(index, ms);
            lsb.setByte(index, ls);
            return this;
        }
        
        /**
         * Builds the line and incapacitates
         * this Builder
         * @return (LcdImageLine) built line
         */
        public LcdImageLine build() {
            BitVector m = msb.build();
            BitVector l = lsb.build();
            BitVector opac = m.or(l);
            return new LcdImageLine(m, l, opac);
        }
    }

    private final static byte IDENTITY = (byte) 0b11100100;

    private final BitVector msb, lsb, opacity;
    
    /**
     * Creates a blank LcdImageLine of
     * the given size
     * @param size (int) width of the line
     */
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
    
    /**
     * Returns the size of the LcdImageLine
     * @return (int) line width
     */
    public int size() {
        return msb.size();
    }
    
    /**
     * Returns the BitVector containing
     * the msbs of the LcdImageLine
     * @return (BitVector) msbs
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * Returns the BitVector containing
     * the lsbs of the LcdImageLine
     * @return (BitVector) lsbs
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * Returns the BitVector containing
     * the opacity of the LcdImageLine
     * @return (BitVector) opacity
     */
    public BitVector opacity() {
        return opacity;
    }
    
    /**
     * Returns a new LcdImageLine constructed
     * by shifting this one of the given distance
     * @param distance (int) distance of the shift
     * (positive distance -> left shift ;
     * negative distance -> right shift)
     * @return (LcdImageLine) shifted LcdImageLine
     */
    public LcdImageLine shift(int distance) {
        return new LcdImageLine(msb.shift(distance), lsb.shift(distance), opacity.shift(distance));
    }
    
    /**
     * Returns a new LcdImageLine obtained by applying
     *  wrapped extraction to this line
     * @param start (int) start of the extraction
     * @param size (int) size of the extraction
     * @return (LcdImageLine) wrapped-extracted LcdImageLine
     */
    public LcdImageLine extractWrapped(int start, int size) {
        return new LcdImageLine(msb.extractWrapped(start, size), 
                lsb.extractWrapped(start, size), opacity.extractWrapped(start, size));
    }
    
    /**
     * Returns a new LcdImageLine obtained by applying
     * the given color map to this line
     * @param colors (byte) color map : 
     * bits 1 & 0 : new color of former color 00 ;
     * bits 3 & 2 : new color of former color 01 ;
     * bits 5 & 4 : new color of former color 10 ;
     * bits 7 & 6 : new color of former color 11
     * @return (LcdImageLine) color mapped LcdImageLine
     */
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

        final BitVector bits1 = new BitVector(size(), true);
        final BitVector bits0 = new BitVector(size(), false);

        BitVector m = bitsByColor[0].and(Bits.test(colorMap[0], 1) ? bits1 : bits0);
        BitVector l = bitsByColor[0].and(Bits.test(colorMap[0], 0) ? bits1 : bits0);
        for (int i = 1 ; i < LENGTH ; ++i) {
            m = m.or(bitsByColor[i].and(Bits.test(colorMap[i], 1) ? bits1 : bits0));
            l = l.or(bitsByColor[i].and(Bits.test(colorMap[i], 0) ? bits1 : bits0));
        }
        return new LcdImageLine(m, l, opacity);
    }
    
    /**
     * Returns the LcdImageLine obtained when placing
     * this LcdImageLine under the given one
     * @param above (LcdImageLine) above line
     * @return (LcdImageLine) superposed LcdImageLine
     */
    public LcdImageLine below(LcdImageLine above) {
        return below(above, above.opacity);
    }
    /**
     * Returns the LcdImageLine obtained when placing
     * this LcdImageLine under the given one, following
     * the given opacity vector
     * @param above (LcdImageLine) above line
     * @param opacity (BitVector) opacity of the above line
     * @return (LcdImageLine) superposed LcdImageLine
     */
    public LcdImageLine below(LcdImageLine above, BitVector opacity) {
        BitVector m = opacity.and(above.msb).or(opacity.not().and(msb));
        BitVector l = opacity.and(above.lsb).or(opacity.not().and(lsb));
        return new LcdImageLine(m, l, this.opacity.or(opacity));
    }
    
    /**
     * Returns the LcdImageLine obtained when joining
     * this LcdImageLine and the given one,
     * at the given index
     * @param other (LcdImageLine) line to join with this
     * @param start (int) index of the jonction
     * (start bits from this, then (size - start) bits from other) 
     * @return (LcdImageLine) joined LcdImageLine
     */
    public LcdImageLine join(LcdImageLine other, int start) {
        if (!(0 <= start && start < size()))
            throw new IllegalArgumentException();
        BitVector opacity = new BitVector(size(), true).shift(start);
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
    @Override
    public String toString() {
        return msb.toString() + '\n' +
                lsb.toString() + '\n' +
                opacity.toString();
    }
}
