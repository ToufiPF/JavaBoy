package ch.epfl.javaboy.component.lcd;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bit;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.cpu.Cpu;
import ch.epfl.javaboy.component.cpu.Cpu.Interrupt;
import ch.epfl.javaboy.component.memory.Ram;
import ch.epfl.javaboy.component.memory.RamController;

public final class LcdController implements Component, Clocked {
    public static enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX;
        public static final List<Reg> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static enum LCDCBits implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS;
        public static final List<LCDCBits> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static enum STATBits implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC;
        public static final List<STATBits> All = Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;

    private final RegisterFile<Reg> videoRegs;
    private final RamController videoRam;
    private final Cpu cpu;
    private LcdImage.Builder nextImageBuilder;
    private LcdImage current;
    private long nextNonIdleCycle;
    private boolean isHalted;

    public LcdController(Cpu cpu) {
        videoRegs = new RegisterFile<>(Reg.values());
        videoRam = new RamController(new Ram(AddressMap.VIDEO_RAM_SIZE),
                AddressMap.VIDEO_RAM_START, AddressMap.VIDEO_RAM_END);
        this.cpu = cpu;
        nextImageBuilder = null;
        current = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT).build();
        nextNonIdleCycle = 0;
        isHalted = false;
    }

    @Override
    public void cycle(long cycle) {
        if (isHalted || cycle < nextNonIdleCycle)
            return;

        int mode = getMode();
        int line = videoRegs.get(Reg.LY);

        switch (mode) {
        case 0:
            setMode(2);
            nextNonIdleCycle += 20;
            break;
        case 1:
            ++line;
            if (line > 153) {
                line = 0;
                setMode(2);
                nextNonIdleCycle += 20;
                cpu.requestInterrupt(Interrupt.VBLANK);
            }
            break;
        case 2:
            setMode(3);
            nextNonIdleCycle += 43;
            break;
        case 3:
            nextImageBuilder.setLine(line, new LcdImageLine(LCD_WIDTH));
            ++line;
            setMode(0);
            nextNonIdleCycle += 51;
            break;
        default:
            throw new Error();
        }
    }

    @Override
    public int read(int address) {
        Reg reg = getRegForAddress(address);

        if (reg != null)
            return videoRegs.get(reg);

        return videoRam.read(address);
    }

    @Override
    public void write(int address, int value) {
        Reg reg = getRegForAddress(address);

        if (reg != null) {
            if (reg == Reg.LCDC) {
                videoRegs.set(Reg.LCDC, value);
                if (!videoRegs.testBit(Reg.LCDC, LCDCBits.LCD_STATUS)) {
                    setMode(0);
                    videoRegs.set(Reg.LY, 0);
                    refreshLycEqLy();
                    isHalted = true;
                }
            } else if (reg == Reg.STAT) {
                int prev = videoRegs.get(Reg.STAT);
                int newVal = value & (Bits.fullmask(Byte.SIZE - 3) << 3);
                newVal |= prev & Bits.fullmask(3);
                videoRegs.set(Reg.STAT, newVal);
            } else if (reg == Reg.LYC) {
                videoRegs.set(Reg.LYC, value);
                refreshLycEqLy();
            } else {
                videoRegs.set(reg, value);
            }
        } else {
            videoRam.write(address, value);
        }
    }

    public LcdImage currentImage() {
        return current;
    }

    private Reg getRegForAddress(int address) {
        if (AddressMap.REGS_LCDC_START <= address && address < AddressMap.REGS_LCDC_END)
            return Reg.ALL.get(address - AddressMap.REGS_LCDC_START);

        return null;
    }

    private int getMode() {
        return Bits.extract(videoRegs.get(Reg.STAT), STATBits.MODE0.index(), 2);
    }

    private void setMode(int mode) {
        if (!(0 <= mode && mode < 4))
            throw new IllegalArgumentException();
        boolean bit0 = Bits.test(mode, 0);
        boolean bit1 = Bits.test(mode, 1);

        videoRegs.setBit(Reg.STAT, STATBits.MODE0, bit0);
        videoRegs.setBit(Reg.STAT, STATBits.MODE1, bit1);

        if ((mode == 0 && videoRegs.testBit(Reg.STAT, STATBits.INT_MODE0)) || 
                (mode == 1 && videoRegs.testBit(Reg.STAT, STATBits.INT_MODE1)) || 
                (mode == 2 && videoRegs.testBit(Reg.STAT, STATBits.INT_MODE2)))
            cpu.requestInterrupt(Interrupt.LCD_STAT);

        if (mode == 1)
            cpu.requestInterrupt(Interrupt.VBLANK);
    }

    private void refreshLycEqLy() {
        boolean equal = videoRegs.get(Reg.LYC) == videoRegs.get(Reg.LY);
        videoRegs.setBit(Reg.STAT, STATBits.LYC_EQ_LY, equal);
        if (equal && videoRegs.testBit(Reg.STAT, STATBits.INT_LYC))
            cpu.requestInterrupt(Interrupt.LCD_STAT);
    }
}
