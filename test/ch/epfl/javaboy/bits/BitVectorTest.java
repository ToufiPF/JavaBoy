package ch.epfl.javaboy.bits;

public class BitVectorTest {
    public static void main(String[] args) {
        BitVector v1 = new BitVector(Integer.SIZE, true);
        BitVector v2 = v1.extractZeroExtended(-16, 32);
        System.out.println(v1);
        System.out.println(v2);
        BitVector v4 = v2.extractWrapped(15, 96);
        System.out.println(v4);
        
    }
}
