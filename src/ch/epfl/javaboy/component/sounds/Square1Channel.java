package ch.epfl.javaboy.component.sounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;

final class Square1Channel implements Component, Clocked {

    public static enum NR1 implements Register {
        NR10, NR11, NR12, NR13, NR14;
        public final static List<NR1> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }
    
    public static boolean addressInRegs(int address) {
        return AddressMap.REGS_NR1_START <= address && address < AddressMap.REGS_NR1_END;
    }

    private final RegisterFile<NR1> NR1Regs;

    public Square1Channel() {
        NR1Regs = new RegisterFile<NR1>(NR1.values());
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

    }    
}
