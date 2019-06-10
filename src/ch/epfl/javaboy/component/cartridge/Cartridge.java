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

            byte type = data[CARTRIGDE_TYPE_ADDRESS];

            if (type == 0)
                return new Cartridge(new MBC0(new Rom(data)));

            if (type == 1 || type == 2 || type == 3) {
                byte byteRam = data[0x149];
                int ramSize;
                switch (byteRam) {
                case 0:
                    ramSize = 0;
                    break;
                case 1:
                    ramSize = 2_048;
                    break;
                case 2:
                    ramSize = 8_192;
                    break;
                case 3:
                    ramSize = 32_768;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid Rom");
                }
                return new Cartridge(new MBC1(new Rom(data), ramSize));
            }
            
            throw new IllegalArgumentException("Rom type non supported.");

        }
    }
    private final static int CARTRIGDE_TYPE_ADDRESS = 0x147;

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
