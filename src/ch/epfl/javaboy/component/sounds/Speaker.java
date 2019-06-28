package ch.epfl.javaboy.component.sounds;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.component.Component;

public final class Speaker implements Component {
    
    public static enum NR5 implements Register {
        NR50, NR51, NR52;
        public final static List<NR5> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }
    
    public static boolean addressInNr5(int address) {
        return AddressMap.REGS_NR5_START <= address && address < AddressMap.REGS_NR5_END;
    }
    
    private final Square1Channel square1;
    private final Square2Channel square2;
    private final WaveChannel wave;
    private final NoiseChannel noise;
    private final RegisterFile<NR5> NR5Regs;
    
    public Speaker() {
        square1 = new Square1Channel();
        square2 = new Square2Channel();
        wave = new WaveChannel();
        noise = new NoiseChannel();
        NR5Regs = new RegisterFile<NR5>(NR5.values());
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (Square1Channel.addressInRegs(address))
            return square1.read(address);
        if (Square2Channel.addressInRegs(address))
            return square2.read(address);
        if (WaveChannel.addressInRegs(address))
            return wave.read(address);
        if (NoiseChannel.addressInRegs(address))
            return noise.read(address);
        if (addressInNr5(address))
            return NR5Regs.get(NR5.ALL.get(address - AddressMap.REGS_NR5_START));
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);
        square1.write(address, value);
        square2.write(address, value);
        wave.write(address, value);
        noise.write(address, value);
    }
}
