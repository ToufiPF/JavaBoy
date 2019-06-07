package ch.epfl.javaboy.component.lcd;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class LcdImageLineTest {
    
    public static void main(String[] args) {
        LcdImageLine l1 = new LcdImageLine(32);
        l1 = mapColorAndShift(l1, (byte) 0b1001_0011, 8);
        printNextLine(l1);
        l1 = mapColorAndShift(l1, (byte) 0b1001_0011, 8);
        printNextLine(l1);
        l1 = mapColorAndShift(l1, (byte) 0b1001_0011, 8);
        printNextLine(l1);
        l1 = mapColorAndShift(l1, (byte) 0b1001_0011, 8);
        printNextLine(l1);
    }
    private static LcdImageLine mapColorAndShift(LcdImageLine l, byte map, int dist) {
        return l.mapColors(map).shift(dist);
    }
    private static void printNextLine(LcdImageLine l) {
        System.out.println("    || ");
        System.out.println("    \\/");
        System.out.println(l);
    }
    
    @Test
    void test() {
        fail("Not yet implemented");
    }

}
