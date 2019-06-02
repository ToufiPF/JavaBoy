package ch.epfl.javaboy;

import ch.epfl.javaboy.component.memory.Ram;
import ch.epfl.javaboy.component.memory.RamController;

public final class GameBoy {
    private final Bus serial;
    private final Ram ram;
    private final RamController workRam;
    private final RamController echoRam;
    
    public GameBoy(Object cardridge) {
        serial = new Bus();
        ram = new Ram(0xFFFF);
        echoRam = new RamController(ram, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        echoRam.attachTo(serial);
        workRam = new RamController(ram, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        workRam.attachTo(serial);
    }
    
    public Bus bus() {
        return serial;
    }
}
