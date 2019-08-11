package ch.epfl.javaboy;

import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Joypad;
import ch.epfl.javaboy.component.Timer;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.cpu.Cpu;
import ch.epfl.javaboy.component.lcd.LcdController;
import ch.epfl.javaboy.component.memory.BootRomController;
import ch.epfl.javaboy.component.memory.Ram;
import ch.epfl.javaboy.component.memory.RamController;
import ch.epfl.javaboy.component.sounds.AudioLineSoundOutput;
import ch.epfl.javaboy.component.sounds.SoundController;

import javax.sound.sampled.LineUnavailableException;
import java.io.*;
import java.util.Objects;

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
    private final Cartridge cartridge;

    private final RamController workRamCtrl;

    private long simulatedCycles;

    /**
     * Constructs a new GameBoy with
     * the given cartridge
     * @param cartridge (Cartridge)
     */
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.cartridge = cartridge;

        bus = new Bus();
        cpu = new Cpu();
        cpu.attachTo(bus);
        lcd = new LcdController(cpu);
        lcd.attachTo(bus);
        AudioLineSoundOutput soundOutput;
        try {
            soundOutput = new AudioLineSoundOutput();
        } catch (LineUnavailableException e) {
            throw new RuntimeException(e);
        }
        soundController = new SoundController(soundOutput);
        soundController.attachTo(bus);
        soundController.startAudio();
        joypad = new Joypad(cpu);
        joypad.attachTo(bus);
        
        timer = new Timer(cpu);
        timer.attachTo(bus);

        bootRomCtrl = new BootRomController(cartridge);
        bootRomCtrl.attachTo(bus);

        Ram workRam = new Ram(AddressMap.WORK_RAM_SIZE);
        workRamCtrl = new RamController(workRam, AddressMap.WORK_RAM_START, AddressMap.WORK_RAM_END);
        workRamCtrl.attachTo(bus);
        
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


    public void saveState(File saveFile) throws IOException {
        OutputStream os = new FileOutputStream(saveFile);
        byte[] buffer;

        buffer = cpu.saveState();
        os.write(Bits.decomposeInteger(buffer.length));
        os.write(buffer);

        buffer = lcd.saveState();
        os.write(Bits.decomposeInteger(buffer.length));
        os.write(buffer);

        buffer = soundController.saveState();
        os.write(Bits.decomposeInteger(buffer.length));
        os.write(buffer);

        buffer = joypad.saveState();
        os.write(Bits.decomposeInteger(buffer.length));
        os.write(buffer);

        buffer = timer.saveState();
        os.write(Bits.decomposeInteger(buffer.length));
        os.write(buffer);

        buffer = bootRomCtrl.saveState();
        os.write(Bits.decomposeInteger(buffer.length));
        os.write(buffer);

        buffer = cartridge.saveState();
        os.write(Bits.decomposeInteger(buffer.length));
        os.write(buffer);

        buffer = workRamCtrl.saveState();
        os.write(Bits.decomposeInteger(buffer.length));
        os.write(buffer);

        buffer = new byte[Long.BYTES];
        for (int i = 0 ; i < Long.BYTES ; ++i)
            buffer[i] = (byte) Bits.extract(simulatedCycles, i * Byte.SIZE, Byte.SIZE);
        os.write(buffer);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void loadState(File loadFile) throws IOException {
        InputStream is = new FileInputStream(loadFile);
        byte[] buffLength = new byte[Integer.BYTES];
        byte[] buffState;

        is.read(buffLength);
        buffState = new byte[Bits.recomposeInteger(buffLength)];
        is.read(buffState);
        cpu.loadState(buffState);

        is.read(buffLength);
        buffState = new byte[Bits.recomposeInteger(buffLength)];
        is.read(buffState);
        lcd.loadState(buffState);

        is.read(buffLength);
        buffState = new byte[Bits.recomposeInteger(buffLength)];
        is.read(buffState);
        soundController.loadState(buffState);

        is.read(buffLength);
        buffState = new byte[Bits.recomposeInteger(buffLength)];
        is.read(buffState);
        joypad.loadState(buffState);

        is.read(buffLength);
        buffState = new byte[Bits.recomposeInteger(buffLength)];
        is.read(buffState);
        timer.loadState(buffState);

        is.read(buffLength);
        buffState = new byte[Bits.recomposeInteger(buffLength)];
        is.read(buffState);
        bootRomCtrl.loadState(buffState);

        is.read(buffLength);
        buffState = new byte[Bits.recomposeInteger(buffLength)];
        is.read(buffState);
        cartridge.loadState(buffState);

        is.read(buffLength);
        buffState = new byte[Bits.recomposeInteger(buffLength)];
        is.read(buffState);
        workRamCtrl.loadState(buffState);

        buffState = new byte[Long.BYTES];
        is.read(buffState);
        simulatedCycles = 0L;
        for (int i = 0 ; i < Long.BYTES ; ++i)
            simulatedCycles |= Byte.toUnsignedLong(buffState[i]) << (i * Byte.SIZE);
    }
}
