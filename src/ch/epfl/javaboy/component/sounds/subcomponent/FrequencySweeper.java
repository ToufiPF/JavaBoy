package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.sounds.Sound;
import ch.epfl.javaboy.component.sounds.Square1Channel;

public class FrequencySweeper implements Clocked {
    
    private final RegisterFile<Sound.Reg> regs;
    private SoundTimer timer;
    private boolean enabled;
    private int shadowReg;
    
    public FrequencySweeper(RegisterFile<Sound.Reg> regFile) {
        regs = regFile;
        timer = SoundTimer.fromPeriodTicks(Square1Channel.getSweepPeriod(regs));
        enabled = false;
        shadowReg = 0;
    }
    
    @Override
    public void cycle(long cycle) {
        timer.cycle(cycle);
        
        if (timer.enable()) {
            int addFreq = Square1Channel.getSweepShift(regs);
        }
    }
}
