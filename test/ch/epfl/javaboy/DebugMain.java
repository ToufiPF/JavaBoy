package ch.epfl.javaboy;

import java.io.File;
import java.io.IOException;

import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.cpu.Cpu;

public final class DebugMain {
    
    public static final String[] romNames = {
            "01-special.gb", "02-interrupts.gb", "03-op sp,hl.gb",
            "04-op r,imm.gb", "05-op rp.gb", "06-ld r,r.gb",
            "07-jr,jp,call,ret,rst.gb", "08-misc instrs.gb",
            "09-op r,r.gb", "10-bit ops.gb", "11-op a,(hl).gb",
            "instr_timing.gb"
    };
    
    public static void main(String[] args) throws IOException {
        
        if (args.length >= 1) {
            runTest(new File(args[0]));
        } else {
            for (int i = 0 ; i < romNames.length ; ++i)
                runTest(new File("Roms/" + romNames[i]));
        }
    }
    
    private static void runTest(File romFile) throws IOException {
        System.err.println("Running Test " + romFile.getName() + " :");
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
}

