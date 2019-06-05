package ch.epfl.javaboy.component;

import java.util.Objects;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.cpu.Cpu;
import ch.epfl.javaboy.component.cpu.Cpu.Interrupt;

public final class Timer implements Component, Clocked {
    
    private static enum RegT implements Register {
        TIMA, TMA, TAC
    }
    
    private static final int MAX_MAIN_COUNTER = 0xFFFF;
    private static final int MAX_TIMA = 0xFF;
    private static final int UNITS_BY_CYCLE = 4;
    
    private final Cpu cpu;
    private final RegisterFile<RegT> regTimer;
    private int mainCounter;
    
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
        regTimer = new RegisterFile<>(RegT.values());
        mainCounter = 0;
    }
    
    @Override
    public void cycle(long cycle) {
        boolean previousState = computeState();
        
        mainCounter += UNITS_BY_CYCLE;
        if (mainCounter > MAX_MAIN_COUNTER)
            mainCounter = 0;
        
        boolean currState = computeState();
        incrementIfFallingEdge(previousState, currState);
    }
    
    @Override
    public int read(int address) {
        if (address == AddressMap.REG_DIV)
            return Bits.extract(mainCounter, Byte.SIZE, Byte.SIZE);
        else if (address == AddressMap.REG_TIMA)
            return regTimer.get(RegT.TIMA);
        else if (address == AddressMap.REG_TAC)
            return regTimer.get(RegT.TAC);
        else if (address == AddressMap.REG_TMA)
            return regTimer.get(RegT.TMA);
        
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);

        if (address == AddressMap.REG_DIV) {
            boolean prev = computeState();
            mainCounter = value << Byte.SIZE;
            incrementIfFallingEdge(prev, computeState());
            
        } else if (address == AddressMap.REG_TIMA) {
            regTimer.set(RegT.TIMA, value);
            
        } else if (address == AddressMap.REG_TAC) {
            boolean prev = computeState();
            regTimer.set(RegT.TAC, value);
            incrementIfFallingEdge(prev, computeState());
            
        } else if (address == AddressMap.REG_TMA) {
            regTimer.set(RegT.TMA, value);
        }
    }
    
    private void incrementIfFallingEdge(boolean previous, boolean current) {
        if (previous && !current) {
            int tima = regTimer.get(RegT.TIMA) + 1;
            if (tima > MAX_TIMA) {
                tima = regTimer.get(RegT.TMA);
                cpu.requestInterrupt(Interrupt.TIMER);
            }
            regTimer.set(RegT.TIMA, tima);
        }
    }
    
    private final boolean computeState() {
        return Bits.test(regTimer.get(RegT.TAC), 2) && Bits.test(mainCounter, getCriticalBit());
    }
    
    private int getCriticalBit() {
        int tac = Bits.extract(regTimer.get(RegT.TAC), 0, 2);
        switch (tac) {
        case 0b00:
            return 9;
        case 0b01:
            return 3;
        case 0b10:
            return 5;
        case 0b11:
            return 7;

        default:
            throw new Error();
        }
    }
}
