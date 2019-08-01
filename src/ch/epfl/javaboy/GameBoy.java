package ch.epfl.javaboy;

import java.util.Objects;

import ch.epfl.javaboy.component.Joypad;
import ch.epfl.javaboy.component.Timer;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.cpu.Cpu;
import ch.epfl.javaboy.component.lcd.LcdController;
import ch.epfl.javaboy.component.memory.BootRomController;
import ch.epfl.javaboy.component.memory.Ram;
import ch.epfl.javaboy.component.memory.RamController;
import ch.epfl.javaboy.component.sounds.AudioSystemSoundOutput;
import ch.epfl.javaboy.component.sounds.DebugSoundOutput;
import ch.epfl.javaboy.component.sounds.SoundController;

/**
 * Represents a GameBoy
 * Contains and links all necessary components
 * @author Toufi
 */
public final class GameBoy {
    
    public static final long CYCLES_PER_SECOND = 1L << 20;
    public static final double CYCLES_PER_NANO_SECOND = CYCLES_PER_SECOND / 1e9;
    
    private final Bus bus;
    private final Cpu cpu;
    private final LcdController lcd;
    private final SoundController soundController;
    private final Joypad joypad;
    
    private final Timer timer;
    
    private final BootRomController bootRomCtrl;
    
    private final Ram workRam;
    private final RamController workRamCtrl;
    private final RamController echoRamCtrl;

    private long simulatedCycles;
    
    /**
     * Constructs a new GameBoy with
     * the given cartridge
     * @param cartridge
     */
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        bus = new Bus();
        cpu = new Cpu();
        cpu.attachTo(bus);
        lcd = new LcdController(cpu);
        lcd.attachTo(bus);
        soundController = new SoundController(new AudioSystemSoundOutput());
        soundController.attachTo(bus);
        joypad = new Joypad(cpu);
        joypad.attachTo(bus);
        
        timer = new Timer(cpu);
        timer.attachTo(bus);
        
        bootRomCtrl = new BootRomController(cartridge);
        bootRomCtrl.attachTo(bus);
        
        workRam = new Ram(AddressMap.WORK_RAM_SIZE);
        workRamCtrl = new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        workRamCtrl.attachTo(bus);
        echoRamCtrl = new RamController(workRam, AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        echoRamCtrl.attachTo(bus);
        
        simulatedCycles = 0;
    }
    
    /**
     * Returns the bus
     * @return (Bus) bus of the GameBoy
     */
    public Bus bus() {
        return bus;
    }
    
    /**
     * Returns the cpu
     * @return (Cpu) cpu of the GameBoy
     */
    public Cpu cpu() {
        return cpu;
    }
    
    /**
     * Returns the LcdController
     * @return (LcdController) lcd of the GameBoy
     */
    public LcdController lcdController() {
        return lcd;
    }

    /**
     * Returns the SoundController
     * @return (SoundController) sound of the GameBoy
     */
    public SoundController soundController() { return soundController; }

    /**
     * Returns the Joypad
     * @return (Joypad) joypad of the GameBoy
     */
    public Joypad joypad() {
        return joypad;
    }
    
    /**
     * Returns the timer
     * @return (Timer) timer of the GameBoy
     */
    public Timer timer() {
        return timer;
    }
    
    /**
     * Runs all clocked components until
     * the given cycle (excluded)
     * @param cycle (long) limit cycle
     * @throws IllegalArgumentException
     * if the given cycle has already been simulated
     */
    public void runUntil(long cycle) {
        if (cycle < simulatedCycles)
            throw new IllegalArgumentException("Cycle already simulated.");
        while (simulatedCycles < cycle) {
            timer.cycle(simulatedCycles);
            lcd.cycle(simulatedCycles);
            soundController.cycle(simulatedCycles);
            cpu.cycle(simulatedCycles);
            ++simulatedCycles;
        }
    }
    
    /**
     * Returns the number of simulated cycles
     * @return (long) cycles
     */
    public long cycles() {
        return simulatedCycles;
    }
}
