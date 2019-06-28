package ch.epfl.javaboy.component.sounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;

final class Square2Channel implements Component, Clocked {
    public static enum NR2 implements Register {
        UNUSED_20, NR21, NR22, NR23, NR24;
        public final static List<NR2> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static boolean addressInRegs(int address) {
        return AddressMap.REGS_NR2_START <= address && address < AddressMap.REGS_NR2_END;
    }
    
    private final RegisterFile<NR2> NR2Regs;
    
    public Square2Channel() {
        NR2Regs = new RegisterFile<NR2>(NR2.values());
    }

    @Override
    public int read(int address) {
        if (!addressInRegs(address))
            return NO_DATA;
        return NR2Regs.get(NR2.ALL.get(address - AddressMap.REGS_NR2_START));
    }

    @Override
    public void write(int address, int value) {
        
    }

    @Override
    public void cycle(long cycle) {
        
    }
}
