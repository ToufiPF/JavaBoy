package ch.epfl.javaboy.bits;

import java.util.List;

public class BitVectorTest {
    public static void main(String[] args) {
        BitVector.Builder build = new BitVector.Builder(64);
        for (int i = 0 ; i < 8 ; ++i)
            build.setByte(i, (byte)(i - 4));
        
        BitVector vA = build.build();
        System.out.println(vA.shift(0).shift(15));
        
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = v1.extractZeroExtended(-17, 32).not();
        BitVector v3 = v2.extractWrapped(11, 64);
        for (BitVector v: List.of(v1, v2, v3))
          System.out.println(v);
    }
}
