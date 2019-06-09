package ch.epfl.javaboy.component.lcd;

import java.util.Objects;

import ch.epfl.javaboy.bits.BitVector;
import ch.epfl.javaboy.bits.Bits;

public final class LcdImageLine {

    public static final class Builder {
        private final BitVector.Builder msb, lsb;

        public Builder(int size) {
            msb = new BitVector.Builder(size);
            lsb = new BitVector.Builder(size);
        }

        public Builder setBytes(int index, int ms, int ls) {
            msb.setByte(index, ms);
            lsb.setByte(index, ls);
            return this;
        }
        
        public LcdImageLine build() {
            BitVector m = msb.build();
            BitVector l = lsb.build();
            BitVector opac = m.or(l);
            return new LcdImageLine(m, l, opac);
        }
    }

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
        StringBuilder b = new StringBuilder(3 * size() + 3);
        b.append(msb.toString()).append('\n');
        b.append(lsb.toString()).append('\n');
        b.append(opacity.toString());
        return b.toString();
    }
}
