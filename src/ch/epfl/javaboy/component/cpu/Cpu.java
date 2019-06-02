package ch.epfl.javaboy.component.cpu;

import java.util.Arrays;
import java.util.List;

import ch.epfl.javaboy.AddressMap;
import ch.epfl.javaboy.Bus;
import ch.epfl.javaboy.Register;
import ch.epfl.javaboy.RegisterFile;
import ch.epfl.javaboy.bits.Bit;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.cpu.Alu.Flag;

public final class Cpu implements Component, Clocked {

    private static enum Reg implements Register {
        A, F, B, C, D, E, H, L;
        public static final List<Reg> ALL = Arrays.asList(values());
    }

    private static enum Reg16 implements Register {
        AF, BC, DE, HL;
    }

    private static enum FlagSrc implements Bit {
        F0, F1, ALU, CPU;
    }

    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);

    private static final Opcode[] PREFIXED_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.PREFIXED);

    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        Opcode[] tab = new Opcode[Opcode.values().length];
        for (Opcode oc : Opcode.values())
            if (oc.kind == kind)
                tab[oc.encoding] = oc;
        return tab;
    }

    private static final int REG8_CODE_SIZE = 3;
    private static final int REG16_CODE_START = 4;
    private static final int REG16_CODE_SIZE = 2;
    private static final int INDEX_INCREM_HL = 4;

    private long nextNonIdleCycle = 0;

    private final RegisterFile<Reg> reg8bits = new RegisterFile<>(Reg.values());
    private int PC = 0;
    private int SP = 0;

    private Bus bus = null;

    @Override
    public void cycle(long cycle) {
        if (cycle < nextNonIdleCycle)
            return;

        int encoding = read8(PC);
        Opcode opcode = encoding == 0xCB ?
                PREFIXED_OPCODE_TABLE[read8AfterOpcode()] : DIRECT_OPCODE_TABLE[encoding];

                dispatch(opcode);

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
        this.bus = bus;
        Component.super.attachTo(bus);
    }

    public int[] _testGetPcSpAFBCDEHL() {
        int[] tab = new int[10];
        tab[0] = PC;
        tab[1] = SP;
        for (int i = 2 ; i < 10 ; ++i)
            tab[i] = reg8bits.get(Reg.ALL.get(i - 2));
        return tab;
    }

    private void dispatch(Opcode opcode) {
        switch (opcode.family) {
        // No operations :
        case NOP:
            return;
            // Load instructions : 
        case LD_R8_HLR: {
            Reg r = extractReg(opcode, 3);
            reg8bits.set(r, read8AtHL());
        } break;
        case LD_A_HLRU: {
            reg8bits.set(Reg.A, read8AtHL());
            int inc = extractHLIncrement(opcode);
            setReg16(Reg16.HL, getReg16(Reg16.HL) + inc);
        } break;
        case LD_A_N8R: {
            int n8 = read8AfterOpcode();
            reg8bits.set(Reg.A, read8(AddressMap.REGS_START + n8));
        } break;
        case LD_A_CR: {
            reg8bits.set(Reg.A, read8(AddressMap.REGS_START + reg8bits.get(Reg.C)));
        } break;
        case LD_A_N16R: {
            int n16 = read16AfterOpcode();
            reg8bits.set(Reg.A, read8(n16));
        } break;
        case LD_A_BCR: {
            reg8bits.set(Reg.A, read8(getReg16(Reg16.BC)));
        } break;
        case LD_A_DER: {
            reg8bits.set(Reg.A, read8(getReg16(Reg16.DE)));
        } break;
        case LD_R8_N8: {
            int n8 = read8AfterOpcode();
            Reg r = extractReg(opcode, 3);
            reg8bits.set(r, n8);
        } break;
        case LD_R16SP_N16: {
            int n16 = read16AfterOpcode();
            Reg16 r = extractReg16(opcode);
            setReg16SP(r, n16);
        } break;
        case POP_R16: {
            Reg16 r = extractReg16(opcode);
            setReg16(r, pop16());
        } break;
        // Store instructions :
        case LD_HLR_R8: {
            Reg r = extractReg(opcode, 0);
            write8AtHL(reg8bits.get(r));
        } break;
        case LD_HLRU_A: {
            write8AtHL(reg8bits.get(Reg.A));
            int inc = extractHLIncrement(opcode);
            setReg16(Reg16.HL, getReg16(Reg16.HL) + inc);
        } break;
        case LD_N8R_A: {
            int n8 = read8AfterOpcode();
            write8(AddressMap.REGS_START + n8, reg8bits.get(Reg.A));
        } break;
        case LD_CR_A: {
            write8(AddressMap.REGS_START + reg8bits.get(Reg.C), reg8bits.get(Reg.A));
        } break;
        case LD_N16R_A: {
            int n16 = read16AfterOpcode();
            write8(n16, reg8bits.get(Reg.A));
        } break;
        case LD_BCR_A: {
            write16(getReg16(Reg16.BC), reg8bits.get(Reg.A));
        } break;
        case LD_DER_A: {
            write16(getReg16(Reg16.DE), reg8bits.get(Reg.A));
        } break;
        case LD_HLR_N8: {
            int n8 = read8AfterOpcode();
            write16(getReg16(Reg16.HL), n8);
        } break;
        case LD_N16R_SP: {
            int n16 = read16AfterOpcode();
            write16(n16, SP);
        } break;
        case PUSH_R16: {
            Reg16 r = extractReg16(opcode);
            push16(getReg16(r));
        } break;
        // Move Instructions :
        case LD_R8_R8: { 
            Reg r1 = extractReg(opcode, 3);
            Reg r2 = extractReg(opcode, 0);
            reg8bits.set(r1, reg8bits.get(r2));
        } break;
        case LD_SP_HL: {
            SP = getReg16(Reg16.HL);
        } break;

        // Add Instructions :
        case ADD_A_N8: {
            int left = reg8bits.get(Reg.A);
            int right = read8AfterOpcode();
            boolean carry = Bits.test(opcode.encoding, 3) && Bits.test(reg8bits.get(Reg.F), Flag.C);
            setRegAndFlags(Reg.A, Alu.add(left, right, carry));
        } break;
        case ADD_A_R8: {
            int left = reg8bits.get(Reg.A);
            int right = reg8bits.get(extractReg(opcode, 0));
            boolean carry = Bits.test(opcode.encoding, 3) && Bits.test(reg8bits.get(Reg.F), Flag.C);
            setRegAndFlags(Reg.A, Alu.add(left, right, carry));
        } break;
        case ADD_A_HLR: {
            int left = reg8bits.get(Reg.A);
            int right = read8(getReg16(Reg16.HL));
            boolean carry = Bits.test(opcode.encoding, 3) && Bits.test(reg8bits.get(Reg.F), Flag.C);
            setRegAndFlags(Reg.A, Alu.add(left, right, carry));
        } break;
        case INC_R8: {
        } break;
        case INC_HLR: {
        } break;
        case INC_R16SP: {
        } break;
        case ADD_HL_R16SP: {
        } break;
        case LD_HLSP_S8: {
        } break;
        // Subtract/Compare Instructions :
        case SUB_A_R8: {
        } break;
        case SUB_A_N8: {
        } break;
        case SUB_A_HLR: {
        } break;
        case DEC_R8: {
        } break;
        case DEC_HLR: {
        } break;
        case CP_A_R8: {
        } break;
        case CP_A_N8: {
        } break;
        case CP_A_HLR: {
        } break;
        case DEC_R16SP: {
        } break;
        // And, or, xor, complement Instructions :
        case AND_A_N8: {
        } break;
        case AND_A_R8: {
        } break;
        case AND_A_HLR: {
        } break;
        case OR_A_R8: {
        } break;
        case OR_A_N8: {
        } break;
        case OR_A_HLR: {
        } break;
        case XOR_A_R8: {
        } break;
        case XOR_A_N8: {
        } break;
        case XOR_A_HLR: {
        } break;
        case CPL: {
        } break;
        // Rotate, shift Instructions :
        case ROTCA: {
        } break;
        case ROTA: {
        } break;
        case ROTC_R8: {
        } break;
        case ROT_R8: {
        } break;
        case ROTC_HLR: {
        } break;
        case ROT_HLR: {
        } break;
        case SWAP_R8: {
        } break;
        case SWAP_HLR: {
        } break;
        case SLA_R8: {
        } break;
        case SRA_R8: {
        } break;
        case SRL_R8: {
        } break;
        case SLA_HLR: {
        } break;
        case SRA_HLR: {
        } break;
        case SRL_HLR: {
        } break;
        // Bit test and set Instructions :
        case BIT_U3_R8: {
        } break;
        case BIT_U3_HLR: {
        } break;
        case CHG_U3_R8: {
        } break;
        case CHG_U3_HLR: {
        } break;
        // Misc. ALU Instructions :
        case DAA: {
        } break;
        case SCCF: {
        } break;
        default:
            throw new Error("Family Opcode Non Supported");
        }
    }

    private int read8(int address) {
        return bus.read(address);
    }

    private int read8AtHL() {
        return read8(getReg16(Reg16.HL));
    }

    private int read8AfterOpcode() {
        return read8(PC + 1);
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
        write8(getReg16(Reg16.HL), v8);
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

    private int getReg16(Reg16 r) {
        Reg rMSB = Reg.ALL.get(r.index() * 2);
        Reg rLSB = Reg.ALL.get(r.index() * 2 + 1);
        return (reg8bits.get(rMSB) << 8) | reg8bits.get(rLSB);
    }

    private void setReg16(Reg16 r, int newValue) {
        Reg rMSB = Reg.ALL.get(r.index() * 2);
        Reg rLSB = Reg.ALL.get(r.index() * 2 + 1);
        if (r == Reg16.AF) {
            reg8bits.set(rLSB, Bits.extract(newValue, 4, 4) << 4);
            reg8bits.set(rMSB, Bits.extract(newValue, Byte.SIZE, Byte.SIZE));
        } else {
            reg8bits.set(rLSB, Bits.extract(newValue, 0, Byte.SIZE));
            reg8bits.set(rMSB, Bits.extract(newValue, Byte.SIZE, Byte.SIZE));
        }
    }

    private void setReg16SP(Reg16 r, int newValue) {
        if (r == Reg16.AF)
            SP = newValue;
        else
            setReg16(r, newValue);
    }

    private Reg extractReg(Opcode opcode, int startBit) {
        int regCode = Bits.extract(opcode.encoding, startBit, REG8_CODE_SIZE);
        switch (regCode) {
        case 0b000:
            return Reg.B;
        case 0b001:
            return Reg.C;
        case 0b010:
            return Reg.D;
        case 0b011:
            return Reg.E;
        case 0b100:
            return Reg.H;
        case 0b101:
            return Reg.L;
        case 0b111:
            return Reg.A;
        default:
            throw new Error("Register (8 bits) encoding not valid.");
        }
    }
    private Reg16 extractReg16(Opcode opcode) {
        int regCode = Bits.extract(opcode.encoding, REG16_CODE_START, REG16_CODE_SIZE);
        switch (regCode) {
        case 0b00:
            return Reg16.BC;
        case 0b01:
            return Reg16.DE;
        case 0b10:
            return Reg16.HL;
        case 0b11:
            return Reg16.AF;
        default:
            throw new Error("Register (16 bits) encoding not valid.");
        }
    }
    private int extractHLIncrement(Opcode opcode) {
        return (opcode.encoding & Bits.mask(INDEX_INCREM_HL)) != 0 ? -1 : 1;
    }

    private void setRegFromAlu(Reg reg, int valueFlags) {
        reg8bits.set(reg, Bits.extract(valueFlags, Byte.SIZE, Byte.SIZE));
    }
    private void setFlagsFromAlu(int valueFlags) {
        reg8bits.set(Reg.F, Bits.extract(valueFlags, 0, Byte.SIZE));
    }
    private void setRegAndFlags(Reg reg, int valueFlags) {
        setRegFromAlu(reg, valueFlags);
        setFlagsFromAlu(valueFlags);
    }
    private void write8AtHLAndSetFlags(int valueFlags) {
        write8AtHL(Bits.extract(valueFlags, Byte.SIZE, Byte.SIZE));
        setFlagsFromAlu(valueFlags);
    }

    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h, FlagSrc c) {
        int regF = reg8bits.get(Reg.F);

        int flags = getFlagMask(z, Flag.Z, vf, regF) | getFlagMask(n, Flag.N, vf, regF)
                | getFlagMask(h, Flag.H, vf, regF) | getFlagMask(c, Flag.C, vf, regF);
        reg8bits.set(Reg.F, flags);
    }
    private int getFlagMask(FlagSrc src, Flag flag, int aluVF, int cpuF) {
        switch (src) {
        case F1:
            return flag.mask();
        case ALU:
            return aluVF & flag.mask();
        case CPU:
            return cpuF & flag.mask();
        default:
            return 0;
        }
    }
}
