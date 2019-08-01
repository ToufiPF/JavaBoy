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
import ch.epfl.javaboy.component.sounds.subcomponent.ProgrammableWave;

final class WaveChannel implements Channel {

    public enum NR3 implements Register {
        NR30, NR31, NR32, NR33, NR34;
        public final static List<NR3> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }
    public final static int[] INIT_VALUES = {
            0x84, 0x40, 0x43, 0xAA,
            0x2D, 0x78, 0x92, 0x3C,
            0x60, 0x59, 0x59, 0xB0,
            0x34, 0xB8, 0x2E, 0xDA
    };

    public static boolean addressInRegs(int address) {
        return AddressMap.REGS_NR3_START <= address && address < AddressMap.REGS_NR3_END;
    }
    public static boolean addressInWaveRam(int address) {
        return AddressMap.REGS_WAVE_TABLE_START <= address && address < AddressMap.REGS_WAVE_TABLE_END;
    }

    private static int extractChannelFrequency(int nr33, int nr34) {
        return (Bits.extract(nr34, Sound.NRX4Bits.FREQUENCY_MSB_START,
                Sound.NRX4Bits.FREQUENCY_MSB_SIZE) << Byte.SIZE) | nr33;
    }
    private final static int MAX_LENGTH = 256;

    private final RegisterFile<NR3> NR3Regs;
    private final int[] waveRam;

    private final FrameSequencer fs;
    private long period, timer;
    private final ProgrammableWave wave;
    private final LengthCounter lc;

    private boolean channelEnabled;
    
    public WaveChannel(FrameSequencer frameSequencer) {
        NR3Regs = new RegisterFile<NR3>(NR3.values());
        waveRam = Arrays.copyOf(INIT_VALUES, INIT_VALUES.length);

        fs = frameSequencer;
        period = timer = 0;
        wave = new ProgrammableWave(waveRam);
        lc = new LengthCounter(MAX_LENGTH);
        channelEnabled = false;
    }

    @Override
    public void reset() {
        wave.reset();
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
    }

    @Override
    public int read(int address) {
        if (addressInRegs(address))
            return NR3Regs.get(NR3.ALL.get(address - AddressMap.REGS_NR3_START));
        if (addressInWaveRam(address))
            return NO_DATA;
        return NO_DATA;
    }
    @Override
    public void write(int address, int value) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);
        if (addressInRegs(address)) {
            NR3 reg = NR3.ALL.get(address - AddressMap.REGS_NR3_START);
            switch (reg) {
                case NR30:
                    break;
                case NR31:
                    lc.reload(MAX_LENGTH - value);
                    break;
                case NR32:
                    wave.setVolumeCode(Bits.extract(value,
                            Sound.NR32Bits.VOLUME_CODE_START, Sound.NR32Bits.VOLUME_CODE_SIZE));
                    break;
                case NR33:
                    int freq = extractChannelFrequency(value, NR3Regs.get(NR3.NR34));
                    period = GameBoy.CYCLES_PER_SECOND / (2 * (2048 - freq));
                    break;
                case NR34:
                    int freq2 = extractChannelFrequency(NR3Regs.get(NR3.NR33), value);
                    period = GameBoy.CYCLES_PER_SECOND / (2 * (2048 - freq2));

                    lc.setCountingEnabled(Bits.test(value, Sound.NRX4Bits.LENGTH_ENABLE_BIT));
                    if (Bits.test(value, Sound.NRX4Bits.TRIGGER_BIT))
                        trigger();
                    break;
                default:
                    throw new Error();
            }
            NR3Regs.set(reg, value);
        } else if (addressInWaveRam(address)) {
            waveRam[address - AddressMap.REGS_WAVE_TABLE_START] = value;
        }
    }
    @Override
    public void trigger() {
        channelEnabled = true;
        timer = period;
        wave.trigger();
        lc.trigger();
    }

    @Override
    public boolean isEnabled() {
        return channelEnabled;
    }

    @Override
    public int getOutput() {
        return channelEnabled && dacPowered() ? wave.getOutput() : 0;
    }

    private boolean dacPowered() {
        return Bits.test(NR3Regs.get(NR3.NR30), Sound.NR30Bits.DAC_POWER_BIT);
    }
}
