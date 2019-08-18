package ch.epfl.javaboy.component.cartridge;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.memory.Rom;

/**
 * Represents a GameBoy cartridge
 * MBC supported : 0, 1
 * @author Toufi
 */
public final class Cartridge implements Component {
    
    /**
     * Creates a new Cartridge from the specified file
     * @param romFile (File) the rom file
     * @return (Cartridge) the rom cartridge
     * @throws IOException
     * if a problem occured when reading the romFile
     * @throws IllegalArgumentException
     * if the given rom is invalid or non-supported
     */
    public static Cartridge ofFile(File romFile) throws IOException {
        try (InputStream is = new FileInputStream(romFile)) {
            byte[] data = readAllBytes(is);

            byte type = data[CARTRIGDE_TYPE_ADDRESS];

            if (type == 0)
                return new Cartridge(new MBC0(new Rom(data)));

            if (type == 1 || type == 2 || type == 3) {
                byte byteRam = data[CARTRIGDE_RAM_SIZE_ADDRESS];
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
    
    private static byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        int nRead;
        final int SAMPLE_SIZE = 1 << 14;
        byte[] data = new byte[SAMPLE_SIZE];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
          buffer.write(data, 0, nRead);
        }

        return buffer.toByteArray();
    }
    
    private final static int CARTRIGDE_TYPE_ADDRESS = 0x147;
    private final static int CARTRIGDE_RAM_SIZE_ADDRESS = 0x149;

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

    @Override
    public byte[] saveState() {
        return mbc.saveState();
    }

    @Override
    public void loadState(byte[] state) {
        mbc.loadState(state);
    }
}
