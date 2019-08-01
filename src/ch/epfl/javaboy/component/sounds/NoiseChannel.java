package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.component.sounds.subcomponent.FrameSequencer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

final class NoiseChannel implements Channel {

    public static enum NR4 implements Register {
        UNUSED_40, NR41, NR42, NR43, NR44;
        public final static List<NR4> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }
    
    public static boolean addressInRegs(int address) {
        return AddressMap.REGS_NR4_START <= address && address < AddressMap.REGS_NR4_END;
    }
    
    private final RegisterFile<NR4> NR4Regs;

    private final FrameSequencer fs;
    
    public NoiseChannel(FrameSequencer frameSequencer) {
        NR4Regs = new RegisterFile<NR4>(NR4.values());

        fs = frameSequencer;
    }

    @Override
    public void reset() {

    }
    @Override
    public void cycle(long cycle) {
    }

    @Override
    public int read(int address) {
        if (!addressInRegs(address))
            return NO_DATA;
        return NR4Regs.get(NR4.ALL.get(address - AddressMap.REGS_NR4_START));
    }
    @Override
    public void write(int address, int value) {
    }

    @Override
    public void trigger() {

    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public int getOutput() {
        return 0;
    }
}
