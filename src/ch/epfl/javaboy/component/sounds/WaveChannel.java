package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;

public class WaveChannel implements Component, Clocked {
    public static enum NR3 implements Register {
        NR30, NR31, NR32, NR33, NR34
    }

    @Override
    public void cycle(long cycle) {
        
    }

    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {
        
    }
}
