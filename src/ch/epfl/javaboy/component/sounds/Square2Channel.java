package ch.epfl.javaboy.component.sounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javaboy.*;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.sounds.subcomponent.FrameSequencer;
import ch.epfl.javaboy.component.sounds.subcomponent.LengthCounter;
import ch.epfl.javaboy.component.sounds.subcomponent.SquareWave;
import ch.epfl.javaboy.component.sounds.subcomponent.VolumeEnvelope;

final class Square2Channel implements Channel {
    public static enum NR2 implements Register {
        UNUSED_20, NR21, NR22, NR23, NR24;
        public final static List<NR2> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static boolean addressInRegs(int address) {
        return AddressMap.REGS_NR2_START <= address && address < AddressMap.REGS_NR2_END;
    }

    private static int extractDutyCycle(int nr21) {
        return Bits.extract(nr21, Sound.NRX1Bits.DUTY_START, Sound.NRX1Bits.DUTY_SIZE);
    }
    private static int extractLengthLoad(int nr21) {
        return Bits.extract(nr21, Sound.NRX1Bits.LENGTH_LOAD_START, Sound.NRX1Bits.LENGTH_LOAD_SIZE);
    }

    private static int extractStartingVolume(int nr22) {
        return Bits.extract(nr22, Sound.NRX2Bits.STARTING_VOLUME_START, Sound.NRX2Bits.STARTING_VOLUME_SIZE);
    }
    private static boolean extractEnvelopeIncrement(int nr22) {
        return Bits.test(nr22, Sound.NRX2Bits.ENVELOPE_ADD_MODE_BIT);
    }
    private static int extractEnvelopePeriod(int nr22) {
        return Bits.extract(nr22, Sound.NRX2Bits.ENVELOPE_PERIOD_START, Sound.NRX2Bits.ENVELOPE_PERIOD_SIZE);
    }

    private static int extractChannelFrequency(int nr23, int nr24) {
        return (Bits.extract(nr24, Sound.NRX4Bits.FREQUENCY_MSB_START,
                Sound.NRX4Bits.FREQUENCY_MSB_SIZE) << Byte.SIZE) | nr23;
    }

    private static boolean extractLengthEnable(int nr24) {
        return Bits.test(nr24, Sound.NRX4Bits.LENGTH_ENABLE_BIT);
    }
    private static boolean extractTrigger(int nr24) {
        return Bits.test(nr24, Sound.NRX4Bits.TRIGGER_BIT);
    }

    private static final int MAX_LENGTH = 64;

    private final RegisterFile<NR2> NR2Regs;

    private final FrameSequencer fs;
    private long period, timer;
    private final SquareWave wave;
    private final LengthCounter lc;
    private final VolumeEnvelope ve;

    private boolean channelEnabled;
    
    public Square2Channel(FrameSequencer frameSequencer) {
        NR2Regs = new RegisterFile<NR2>(NR2.values());

        fs = frameSequencer;
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
        return NR2Regs.get(NR2.ALL.get(address - AddressMap.REGS_NR2_START));
    }

    @Override
    public void write(int address, int value) {
        if (!addressInRegs(address))
            return;
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);

        NR2 reg = NR2.ALL.get(address - AddressMap.REGS_NR2_START);
        switch (reg) {
            case NR21:
                wave.setDutyCycle(extractDutyCycle(value));
                lc.reload(MAX_LENGTH - extractLengthLoad(value));
                break;
            case NR22:
                ve.setStartingVolume(extractStartingVolume(value));
                ve.setIncrementMode(extractEnvelopeIncrement(value));
                ve.setEnvelopePeriod(extractEnvelopePeriod(value));
                break;
            case NR23:
                int freq = extractChannelFrequency(value, NR2Regs.get(NR2.NR24));
                period = GameBoy.CYCLES_PER_SECOND / (2048 - freq);
                break;
            case NR24:
                int freq2 = extractChannelFrequency(NR2Regs.get(NR2.NR23), value);
                period = GameBoy.CYCLES_PER_SECOND / (2048 - freq2);

                lc.setCountingEnabled(extractLengthEnable(value));
                if (extractTrigger(value))
                    trigger();
                break;
            default:
                break;
        }
        NR2Regs.set(reg, value);
    }

    @Override
    public void cycle(long cycle) {
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
    public int getOutput() {
        return channelEnabled && wave.getOutput() && dacPowered() ? ve.getVolume() : 0;
    }

    @Override
    public boolean isEnabled() {
        return channelEnabled;
    }

    @Override
    public void trigger() {
        channelEnabled = true;
        timer = period;
        lc.trigger();
        ve.trigger();
    }

    private boolean dacPowered() {
        return Bits.extract(NR2Regs.get(NR2.NR22), 3, 5) != 0;
    }
}
