package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.sounds.subcomponent.FrameSequencer;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Mixer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class SoundController implements Component, Clocked {

    public enum NR5 implements Register {
        NR50, NR51, NR52;
        public final static List<NR5> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }
    
    public static boolean addressInNr5(int address) {
        return AddressMap.REGS_NR5_START <= address && address < AddressMap.REGS_NR5_END;
    }

    private static boolean extractSoundCountrollerEnabled(int nr52) {
        return Bits.test(nr52, Sound.NR52Bits.POWER_CONTROL_BIT);
    }

    private final static int CHANNEL_COUNT = 4;

    private final FrameSequencer fs;

    private final Channel[] channels;
    private final RegisterFile<NR5> NR5Regs;

    private boolean controllerEnabled;

    private final SoundOutput output;

    public SoundController(SoundOutput soundOutput) {
        fs = new FrameSequencer();
        channels = new Channel[CHANNEL_COUNT];

        channels[0] = new Square1Channel(fs);
        channels[1] = new Square2Channel(fs);
        channels[2] = new WaveChannel(fs);
        channels[3] = new NoiseChannel(fs);
        NR5Regs = new RegisterFile<>(NR5.values());

        controllerEnabled = false;
        output = soundOutput;
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        int id = address - AddressMap.REGS_NR1_START;
        if (Square1Channel.addressInRegs(address))
            return channels[0].read(address) | Sound.registerReadingMasks[id];
        if (Square2Channel.addressInRegs(address))
            return channels[1].read(address) | Sound.registerReadingMasks[id];
        if (WaveChannel.addressInRegs(address))
            return channels[2].read(address) | Sound.registerReadingMasks[id];
        if (NoiseChannel.addressInRegs(address))
            return channels[3].read(address) | Sound.registerReadingMasks[id];
        if (addressInNr5(address))
            return NR5Regs.get(NR5.ALL.get(address - AddressMap.REGS_NR5_START))
                    | Sound.registerReadingMasks[id];
        if (WaveChannel.addressInWaveRam(address))
            return channels[2].read(address);

        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);

        if (addressInNr5(address)) {
            NR5 reg = NR5.ALL.get(address - AddressMap.REGS_NR5_START);
            if (reg == NR5.NR52) {
                if (controllerEnabled && !extractSoundCountrollerEnabled(value))
                    powerOffSoundController();
                else if (!controllerEnabled && extractSoundCountrollerEnabled(value))
                    powerOnSoundController();

                controllerEnabled = extractSoundCountrollerEnabled(value);
                NR5Regs.set(NR5.NR52, (0xF0 & value) | (0xF & NR5Regs.get(NR5.NR52)));
            } else if (controllerEnabled) {
                NR5Regs.set(reg, value);
            }
            return;
        }
        if (WaveChannel.addressInWaveRam(address)) {
            channels[2].write(address, value);
        }
        if (controllerEnabled) {
            for (Channel c : channels)
                c.write(address, value);
        }
    }

    @Override
    public void cycle(long cycle) {
        if (!controllerEnabled)
            return;
        fs.cycle(cycle);

        for (Channel c : channels)
            c.cycle(cycle);

        final int selection = NR5Regs.get(NR5.NR51);
        int left = 0, right = 0;
        for (int i = 0 ; i < CHANNEL_COUNT ; ++i) {
            if (Bits.test(selection, CHANNEL_COUNT + i))
                left += channels[i].getOutput();
            if (Bits.test(selection, i))
                right += channels[i].getOutput();
        }
        left /= CHANNEL_COUNT;
        right /= CHANNEL_COUNT;

        final int volumes = NR5Regs.get(NR5.NR50);
        left *= Bits.extract(volumes, Sound.NR50Bits.LEFT_VOLUME_START, Sound.NR50Bits.LEFT_VOLUME_SIZE) + 1;
        right *= Bits.extract(volumes, Sound.NR50Bits.RIGHT_VOLUME_START, Sound.NR50Bits.RIGHT_VOLUME_SIZE) + 1;

        output.play(left, right);
    }

    private void powerOffSoundController() {
        for (int i = AddressMap.REGS_NR1_START ; i < AddressMap.REGS_NR4_END ; ++i)
            write(i, 0);
        NR5Regs.set(NR5.NR50, 0);
        NR5Regs.set(NR5.NR51, 0);
        output.stop();
    }
    private void powerOnSoundController() {
        fs.reset();

        for (Channel c : channels)
            c.reset();
        output.start();
    }
}
