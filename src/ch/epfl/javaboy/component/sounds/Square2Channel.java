package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;

public class Square2Channel implements Component, Clocked {
    public static enum NR2 implements Register {
        UNUSED_20, NR21, NR22, NR23, NR24
    }

    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {
        
    }

    @Override
    public void cycle(long cycle) {
        
    }
}
