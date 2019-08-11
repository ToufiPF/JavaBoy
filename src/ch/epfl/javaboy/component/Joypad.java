package ch.epfl.javaboy.component;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.cpu.Cpu;
import ch.epfl.javaboy.component.cpu.Cpu.Interrupt;

import java.util.List;

/**
 * Represents the Joypad of a GameBoy
 * @author Toufi
 */
public final class Joypad implements Component {
    
    /**
     * Keys of the GameBoy
     * @author Toufi
     */
    public enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START;
        public static final List<Key> ALL = List.of(values());
        public static final int COUNT = ALL.size();
    }

    private final Cpu cpu;
    private int regP1;
    private int buttonStates;
    
    /**
     * Constructs a new Joypad
     * @param cpu (Cpu) the cpu of the GameBoy,
     * used to raise interruptions
     */
    public Joypad(Cpu cpu) {
        this.cpu = cpu;
        regP1 = 0;
        buttonStates = 0;
    }

    @Override
    public int read(int address) {
        if (address == AddressMap.REG_P1)
            return Bits.complement8(regP1);
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {
        if (address == AddressMap.REG_P1) {
            value = Bits.complement8(value);
            int lsb = Bits.clip(4, regP1);
            regP1 = (Bits.extract(value, 4, 2) << 4) | lsb;
            refreshP1();
        }
    }

    @Override
    public byte[] saveState() {
        byte[] state = new byte[2];
        state[0] = (byte) regP1;
        state[1] = (byte) buttonStates;
        return state;
    }

    @Override
    public void loadState(byte[] state) {
        if (state.length != 2)
            throw new IllegalStateException("Invalid state.");
        regP1 = Byte.toUnsignedInt(state[0]);
        buttonStates = Byte.toUnsignedInt(state[1]);
    }

    /**
     * To call when the given key was pressed
     * @param key (Joypad.Key) pressed key
     */
    public void keyPressed(Key key) {
        buttonStates = Bits.set(buttonStates, key.ordinal(), true);
        refreshP1();
    }

    /**
     * To call when the given key was released
     * @param key (Joypad.Key) released key
     */
    public void keyReleased(Key key) {
        buttonStates = Bits.set(buttonStates, key.ordinal(), false);
        refreshP1();
    }

    private void refreshP1() {
        int state = 0b0000;
        // Ligne 0
        if (Bits.test(regP1, 4)) {
            for (int i = 0 ; i < 4 ; ++i)
                if (Bits.test(buttonStates, i))
                    state |= Bits.mask(i);
        }
        // Ligne 1
        if (Bits.test(regP1, 5)) {
            for (int i = 0 ; i < 4 ; ++i)
                if (Bits.test(buttonStates, 4 + i))
                    state |= Bits.mask(i);
        }
        if (state != 0)
            cpu.requestInterrupt(Interrupt.JOYPAD);
        
        int msb = Bits.extract(regP1, 4, 4);
        regP1 = (msb << 4) | state;
    }
}
