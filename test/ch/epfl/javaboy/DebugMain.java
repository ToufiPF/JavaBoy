package ch.epfl.javaboy;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.cpu.Cpu;
import ch.epfl.javaboy.component.lcd.LcdImage;

public final class DebugMain {

    private static final int[] COLOR_MAP = new int[] {
            0xFF_FF_FF, 0xD3_D3_D3, 0xA9_A9_A9, 0x00_00_00
    };
    
    public static final String[] romNames1 = {
            "01-special.gb", "02-interrupts.gb", "03-op sp,hl.gb",
            "04-op r,imm.gb", "05-op rp.gb", "06-ld r,r.gb",
            "07-jr,jp,call,ret,rst.gb", "08-misc instrs.gb",
            "09-op r,r.gb", "10-bit ops.gb", "11-op a,(hl).gb",
            "instr_timing.gb"
    };
    
    public static final String[] romNames2 = {
            "07-jr,jp,call,ret,rst.gb", "flappyboy.gb", "tetris.gb"
    };

    public static void main(String[] args) throws IOException {

        if (args.length >= 1) {
            runTest1(new File(args[0]));
        } else {
            /*
            for (int i = 0 ; i < romNames1.length ; ++i)
                runTest2(new File("Roms/" + romNames1[i]));
            */
            runTest2(new File("Roms/" + romNames2[2]));
        }
    }

    public static void runTest1(File romFile) throws IOException {
        System.err.println("Running Test1 " + romFile.getName() + " :");
        final long cycles = 30_000_000L;

        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        Component printer = new DebugPrinter();
        printer.attachTo(gb.bus());
        while (gb.cycles() < cycles) {
            long nextCycles = Math.min(gb.cycles() + 17556, cycles);
            gb.runUntil(nextCycles);
            gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
        }
        System.out.println("");
    }

    public static void runTest2(File romFile) throws IOException {
        System.err.println("Running Test2 " + romFile.getName() + " :");
        final long cycles = 30_000_000L;
        
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        gb.runUntil(cycles);

        System.out.println("+--------------------+");
        for (int y = 0; y < 18; ++y) {
            System.out.print("|");
            for (int x = 0; x < 20; ++x) {
                char c = (char) gb.bus().read(0x9800 + 32*y + x);
                System.out.print(Character.isISOControl(c) ? " " : c);
            }
            System.out.println("|");
        }
        System.out.println("+--------------------+");

        LcdImage li = gb.lcdController().currentImage();
        BufferedImage i =
                new BufferedImage(li.width(),
                        li.height(),
                        BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < li.height(); ++y)
            for (int x = 0; x < li.width(); ++x)
                i.setRGB(x, y, COLOR_MAP[li.getColor(x, y)]);
        ImageIO.write(i, "png", new File("gb.png"));
    }
}

