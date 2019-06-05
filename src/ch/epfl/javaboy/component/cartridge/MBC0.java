package ch.epfl.javaboy.component.cartridge;

import java.util.Objects;

import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.memory.Rom;

public final class MBC0 implements Component {
    
    public static final int MBC0_SIZE = 32_768;
    
    private final Rom rom;
    
    public MBC0(Rom rom) {
        Objects.requireNonNull(rom);
        if (rom.size() != MBC0_SIZE)
            throw new IllegalArgumentException("Invalid rom size.");
        this.rom = rom;
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (0 <= address && address < MBC0_SIZE)
            return rom.read(address);
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {        
    }
}
