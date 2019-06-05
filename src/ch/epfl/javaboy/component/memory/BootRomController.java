package ch.epfl.javaboy.component.memory;

import java.util.Objects;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.cartridge.Cartridge;

public class BootRomController implements Component {
    
    private final Cartridge cart;
    private final Rom bootRom;
    private boolean bootRomDisabled;
    
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
}