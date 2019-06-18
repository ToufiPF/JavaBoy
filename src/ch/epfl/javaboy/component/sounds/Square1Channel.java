package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.sounds.Sound.NR10Bits;
import ch.epfl.javaboy.component.sounds.Sound.NR11Bits;
import ch.epfl.javaboy.component.sounds.Sound.NR12Bits;
import ch.epfl.javaboy.component.sounds.Sound.NR14Bits;
import ch.epfl.javaboy.component.sounds.Sound.Reg;

public class Square1Channel {

    public static int getSweepPeriod(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR10), NR10Bits.SWEEP_PERIOD_START, NR10Bits.SWEEP_PERIOD_SIZE);
    }
    public static boolean getSweepNegation(RegisterFile<Sound.Reg> regs) {
        return Bits.test(regs.get(Reg.NR10), NR10Bits.SWEEP_NEGATE_BIT);
    }
    public static int getSweepShift(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR10), NR10Bits.SWEEP_SHIFT_START, NR10Bits.SWEEP_SHIFT_SIZE);
    }
    
    public static int getDutyWave(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR11), NR11Bits.DUTY_START, NR11Bits.DUTY_SIZE);
    }
    public static int getLengthLoad(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR11), NR11Bits.LENGTH_LOAD_START, NR11Bits.LENGTH_LOAD_SIZE);
    }
    
    public static int getStartingVolume(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR12), NR12Bits.STARTING_VOLUME_START, NR12Bits.STARTING_VOLUME_SIZE);
    }
    public static boolean getEnvelopeAddMode(RegisterFile<Sound.Reg> regs) {
        return Bits.test(regs.get(Reg.NR12), NR12Bits.ENVELOPE_ADD_MODE_BIT);
    }
    public static int getEnvelopePeriod(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR12), NR12Bits.ENVELOPE_PERIOD_START, NR12Bits.ENVELOPE_PERIOD_SIZE);
    }
    
    public static int getWaveFrequency(RegisterFile<Sound.Reg> regs) {
        int msb = Bits.extract(regs.get(Reg.NR14), NR14Bits.FREQUENCY_MSB_START, NR14Bits.FREQUENCY_MSB_SIZE);
        return (msb << Byte.SIZE) | regs.get(Reg.NR13);
    }
}
