package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.component.Clocked;

public class Timer implements Clocked {
    
    /**
     * Creates a new SoundTimer with the
     * given frequency
     * @param freq (int) freq in Hz
     * @return (SoundTimer) the timer
     */
    public static Timer fromFrequency(int freq) {
        Preconditions.checkArgument(freq > 0);
        long period = GameBoy.CYCLES_PER_SECOND / freq;
        return new Timer(period);
    }
    
    /**
     * Creates a new SoundTimer with the
     * given period in cycles (4ticks = 1 cycle).
     * Frequency of a GameBoy : 2^20 cycles/seconds
     * @param cycles (long) period of the timer in cycles
     * @return (SoundTimer) the timer
     */
    public static Timer fromPeriodCycles(long cycles) {
        Preconditions.checkArgument(cycles > 0);
        return new Timer(cycles);
    }
    
    /**
     * Creates a new SoundTimer with the
     * given period in ticks (4ticks = 1 cycle).
     * Frequency of a GameBoy : 2^22 ticks/seconds
     * @param ticks (long) period of the timer in ticks
     * @return (SoundTimer) the timer
     */
    public static Timer fromPeriodTicks(long ticks) {
        return new Timer(ticks >> 2);
    }
    
    private final long period;
    private long count;
    
    private Timer(long period) {        
        this.period = period;
        count = period;
    }
    
    @Override
    public void cycle(long cycle) {
        --count;
        if (count < 0)
            count = period;
    }
    
    /**
     * Returns true when the internal counter reaches 0.
     * @return (boolean) true if the value of the
     * counter is 0, false otherwise
     */
    public boolean enable() {
        return count == 0;
    }
}