package ch.epfl.javaboy.component.sounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;

final class WaveChannel implements Component, Clocked {
    public static enum NR3 implements Register {
        NR30, NR31, NR32, NR33, NR34;
        public final static List<NR3> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static boolean addressInRegs(int address) {
        return AddressMap.REGS_NR3_START <= address && address < AddressMap.REGS_NR3_END;
    }
    
    private final RegisterFile<NR3> NR3Regs;
    
    public WaveChannel() {
        NR3Regs = new RegisterFile<NR3>(NR3.values());
    }
    
    @Override
    public void cycle(long cycle) {
        
    }

    @Override
    public int read(int address) {
        if (!addressInRegs(address))
            return NO_DATA;
        return NR3Regs.get(NR3.ALL.get(address - AddressMap.REGS_NR3_START));
    }

    @Override
    public void write(int address, int value) {
        
    }
}
