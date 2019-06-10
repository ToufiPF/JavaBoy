package ch.epfl.javaboy.component.lcd;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Bus;
import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bit;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.cpu.Cpu;
import ch.epfl.javaboy.component.cpu.Cpu.Interrupt;
import ch.epfl.javaboy.component.memory.Ram;

public final class LcdController implements Component, Clocked {
    public static enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX;
        public static final List<Reg> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static enum Lcdc implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS;
        public static final List<Lcdc> ALL = Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static enum Stat implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC;
        public static final List<Stat> All = Collections.unmodifiableList(Arrays.asList(values()));
    }

    public static enum SpriteAttributes implements Bit {
        UNUSED0, UNUSED1, UNUSED2, UNUSED3, PALETTE, FLIP_H, FLIP_V, BEHIND_BG;
        public static final List<SpriteAttributes> All = Collections.unmodifiableList(Arrays.asList(values()));
    }

    private static enum Mode {
        MODE0(51), MODE1(114), MODE2(20), MODE3(43);
        public static final List<Mode> ALL = Collections.unmodifiableList(Arrays.asList(values()));

        private final int duration;

        private Mode(int duration) {
            this.duration = duration;
        }
        public int duration() {
            return duration;
        }
    }

    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;

    private static final int TILE_SIZE = 8;
    private static final int NB_TILES = 32;
    private static final int ALL_TILES_SIZE = 256;

    private static final int LY_OVERFLOW = LCD_HEIGHT + 10;

    private static final int BYTES_PER_TILE_LINE = 2, BYTES_PER_TILE = BYTES_PER_TILE_LINE * TILE_SIZE;

    private static final LcdImage BLANK_IMAGE = 
            new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT).build();
    private static final LcdImageLine BLANK_LINE = 
            new LcdImageLine.Builder(LCD_WIDTH).build();

    private static final int OFFSET_WX = -7;

    private final RegisterFile<Reg> vregs;
    private final Ram vRam, oam;
    private final Cpu cpu;
    private Bus bus;

    private LcdImage.Builder nextImageBuilder;
    private LcdImage current;
    private long nextNonIdleCycle;
    private boolean isHalted;
    private int winY;

    public LcdController(Cpu cpu) {
        vregs = new RegisterFile<>(Reg.values());
        vRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        oam = new Ram(AddressMap.OAM_RAM_SIZE);
        this.cpu = cpu;
        bus = null;
        
        nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
        current = BLANK_IMAGE;
        nextNonIdleCycle = 0;
        isHalted = true;
        winY = 0;
    }
    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }
    @Override
    public void cycle(long cycle) {
        if (isHalted && vregs.testBit(Reg.LCDC, Lcdc.LCD_STATUS)) {
            isHalted = false;
            nextNonIdleCycle = cycle;
        }

        if (isHalted || cycle < nextNonIdleCycle)
            return;

        int nextLine = vregs.get(Reg.LY);
        Mode nextMode = getMode();
        switch (getMode()) {
        case MODE2:
            nextMode = Mode.MODE3;
            nextImageBuilder.setLine(nextLine, createNewLine());
            break;
        case MODE3:
            nextMode = Mode.MODE0;
            break;
        case MODE0:
            ++nextLine;
            if (nextLine >= LCD_HEIGHT) {
                nextMode = Mode.MODE1;
                current = nextImageBuilder.build();
            } else {
                nextMode = Mode.MODE2;
            }
            break;
        case MODE1:
            ++nextLine;
            if (nextLine >= LY_OVERFLOW) {
                nextLine = 0;
                nextMode = Mode.MODE2;
                nextImageBuilder = new LcdImage.Builder(LCD_WIDTH, LCD_HEIGHT);
            }
            break;
        default:
            throw new Error();
        }
        setMode(nextMode);
        nextNonIdleCycle += nextMode.duration();
        writeToLycLy(Reg.LY, nextLine);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (AddressMap.REGS_LCDC_START <= address && address < AddressMap.REGS_LCDC_END)
            return vregs.get(addressToReg(address));

        if (AddressMap.VIDEO_RAM_START <= address && address < AddressMap.VIDEO_RAM_END)
            return vRam.read(address - AddressMap.VIDEO_RAM_START);

        if (AddressMap.OAM_START <= address && address < AddressMap.OAM_END)
            return oam.read(address - AddressMap.OAM_START);

        return NO_DATA;
    }
    @Override
    public void write(int address, int value) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);

        if (AddressMap.REGS_LCDC_START <= address && address < AddressMap.REGS_LCDC_END)
            writeToReg(address, value);
        else if (AddressMap.VIDEO_RAM_START <= address && address < AddressMap.VIDEO_RAM_END)
            vRam.write(address - AddressMap.VIDEO_RAM_START, value);
        else if (AddressMap.OAM_START <= address && address < AddressMap.OAM_END)
            oam.write(address - AddressMap.OAM_START, value);
    }

    public LcdImage currentImage() {
        return current;
    }

    private LcdImageLine createNewLine() {
        int ly = vregs.get(Reg.LY);
        int bgLineIndex = (vregs.get(Reg.SCY) + ly) % ALL_TILES_SIZE;
        LcdImageLine lcdLine = backgroundLine(bgLineIndex);
        lcdLine = addWindowLine(lcdLine, ly);
        return lcdLine;
    }


    /* Drawing Methods */

    private LcdImageLine backgroundLine(int line) {
        if (vregs.testBit(Reg.LCDC, Lcdc.BG))
            return computeBackgroundLine(line);
        return BLANK_LINE;
    }
    private LcdImageLine addWindowLine(LcdImageLine lcdLine, int line) {
        if (windowIsOn() && line >= vregs.get(Reg.WY)) {
            LcdImageLine winLine = computeWindowLine(winY);
            lcdLine = lcdLine.join(winLine, wx());
            winY = (winY + 1) % ALL_TILES_SIZE;
        }
        return lcdLine;
    }

    private LcdImageLine computeLine(int line, Bit bgOrWin_area) {
        LcdImageLine.Builder lcdB = new LcdImageLine.Builder(ALL_TILES_SIZE);
        for (int x = 0 ; x < ALL_TILES_SIZE / Byte.SIZE ; ++x) {
            int id = getIdTile(x, line / TILE_SIZE, bgOrWin_area);
            int msb_lsb = readTileMSBLSB(id, line % TILE_SIZE);
            lcdB.setBytes(x, Bits.extract(msb_lsb, Byte.SIZE, Byte.SIZE), Bits.clip(Byte.SIZE, msb_lsb));
        }
        return lcdB.build();
    }
    private LcdImageLine computeBackgroundLine(int line) {
        LcdImageLine l = computeLine(line, Lcdc.BG_AREA);
        return l.extractWrapped(vregs.get(Reg.SCX), LCD_WIDTH)
                .mapColors((byte) vregs.get(Reg.BGP));
    }
    private LcdImageLine computeWindowLine(int line) {
        LcdImageLine l = computeLine(line, Lcdc.WIN_AREA);
        return l.extractWrapped(0, LCD_WIDTH).shift(wx())
                .mapColors((byte) vregs.get(Reg.BGP));
    }


    /* General Utilities */

    private int getIdTile(int xTile, int yTile, Bit b) {
        int id = xTile + yTile * NB_TILES;
        int area = vregs.testBit(Reg.LCDC, b) ? 1 : 0;
        return read(AddressMap.BG_DISPLAY_DATA[area] + id);
    }
    private int readTileMSBLSB(int idTile, int line) {
        boolean tile_source = vregs.testBit(Reg.LCDC, Lcdc.TILE_SOURCE);
        int address = (!tile_source && idTile < 0x80 ? 0x9000 : 0x8000)
                + idTile * BYTES_PER_TILE + line * BYTES_PER_TILE_LINE;

        return Bits.make16(Bits.reverse8(read(address + 1)), Bits.reverse8(read(address)));
    }

    private int wx() {
        return vregs.get(Reg.WX) + OFFSET_WX;
    }
    private boolean windowIsOn() {
        int wx = wx();
        return vregs.testBit(Reg.LCDC,  Lcdc.WIN) && 0 <= wx && wx < LCD_WIDTH;
    }

    private Mode getMode() {
        return Mode.ALL.get(Bits.extract(vregs.get(Reg.STAT), Stat.MODE0.index(), 2));
    }
    private void setMode(Mode mode) {
        if (!getMode().equals(mode)) {
            vregs.set(Reg.STAT, vregs.get(Reg.STAT) & ~(0b11) | mode.ordinal());

            if ((mode == Mode.MODE0 && vregs.testBit(Reg.STAT, Stat.INT_MODE0)) || 
                    (mode == Mode.MODE1 && vregs.testBit(Reg.STAT, Stat.INT_MODE1)) || 
                    (mode == Mode.MODE2 && vregs.testBit(Reg.STAT, Stat.INT_MODE2)))
                cpu.requestInterrupt(Interrupt.LCD_STAT);

            if (mode == Mode.MODE1)
                cpu.requestInterrupt(Interrupt.VBLANK);
        }
    }


    /* Registers IO Methods */

    private Reg addressToReg(int address) {
        return Reg.ALL.get(address - AddressMap.REGS_LCDC_START);
    }
    private void writeToReg(int address, int value) {
        Reg reg = addressToReg(address);
        switch (reg) {
        case LCDC:
            vregs.set(Reg.LCDC, value);
            if (!vregs.testBit(Reg.LCDC, Lcdc.LCD_STATUS)) {
                setMode(Mode.MODE0);
                writeToLycLy(Reg.LY, 0);
                isHalted = true;
            }
            break;
        case STAT:
            int lsb = Bits.clip(3, vregs.get(Reg.STAT));
            int msb = Bits.extract(value, 3, Byte.SIZE - 3) << 3;
            vregs.set(Reg.STAT, msb | lsb);
            break;
        case LYC:
            writeToLycLy(Reg.LYC, value);
            break;
        case LY:
            break;
        default:
            vregs.set(reg, value);
            break;  
        }
    }

    private void writeToLycLy(Reg lycOrLy, int val) {
        if (lycOrLy ==  Reg.LYC) {
            if (val != vregs.get(Reg.LYC)) {
                vregs.set(Reg.LYC, val);
                boolean equal = vregs.get(Reg.LYC) == vregs.get(Reg.LY);
                vregs.setBit(Reg.STAT, Stat.LYC_EQ_LY, equal);
                if (equal && vregs.testBit(Reg.STAT, Stat.INT_LYC))
                    cpu.requestInterrupt(Interrupt.LCD_STAT);
            }
        } else if (lycOrLy == Reg.LY) {
            if (val != vregs.get(Reg.LY)) {
                vregs.set(Reg.LY, val);
                boolean equal = vregs.get(Reg.LYC) == vregs.get(Reg.LY);
                vregs.setBit(Reg.STAT, Stat.LYC_EQ_LY, equal);
                if (equal && vregs.testBit(Reg.STAT, Stat.INT_LYC))
                    cpu.requestInterrupt(Interrupt.LCD_STAT);
            }

        } else {
            throw new IllegalArgumentException();
        }
    }
}