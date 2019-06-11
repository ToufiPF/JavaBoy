package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.component.Clocked;

public class SoundTimer implements Clocked {
    
    public static SoundTimer fromFrequency(int freq) {
        Preconditions.checkArgument(freq > 0);
        long period = GameBoy.CYCLES_PER_SECOND / freq;
        return new SoundTimer(period);
    }
    public static SoundTimer fromPeriod(long periodCycles) {
        Preconditions.checkArgument(periodCycles > 0);
        return new SoundTimer(periodCycles);
    }
    
    private final long period;
    private long count;
    
    private SoundTimer(long period) {        
        this.period = period;
        count = period;
    }
    
    @Override
    public void cycle(long cycle) {
        --count;
        if (count < 0)
            count = period;
    }
    
    public boolean enable() {
        return count == 0;
    }
}
