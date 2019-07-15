package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.component.Clocked;

public class Timer implements Clocked {

    
    private long period;
    private long count;
    
    public Timer() {
        period = 0;
        count = 0;
    }

    /**
     * Sets the frequency of the timer
     * Frequency of a GameBoy : 2^20 cycles/seconds
     * @param freq (int) freq in Hz
     */
    public void setFrequency(int freq) {
        Preconditions.checkArgument(freq > 0);
        period = GameBoy.CYCLES_PER_SECOND / freq;
    }

    /**
     * Sets the period in cycles (4ticks = 1 cycle).
     * Frequency of a GameBoy : 2^20 cycles/seconds
     * @param cycles (long) period of the timer in cycles
     */
    public void setPeriodCycles(long cycles) {
        Preconditions.checkArgument(cycles > 0);
        period = cycles;
    }

    /**
     * Sets the period in ticks (4ticks = 1 cycle).
     * Frequency of a GameBoy : 2^22 ticks/seconds
     * @param ticks (long) period of the timer in ticks
     */
    public void setPeriodTicks(long ticks) {
        Preconditions.checkArgument(ticks > 0);
        period = ticks >> 2;
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
