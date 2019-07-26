package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.*;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.sounds.subcomponent.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class Square1Channel implements Channel {

    public enum NR1 implements Register {
        NR10, NR11, NR12, NR13, NR14;
        public final static List<NR1> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }
    
    public static boolean addressInRegs(int address) {
        return AddressMap.REGS_NR1_START <= address && address < AddressMap.REGS_NR1_END;
    }

    private static final int MAX_LENGTH = 64;

    private static int extractSweepPeriod(int nr10) {
        return Bits.extract(nr10, Sound.NR10Bits.SWEEP_PERIOD_START, Sound.NR10Bits.SWEEP_PERIOD_SIZE);
    }
    private static boolean extractSweepNegate(int nr10) {
        return Bits.test(nr10, Sound.NR10Bits.SWEEP_NEGATE_BIT);
    }
    private static int extractSweepShift(int nr10) {
        return Bits.extract(nr10, Sound.NR10Bits.SWEEP_SHIFT_START, Sound.NR10Bits.SWEEP_SHIFT_SIZE);
    }

    private static int extractDutyCycle(int nr11) {
        return Bits.extract(nr11, Sound.NRX1Bits.DUTY_START, Sound.NRX1Bits.DUTY_SIZE);
    }
    private static int extractLengthLoad(int nr11) {
        return Bits.extract(nr11, Sound.NRX1Bits.LENGTH_LOAD_START, Sound.NRX1Bits.LENGTH_LOAD_SIZE);
    }

    private static int extractStartingVolume(int nr12) {
        return Bits.extract(nr12, Sound.NRX2Bits.STARTING_VOLUME_START, Sound.NRX2Bits.STARTING_VOLUME_SIZE);
    }
    private static boolean extractEnvelopeIncrement(int nr12) {
        return Bits.test(nr12, Sound.NRX2Bits.ENVELOPE_ADD_MODE_BIT);
    }
    private static int extractEnvelopePeriod(int nr12) {
        return Bits.extract(nr12, Sound.NRX2Bits.ENVELOPE_PERIOD_START, Sound.NRX2Bits.ENVELOPE_PERIOD_SIZE);
    }

    private static int extractChannelFrequency(int nr13, int nr14) {
        return (Bits.extract(nr14, Sound.NRX4Bits.FREQUENCY_MSB_START,
                Sound.NRX4Bits.FREQUENCY_MSB_SIZE) << Byte.SIZE) | nr13;
    }

    private static boolean extractLengthEnable(int nr14) {
        return Bits.test(nr14, Sound.NRX4Bits.LENGTH_ENABLE_BIT);
    }
    private static boolean extractTrigger(int nr14) {
        return Bits.test(nr14, Sound.NRX4Bits.TRIGGER_BIT);
    }

    private final RegisterFile<NR1> NR1Regs;

    private final FrameSequencer fs;
    private final FrequencySweeper fsweep;
    private long period, timer;
    private final SquareWave wave;
    private final LengthCounter lc;
    private final VolumeEnvelope ve;

    private boolean channelEnabled;

    public Square1Channel(FrameSequencer frameSequencer) {
        NR1Regs = new RegisterFile<>(NR1.values());

        fs = frameSequencer;
        fsweep = new FrequencySweeper();
        period = timer = 0;
        wave = new SquareWave();
        lc = new LengthCounter(MAX_LENGTH);
        ve = new VolumeEnvelope();

        channelEnabled = false;
    }
    @Override
    public void reset() {
        wave.reset();
        timer = period;
    }

    @Override
    public int read(int address) {
        if (!addressInRegs(address))
            return NO_DATA;
        return NR1Regs.get(NR1.ALL.get(address - AddressMap.REGS_NR1_START));
    }
    @Override
    public void write(int address, int value) {
        if (!addressInRegs(address))
            return;
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);

        NR1 reg = NR1.ALL.get(address - AddressMap.REGS_NR1_START);
        NR1Regs.set(reg, value);
        switch (reg) {
            case NR10:
                fsweep.setSweepPeriod(extractSweepPeriod(value));
                fsweep.setSweepNegate(extractSweepNegate(value));
                fsweep.setSweepShift(extractSweepShift(value));
                break;
            case NR11:
                wave.setDutyCycle(extractDutyCycle(value));
                lc.reload(MAX_LENGTH - extractLengthLoad(value));
                break;
            case NR12:
                ve.setStartingVolume(extractStartingVolume(value));
                ve.setIncrementMode(extractEnvelopeIncrement(value));
                ve.setEnvelopePeriod(extractEnvelopePeriod(value));
                break;
            case NR13:
                int freq = extractChannelFrequency(value, NR1Regs.get(NR1.NR14));
                period = GameBoy.CYCLES_PER_SECOND / (2048 - freq);
                break;
            case NR14:
                int freq2 = extractChannelFrequency(NR1Regs.get(NR1.NR13), value);
                period = GameBoy.CYCLES_PER_SECOND / (2048 - freq2);

                lc.setCountingEnabled(extractLengthEnable(value));
                if (extractTrigger(value))
                    trigger();
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + reg);
        }
    }
    @Override
    public void cycle(long cycle) {
        if (fs.enable128Hz())
            fsweep.tick();

        int newFreq = fsweep.getChannelFrequency();
        if (FrequencySweeper.isOverflow(newFreq))
            channelEnabled = false;
        else if (newFreq != extractChannelFrequency(NR1Regs.get(NR1.NR13), NR1Regs.get(NR1.NR14)))
            writeChannelFrequency(newFreq);

        --timer;
        if (timer <= 0) {
            timer = period;
            wave.tick();
        }

        if (fs.enable256Hz())
            lc.tick();
        if (!lc.channelEnabled())
            channelEnabled = false;

        if (fs.enable64Hz())
            ve.tick();
    }

    @Override
    public void trigger() {
        channelEnabled = true;
        fsweep.trigger();
        timer = period;
        lc.trigger();
        ve.trigger();
    }

    @Override
    public int getOutput() {
        return channelEnabled && wave.getOutput() && dacPowered() ? ve.getVolume() : 0;
    }

    private void writeChannelFrequency(int freq) {
        int msb = Bits.extract(freq, Byte.SIZE, Sound.NRX4Bits.FREQUENCY_MSB_SIZE);
        int nr14 = NR1Regs.get(NR1.NR14) & (Bits.fullmask(Byte.SIZE - Sound.NRX4Bits.FREQUENCY_MSB_SIZE)
                << Sound.NRX4Bits.FREQUENCY_MSB_SIZE);

        NR1Regs.set(NR1.NR13, Bits.clip(freq, Byte.SIZE));
        NR1Regs.set(NR1.NR14, nr14 | msb);

        period = GameBoy.CYCLES_PER_SECOND / (2048 - freq);
    }
    private boolean dacPowered() {
        return Bits.extract(NR1Regs.get(NR1.NR12), 3, 5) != 0;
    }
}
