package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.sounds.Sound;
import ch.epfl.javaboy.component.sounds.Square1Channel;

public class FrequencySweeper implements Clocked {
    
    private static final int MAX_FREQUENCY = 2047;

    private final RegisterFile<Sound.Reg> regs;
    private SoundTimer timer;
    private boolean enabled;
    private int shadowReg;
    
    public FrequencySweeper(RegisterFile<Sound.Reg> regFile) {
        regs = regFile;
        timer = SoundTimer.fromPeriodTicks(Square1Channel.readSweepPeriod(regs));
        enabled = true;
        shadowReg = 0;
    }
    
    @Override
    public void cycle(long cycle) {
        timer.cycle(cycle);
        
        if (timer.enable()) {
            shadowReg = Square1Channel.readWaveFrequency(regs);
            timer = SoundTimer.fromPeriodTicks(Square1Channel.readSweepPeriod(regs));
            enabled = Square1Channel.readSweepPeriod(regs) != 0 || Square1Channel.readSweepShift(regs) != 0;
            if (!enabled)
                return;
            
            int newFreq = computeNewFreq();
            
            if (isOverflow(newFreq)) {
                enabled = false;
            } else {
                shadowReg = newFreq;
                Square1Channel.writeWaveFrequency(regs, shadowReg);
            }
        }
    }

    private int computeNewFreq() {
        int addFreq = shadowReg >>> Square1Channel.readSweepShift(regs);
        if (Square1Channel.readSweepNegation(regs))
            addFreq = -addFreq;
        return shadowReg + addFreq;
    }
    
    private boolean isOverflow(int freq) {
        return !(0 <= freq && freq <= MAX_FREQUENCY);
    }
}
