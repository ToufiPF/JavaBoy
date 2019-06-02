package ch.epfl.javaboy.component.cpu;

import java.util.Arrays;
import java.util.List;

import ch.epfl.javaboy.Bus;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;

public final class Cpu implements Component, Clocked {
    
    private static enum Reg implements Register {
        A, F, B, C, D, E, H, L;
        public static final List<Reg> ALL = Arrays.asList(values());
    }
    
    private static enum Reg16 implements Register {
        AF, BC, DE, HL;
        public static final List<Reg16> ALL = Arrays.asList(values());
    }
    
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);

    private static Opcode[] buildOpcodeTable(Opcode.Kind direct) {
        Opcode[] tab = new Opcode[Opcode.values().length];
        for (Opcode oc : Opcode.values())
            if (oc.kind == direct)
                tab[oc.encoding] = oc;
        return tab;
    }
    
    private long nextNonIdleCycle = 0;
    
    private final RegisterFile<Reg> reg8bits = new RegisterFile<>(Reg.values());
    private int PC = 0;
    private int SP = 0;
    
    private Bus bus = null;
    
    @Override
    public void cycle(long cycle) {
        if (cycle < nextNonIdleCycle)
            return;
        
        int encoding = 0;
        Opcode opcode = DIRECT_OPCODE_TABLE[encoding];
        
        dispatch(opcode.family);
        
        PC += opcode.totalBytes;
        nextNonIdleCycle += opcode.cycles;
    }

    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {
    }
    
    @Override
    public void attachTo(Bus bus) {
        bus = bus;
        Component.super.attachTo(bus);
    }
    
    public int[] _testGetPcSpAFBCDEHL() {
        int[] tab = new int[10];
        tab[0] = PC;
        tab[1] = SP;
        for (int i = 2 ; i < 10 ; ++i)
            tab[i] = reg8bits.get(Reg.values()[i]);
        return tab;
    }
    
    private void dispatch(Opcode.Family familyCode) {
        switch (familyCode) {
        case NOP: {
        } break;
        case LD_R8_HLR: {
        } break;
        case LD_A_HLRU: {
        } break;
        case LD_A_N8R: {
        } break;
        case LD_A_CR: {
        } break;
        case LD_A_N16R: {
        } break;
        case LD_A_BCR: {
        } break;
        case LD_A_DER: {
        } break;
        case LD_R8_N8: {
        } break;
        case LD_R16SP_N16: {
        } break;
        case POP_R16: {
        } break;
        case LD_HLR_R8: {
        } break;
        case LD_HLRU_A: {
        } break;
        case LD_N8R_A: {
        } break;
        case LD_CR_A: {
        } break;
        case LD_N16R_A: {
        } break;
        case LD_BCR_A: {
        } break;
        case LD_DER_A: {
        } break;
        case LD_HLR_N8: {
        } break;
        case LD_N16R_SP: {
        } break;
        case LD_R8_R8: {
        } break;
        case LD_SP_HL: {
        } break;
        case PUSH_R16: {
        } break;
        default:
            throw new Error("Family Opcode Non Supported");
        }
    }
    
    private int read8(int address) {
        return bus.read(address);
    }
    
    private int read8AtHL() {
        return read(reg16(Reg16.HL));
    }
    
    private int read8AfterOpcode() {
        return read(PC + 1);
    }
    
    private int read16(int address) {
        return (bus.read(address + 1) << Byte.SIZE) | bus.read(address);
    }
    
    private int read16AfterOpcode() {
        return read16(PC + 1);
    }
    
    private void write8(int address, int v8) {
        bus.write(address, v8);
    }
    
    private void write8AtHL(int v8) {
        write8(reg16(Reg16.HL), v8);
    }
    
    private void write16(int address, int v16) {
        bus.write(address, Bits.extract(v16, 0, Byte.SIZE));
        bus.write(address + 1, Bits.extract(v16, Byte.SIZE, Byte.SIZE));
    }
    
    private void push16(int v16) {
        SP -= 2;
        write16(SP, v16);
    }
    
    private int pop16() {
        int v16 = read16(SP);
        SP += 2;
        return v16;
    }
    
    private int reg16(Reg16 r) {
        Reg r0 = Reg.ALL.get(r.index() * 2);
        Reg r1 = Reg.ALL.get(r.index() * 2 + 1);
        return (reg8bits.get(r0) << 8) | reg8bits.get(r1);
    }
    
    private void setReg16(Reg16 r, int newValue) {
        Reg r0 = Reg.ALL.get(r.index() * 2);
        Reg r1 = Reg.ALL.get(r.index() * 2 + 1);
        if (r == Reg16.AF) {
            reg8bits.set(r0, Bits.extract(newValue, 0, Byte.SIZE));
            reg8bits.set(r1, Bits.extract(newValue, Byte.SIZE, Byte.SIZE));
        } else {
            reg8bits.set(r0, Bits.extract(newValue, 0, Byte.SIZE));
            reg8bits.set(r1, Bits.extract(newValue, Byte.SIZE, Byte.SIZE));
        }
    }
    
    private void setReg16SP(Reg16 r, int newValue) {
        if (r == Reg16.AF)
            SP = newValue;
        else
            setReg16(r, newValue);
    }
}
