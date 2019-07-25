package ch.epfl.javaboy.component.sounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.sounds.subcomponent.*;

final class Square1Channel implements Component, Clocked {

    public enum NR1 implements Register {
        NR10, NR11, NR12, NR13, NR14;
        public final static List<NR1> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }
    
    public static boolean addressInRegs(int address) {
        return AddressMap.REGS_NR1_START <= address && address < AddressMap.REGS_NR1_END;
    }

    private final RegisterFile<NR1> NR1Regs;

    private final FrameSequencer fs;
    private final FrequencySweeper fsweep;
    private long period, timer;
    private final SquareWave wave;
    private final LengthCounter lc;
    private final VolumeEnvelope ve;

    private boolean channelEnabled;

    public Square1Channel() {
        NR1Regs = new RegisterFile<NR1>(NR1.values());

        fs = new FrameSequencer();
        fsweep = new FrequencySweeper();
        period = timer = 0;
        wave = new SquareWave();
        lc = new LengthCounter(64);
        ve = new VolumeEnvelope();

        channelEnabled = true;
    }

    @Override
    public int read(int address) {
        if (!addressInRegs(address))
            return NO_DATA;
        return NR1Regs.get(NR1.ALL.get(address - AddressMap.REGS_NR1_START));
    }
    @Override
    public void write(int address, int value) {

    }
    @Override
    public void cycle(long cycle) {
        fs.cycle(cycle);
        if (fs.enable128Hz())
            fsweep.tick();

        int newFreq = fsweep.getChannelFrequency();
        boolean freqValid = FrequencySweeper.overflowCheck(newFreq);
        if (!freqValid)
            throw new Error(); // TODO: Disable Channel
        if (newFreq != getChannelFrequency())
            writeChannelFrequency(newFreq);

        --timer;
        if (timer <= 0) {
            timer = period;
            wave.tick();
        }

        if (fs.enable256Hz())
            lc.tick();
        if (fs.enable64Hz())
            ve.tick();
    }


    private int getChannelFrequency() {
        return (Bits.extract(NR1Regs.get(NR1.NR14), Sound.NRX4Bits.FREQUENCY_MSB_START,
                Sound.NRX4Bits.FREQUENCY_MSB_SIZE) << Byte.SIZE) | NR1Regs.get(NR1.NR13);
    }
    private void writeChannelFrequency(int freq) {
        int msb = Bits.extract(freq, Byte.SIZE, Sound.NRX4Bits.FREQUENCY_MSB_SIZE);
        int nr14 = NR1Regs.get(NR1.NR14) & (Bits.fullmask(Byte.SIZE - Sound.NRX4Bits.FREQUENCY_MSB_SIZE) << Sound.NRX4Bits.FREQUENCY_MSB_SIZE);

        NR1Regs.set(NR1.NR13, Bits.clip(freq, Byte.SIZE));
        NR1Regs.set(NR1.NR14, nr14 | msb);

        period = GameBoy.CYCLES_PER_SECOND / freq;
    }
}
