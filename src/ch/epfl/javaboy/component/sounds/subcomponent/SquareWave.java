package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.bits.Bits;

public class SquareWave implements Ticked {

    private static final int[] WAVE_FORM = {
            0b0000_0001, 0b1000_0001, 
            0b1000_0111, 0b0111_1110
    };

    // Regs values
    private long period = 0;

    // Internal Flags
    private int wave = WAVE_FORM[0];
    private int index = 0;
    private long timer = 0;

    @Override
    public void tick() {
        --timer;
        if (timer == 0) {
            timer = period;
            ++index;
            if (index >= Byte.SIZE)
                index = 0;
        }
    }

    public void setDutyCycle(int duty) {
        wave = WAVE_FORM[duty];
        index = 0;
    }
    public void setFrequency(int freq) {
        period = GameBoy.CYCLES_PER_SECOND / freq;
    }
    
    public boolean getOutput() {
        return Bits.test(wave, index);
    }
}
