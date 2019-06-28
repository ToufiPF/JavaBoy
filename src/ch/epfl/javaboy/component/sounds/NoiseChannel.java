package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;

public class NoiseChannel implements Component, Clocked {
    public static enum NR4 implements Register {
        UNUSED_40, NR41, NR42, NR43, NR44
    }

    @Override
    public void cycle(long cycle) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int read(int address) {
        // TODO Auto-generated method stub
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {
        // TODO Auto-generated method stub
        
    }
}
