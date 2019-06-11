package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;

public class FrameSequencer implements Clocked {

    private final static int FRAME_SEQUENCER_BASE_FREQUENCY = 512;
    private final static int MAX_COUNT512 = 7;

    private static boolean isDivisibleByPowerOf2(int n, int power) {
        return (n & Bits.fullmask(power)) == 0;
    }

    private final SoundTimer timer512;
    private int count512;
    
    public FrameSequencer() {
        timer512 = SoundTimer.fromFrequency(FRAME_SEQUENCER_BASE_FREQUENCY);
        count512 = MAX_COUNT512;
    }

    @Override
    public void cycle(long cycle) {
        timer512.cycle(cycle);
        if (timer512.enable()) {
            --count512;
            if (count512 < 0)
                count512 = MAX_COUNT512;
        }
    }

    public boolean enable512Hz() {
        return timer512.enable();
    }

    public boolean enable256Hz() {
        return timer512.enable() && isDivisibleByPowerOf2(count512, 1);
    }

    public boolean enable128Hz() {
        return timer512.enable() && isDivisibleByPowerOf2(count512, 2);
    }

    public boolean enable64Hz() {
        return timer512.enable() && isDivisibleByPowerOf2(count512, 3);
    }
}
