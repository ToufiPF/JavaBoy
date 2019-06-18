package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.sounds.Sound.NR10Bits;
import ch.epfl.javaboy.component.sounds.Sound.NR11Bits;
import ch.epfl.javaboy.component.sounds.Sound.NR12Bits;
import ch.epfl.javaboy.component.sounds.Sound.NR13Bits;
import ch.epfl.javaboy.component.sounds.Sound.NR14Bits;
import ch.epfl.javaboy.component.sounds.Sound.Reg;

public class Square1Channel {

    public static int readSweepPeriod(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR10), NR10Bits.SWEEP_PERIOD_START, NR10Bits.SWEEP_PERIOD_SIZE);
    }
    public static boolean readSweepNegation(RegisterFile<Sound.Reg> regs) {
        return Bits.test(regs.get(Reg.NR10), NR10Bits.SWEEP_NEGATE_BIT);
    }
    public static int readSweepShift(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR10), NR10Bits.SWEEP_SHIFT_START, NR10Bits.SWEEP_SHIFT_SIZE);
    }
    
    public static int readDutyWave(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR11), NR11Bits.DUTY_START, NR11Bits.DUTY_SIZE);
    }
    public static int readLengthLoad(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR11), NR11Bits.LENGTH_LOAD_START, NR11Bits.LENGTH_LOAD_SIZE);
    }
    
    public static int readStartingVolume(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR12), NR12Bits.STARTING_VOLUME_START, NR12Bits.STARTING_VOLUME_SIZE);
    }
    public static boolean readEnvelopeAddMode(RegisterFile<Sound.Reg> regs) {
        return Bits.test(regs.get(Reg.NR12), NR12Bits.ENVELOPE_ADD_MODE_BIT);
    }
    public static int readEnvelopePeriod(RegisterFile<Sound.Reg> regs) {
        return Bits.extract(regs.get(Reg.NR12), NR12Bits.ENVELOPE_PERIOD_START, NR12Bits.ENVELOPE_PERIOD_SIZE);
    }
    
    public static int readWaveFrequency(RegisterFile<Sound.Reg> regs) {
        int msb = Bits.extract(regs.get(Reg.NR14), NR14Bits.FREQUENCY_MSB_START, NR14Bits.FREQUENCY_MSB_SIZE);
        return (msb << Byte.SIZE) | regs.get(Reg.NR13);
    }
    public static void writeWaveFrequency(RegisterFile<Sound.Reg> regs, int waveFreq) {
        regs.set(Reg.NR13, Bits.clip(Byte.SIZE, waveFreq));
        int msb = Bits.extract(waveFreq, Byte.SIZE, NR14Bits.FREQUENCY_MSB_SIZE);
        int prev = Bits.extract(regs.get(Reg.NR14), NR14Bits.FREQUENCY_MSB_SIZE, 
                Byte.SIZE - NR14Bits.FREQUENCY_MSB_SIZE) << NR14Bits.FREQUENCY_MSB_SIZE;
        regs.set(Reg.NR14, prev | msb);
    }
    
    public static boolean readLengthEnable(RegisterFile<Sound.Reg> regs) {
        return Bits.test(regs.get(Reg.NR14), NR14Bits.LENGTH_ENABLE_BIT);
    }
    public static boolean readTrigger(RegisterFile<Sound.Reg> regs) {
        return Bits.test(regs.get(Reg.NR14), NR14Bits.TRIGGER_BIT);
    }
}
