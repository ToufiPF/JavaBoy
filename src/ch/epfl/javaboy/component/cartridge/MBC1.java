package ch.epfl.javaboy.component.cartridge;

import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.memory.Ram;
import ch.epfl.javaboy.component.memory.Rom;

import static ch.epfl.javaboy.Preconditions.checkBits16;
import static ch.epfl.javaboy.Preconditions.checkBits8;

final class MBC1 implements Component {
    private static final int RAM_ENABLE = 0xA;

    private enum Mode { MODE_0, MODE_1 }

    private final Rom rom;
    private final Ram ram;

    private boolean ramEnabled;
    private Mode mode;
    private int romLsb5, ramRom2;
    private final int romMask, ramMask;

    MBC1(Rom rom, int ramSize) {
        this.rom = rom;
        this.ram = new Ram(ramSize);

        this.ramEnabled = false;
        this.mode = Mode.MODE_0;
        this.romLsb5 = 1;
        this.ramRom2 = 0;

        this.romMask = rom.size() - 1;
        this.ramMask = ramSize - 1;
    }
    
    @Override
    public int read(int address) {
        switch (Bits.extract(checkBits16(address), 13, 3)) {
        case 0: case 1:
            return rom.read(romAddress(msb2(), 0, address));
        case 2: case 3:
            return rom.read(romAddress(ramRom2, romLsb5, address));
        case 5:
            return ramEnabled ? ram.read(ramAddress(address)) : 0xFF;
        default:
            return NO_DATA;
        }
    }

    @Override
    public void write(int address, int data) {
        checkBits8(data);
        switch (Bits.extract(checkBits16(address), 13, 3)) {
        case 0:
            ramEnabled = Bits.clip(4, data) == RAM_ENABLE;
            break;
        case 1:
            romLsb5 = Math.max(1, Bits.clip(5, data));
            break;
        case 2:
            ramRom2 = Bits.clip(2, data);
            break;
        case 3:
            mode = Bits.test(data, 0) ? Mode.MODE_1 : Mode.MODE_0;
            break;
        case 5:
            if (ramEnabled)
                ram.write(ramAddress(address), data);
            break;
        }
    }

    @Override
    public byte[] saveState() {
        byte[] state = new byte[ram.size() + 2];

        int statusByte1 = ((ramEnabled ? 1 : 0) << 1) | (mode == Mode.MODE_1 ? 1 : 0);
        int statusByte2 = (romLsb5 << 2) | ramRom2;
        state[0] = (byte) statusByte1;
        state[1] = (byte) statusByte2;

        System.arraycopy(ram.getData(), 0, state, 2, ram.size());
        return state;
    }

    @Override
    public void loadState(byte[] state) {
        if (state.length != ram.size() + 2)
            throw new IllegalStateException("Invalid state.");
        ramEnabled = Bits.test(state[0], 1);
        mode = Bits.test(state[0], 0) ? Mode.MODE_1 : Mode.MODE_0;

        System.arraycopy(state, 2, ram.getData(), 0, ram.size());
    }

    private int msb2() {
        switch (mode) {
        case MODE_0: return 0;
        case MODE_1: return ramRom2;
        default: throw new Error();
        }
    }

    private int romAddress(int b_20_19, int b_18_14, int b_13_0) {
        return ((b_20_19 << 19) | (b_18_14 << 14) | Bits.clip(14, b_13_0)) & romMask;
    }

    private int ramAddress(int b_12_0) {
        return ((msb2() << 13) | Bits.clip(13, b_12_0)) & ramMask;
    }
}
