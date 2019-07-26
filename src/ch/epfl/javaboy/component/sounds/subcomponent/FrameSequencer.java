package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;

public class FrameSequencer implements Clocked {

    private final static int FRAME_SEQUENCER_BASE_FREQUENCY = 512;
    private final static int MAX_COUNT512 = 7;

    private static boolean isDivisibleByPowerOf2(int n, int power) {
        return (n & Bits.fullmask(power)) == 0;
    }

    private final long period;
    private long timer;
    private int count512;
    
    public FrameSequencer() {
        period = GameBoy.CYCLES_PER_SECOND / FRAME_SEQUENCER_BASE_FREQUENCY;
        timer = period;
        count512 = 0;
    }
    public void reset() {
        timer = period;
        count512 = 0;
    }

    @Override
    public void cycle(long cycle) {
        --timer;
        if (timer == 0) {
            timer = period;
            ++count512;
            if (count512 > MAX_COUNT512)
                count512 = 0;
        }
    }

    public boolean enable512Hz() {
        return timer == 0;
    }
    public boolean enable256Hz() {
        return enable512Hz() && isDivisibleByPowerOf2(count512, 1);
    }
    public boolean enable128Hz() {
        return enable512Hz() && isDivisibleByPowerOf2(count512, 2);
    }
    public boolean enable64Hz() {
        return enable512Hz() && count512 == MAX_COUNT512;
    }
}
