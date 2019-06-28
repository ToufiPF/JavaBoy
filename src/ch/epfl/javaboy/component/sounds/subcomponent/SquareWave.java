package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.bits.Bits;

public class SquareWave {

    private static final int[] WAVE_FORM = {
            0b0000_0001, 0b1000_0001, 
            0b1000_0111, 0b0111_1110
    };
    
    private int wave;
    private int index;
    
    public SquareWave() {
        wave = 0;
        index = 0;
    }
    
    public void setDutyCycle(int duty) {
        wave = WAVE_FORM[duty];
        index = 0;
    }
    
    public void tick() {
        ++index;
        if (index >= Byte.SIZE)
            index = 0;
    }
    
    public boolean getOutput() {
        return Bits.test(wave, index);
    }
}