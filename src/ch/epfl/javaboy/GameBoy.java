package ch.epfl.javaboy;

import ch.epfl.javaboy.component.memory.Ram;
import ch.epfl.javaboy.component.memory.RamController;

/**
 * Represents a GameBoy
 * Contains and links all necessary components
 * @author Toufi
 */
public final class GameBoy {
    private final Bus serial;
    private final Ram workRam;
    private final RamController workRamCtrl;
    private final RamController echoRamCtrl;
    
    public GameBoy(Object cardridge) {
        serial = new Bus();
        workRam = new Ram(AddressMap.WORK_RAM_SIZE);
        workRamCtrl = new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        workRamCtrl.attachTo(serial);
        echoRamCtrl = new RamController(workRam, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        echoRamCtrl.attachTo(serial);
    }
    
    public Bus bus() {
        return serial;
    }
}
