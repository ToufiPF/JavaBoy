package ch.epfl.javaboy.component.cartridge;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.memory.Rom;

public final class Cartridge implements Component {
    
    public static Cartridge ofFile(File romFile) throws IOException {
        try (InputStream is = new FileInputStream(romFile)) {
            byte[] data = is.readAllBytes();
            
            if (data[0x147] != 0)
                throw new IllegalArgumentException("Invalid type of Rom");
            
            return new Cartridge(new MBC0(new Rom(data)));
        }
    }
    
    private final Component mbc;
    
    private Cartridge(Component mbc) {
        this.mbc = mbc;
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return mbc.read(address);
    }

    @Override
    public void write(int address, int value) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);
        mbc.write(address, value);
    }
}
