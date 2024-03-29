package ch.epfl.javaboy.component;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.cpu.Cpu;
import ch.epfl.javaboy.component.cpu.Cpu.Interrupt;

import java.util.List;
import java.util.Objects;

/**
 * Represents the timer of a GameBoy
 * @author Toufi
 */
public final class Timer implements Component, Clocked {
    
    private enum RegT implements Register {
        TIMA, TMA, TAC;
        public static final List<RegT> ALL = List.of(values());
    }
    
    private static final int MAX_MAIN_COUNTER = 0xFFFF;
    private static final int MAX_TIMA = 0xFF;
    private static final int UNITS_BY_CYCLE = 4;

    private static final int STATE_LENGTH = Integer.BYTES + RegT.ALL.size();
    
    private final Cpu cpu;
    private final RegisterFile<RegT> regTimer;
    private int mainCounter;
    
    /**
     * Constructs a new Timer
     * @param cpu (Cpu) the cpu of the GameBoy,
     * used to raise interruptions
     */
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
        Preconditions.checkBits16(address);
        
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
            mainCounter = 0;
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

    @Override
    public byte[] saveState() {
        byte[] state = new byte[STATE_LENGTH];

        for (int i = 0 ; i < Integer.BYTES ; ++i)
            state[i] = (byte) Bits.extract(mainCounter, i * Byte.SIZE, Byte.SIZE);

        for (int i = 0 ; i < RegT.ALL.size(); ++i)
            state[Integer.BYTES + i] = (byte) regTimer.get(RegT.ALL.get(i));

        return state;
    }

    @Override
    public void loadState(byte[] state) {
        if (state.length != STATE_LENGTH)
            throw new IllegalStateException("Invalid state.");

        mainCounter = 0;
        for (int i = 0 ; i < Integer.BYTES ; ++i)
            mainCounter |= Byte.toUnsignedInt(state[i]) << (i * Byte.SIZE);

        for (int i = 0 ; i < RegT.ALL.size() ; ++i)
            regTimer.set(RegT.ALL.get(i), Byte.toUnsignedInt(state[Integer.BYTES + i]));
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
    
    private boolean computeState() {
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
