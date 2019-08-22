package ch.epfl.javaboy.component.memory;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.cartridge.Cartridge;

import java.util.Objects;

/**
 * Represents the Boot Rom controller of 
 * a GameBoy
 * @author Toufi
 */
public class BootRomController implements Component {
    
    private final Cartridge cart;
    private final Rom bootRom;
    private boolean bootRomDisabled;
    
    /**
     * Constructs a new BootromController
     * @param cartridge (Cartridge)
     */
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        cart = cartridge;
        bootRom = new Rom(BootRom.DATA);
        bootRomDisabled = false;
    }
    
    @Override
    public int read(int address) {
        if (!bootRomDisabled && AddressMap.BOOT_ROM_START <= address && address < AddressMap.BOOT_ROM_END)
            return bootRom.read(address);
        
        return cart.read(address);
    }

    @Override
    public void write(int address, int value) {
        if (address == AddressMap.REG_BOOT_ROM_DISABLE)
            bootRomDisabled = true;
        cart.write(address, value);
    }

    @Override
    public byte[] saveState() {
        byte[] state = new byte[1];
        state[0] = (byte) (bootRomDisabled ? 1 : 0);
        return state;
    }

    @Override
    public void loadState(byte[] state) {
        if (state.length != 1)
            throw new IllegalStateException("Invalid state.");
        bootRomDisabled = Bits.test(state[0], 0);
    }
}
