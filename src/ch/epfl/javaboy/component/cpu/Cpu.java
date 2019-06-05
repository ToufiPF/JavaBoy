package ch.epfl.javaboy.component.cpu;

import java.util.Arrays;
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
import ch.epfl.javaboy.component.cpu.Alu.Flag;
import ch.epfl.javaboy.component.cpu.Alu.RotDir;
import ch.epfl.javaboy.component.memory.Ram;

/**
 * Represents a Cpu for a GameBoy.
 * Implements Component and Clocked
 * @author Toufi
 */
public final class Cpu implements Component, Clocked {
    
    /**
     * Possible interruptions
     * @author Toufi
     */
    public static enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }

    private static enum Reg implements Register {
        A, F, B, C, D, E, H, L;
        public static final List<Reg> ALL = Arrays.asList(values());
    }
    private static enum Reg16 implements Register {
        AF, BC, DE, HL;
    }
    private enum RegI implements Register {
        IE, IF
    }
    private static enum FlagSrc implements Bit {
        FALSE, TRUE, ALU, CPU;
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
    private static final int OPCODE_PREFIX = 0xCB;
    private static final int INTERRUPTION_MANAGEMENT_DURATION = 5;

    private long nextNonIdleCycle;
    private int PC;
    private int SP;
    private boolean IME;
    private boolean isHalted;

    private final RegisterFile<Reg> reg8bits;
    private final RegisterFile<RegI> interruptionReg;
    private Bus bus;
    private Ram highRam;
    
    /**
     * Constructs a new Cpu
     */
    public Cpu() {
        nextNonIdleCycle = 0;
        PC = 0;
        SP = 0;
        IME = false;
        isHalted = false;

        reg8bits = new RegisterFile<Cpu.Reg>(Reg.values());
        interruptionReg = new RegisterFile<Cpu.RegI>(RegI.values());
        bus = null;
        highRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    }

    @Override
    public void cycle(long cycle) {
        int interrupt = getLowestInterrupt();
        
        if (isHalted && interrupt != -1) {
            nextNonIdleCycle = cycle;
            isHalted = false;
        }

        if (cycle < nextNonIdleCycle || isHalted) {
            return;
        } else if (IME && interrupt != -1) {
            manageInterruption(Interrupt.values()[interrupt]);
        } else {
            int encoding = read8(PC);
            Opcode opcode = encoding == OPCODE_PREFIX ? PREFIXED_OPCODE_TABLE[read8AfterOpcode()] : DIRECT_OPCODE_TABLE[encoding];

            dispatch(opcode);
        }
    }
    
    private void manageInterruption(Interrupt toManage) {
            IME = false;
            interruptionReg.setBit(RegI.IF, toManage, false);
            
            push16(PC);
            PC = AddressMap.INTERRUPTS[toManage.index()];

            nextNonIdleCycle += INTERRUPTION_MANAGEMENT_DURATION;
    }
    
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);

        if (address == AddressMap.REG_IE)
            return interruptionReg.get(RegI.IE);
        else if (address == AddressMap.REG_IF)
            return interruptionReg.get(RegI.IF);
        else if (address >= AddressMap.HIGH_RAM_START
                && address < AddressMap.HIGH_RAM_END)
            return highRam.read(address - AddressMap.HIGH_RAM_START);
        else
            return NO_DATA;
    }
    @Override
    public void write(int address, int value) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);

        if (address == AddressMap.REG_IE)
            interruptionReg.set(RegI.IE, value);
        else if (address == AddressMap.REG_IF)
            interruptionReg.set(RegI.IF, value);
        else if (AddressMap.HIGH_RAM_START <= address
                && address < AddressMap.HIGH_RAM_END)
            highRam.write(address - AddressMap.HIGH_RAM_START, value);
    }

    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        Component.super.attachTo(bus);
    }
    
    /**
     * Request an interruption
     * (set the corresponding bit in IF to true)
     * @param i (Interrupt) interruption to raise
     */
    public void requestInterrupt(Interrupt i) {
        interruptionReg.setBit(RegI.IF, i, true);
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
        int nextPC = Bits.clip(2 * Byte.SIZE, PC + opcode.totalBytes);
        
        switch (opcode.family) {
        // No operations :
        case NOP:
            break;
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
            boolean carry = extractCarryFromOpcodeAndFlags(opcode);
            setRegAndFlags(Reg.A, Alu.add(left, right, carry));
        } break;
        case ADD_A_R8: {
            int left = reg8bits.get(Reg.A);
            int right = reg8bits.get(extractReg(opcode, 0));
            boolean carry = extractCarryFromOpcodeAndFlags(opcode);
            setRegAndFlags(Reg.A, Alu.add(left, right, carry));
        } break;
        case ADD_A_HLR: {
            int left = reg8bits.get(Reg.A);
            int right = read8(getReg16(Reg16.HL));
            boolean carry = extractCarryFromOpcodeAndFlags(opcode);
            setRegAndFlags(Reg.A, Alu.add(left, right, carry));
        } break;
        case INC_R8: {
            Reg r = extractReg(opcode, 3);
            int res = Alu.add(reg8bits.get(r), 1);
            setRegFromAlu(r, res);
            combineAluFlags(res, FlagSrc.ALU, FlagSrc.FALSE, FlagSrc.ALU, FlagSrc.CPU);
        } break;
        case INC_HLR: {
            int res = Alu.add(read8AtHL(), 1);
            write8AtHL(Bits.extract(res, Byte.SIZE, Byte.SIZE));
            combineAluFlags(res,  FlagSrc.ALU, FlagSrc.FALSE, FlagSrc.ALU, FlagSrc.CPU);
        } break;
        case ADD_HL_R16SP: {
            Reg16 r = extractReg16(opcode);
            int res = Alu.add16H(getReg16(Reg16.HL), getReg16SP(r));
            setReg16(Reg16.HL, Bits.extract(res, Byte.SIZE, Byte.SIZE * 2));
            combineAluFlags(res, FlagSrc.CPU, FlagSrc.FALSE, FlagSrc.ALU, FlagSrc.ALU);
        } break;
        case INC_R16SP: {
            Reg16 r = extractReg16(opcode);
            setReg16SP(r, Bits.clip(Byte.SIZE * 2, getReg16SP(r) + 1));
        } break;
        case LD_HLSP_S8: {
            int res = Alu.add16L(SP, Bits.clip(Byte.SIZE * 2, Bits.signExtend8(read8AfterOpcode())));
            boolean stockInHL = Bits.test(opcode.encoding, 4);
            if (stockInHL)
                setReg16(Reg16.HL, Bits.extract(res, Byte.SIZE, Byte.SIZE * 2));
            else
                setReg16SP(Reg16.AF, Bits.extract(res, Byte.SIZE, Byte.SIZE * 2));

            combineAluFlags(res, FlagSrc.FALSE, FlagSrc.FALSE, FlagSrc.ALU, FlagSrc.ALU);
        } break;    
        // Subtract/Compare Instructions :
        case SUB_A_N8: {
            int left = reg8bits.get(Reg.A);
            int right = read8AfterOpcode();
            boolean carry = extractCarryFromOpcodeAndFlags(opcode);
            setRegAndFlags(Reg.A, Alu.sub(left, right, carry));
        } break;
        case SUB_A_R8: {
            int left = reg8bits.get(Reg.A);
            int right = reg8bits.get(extractReg(opcode, 0));
            boolean carry = extractCarryFromOpcodeAndFlags(opcode);
            setRegAndFlags(Reg.A, Alu.sub(left, right, carry));
        } break;
        case SUB_A_HLR: {
            int left = reg8bits.get(Reg.A);
            int right = read8(getReg16(Reg16.HL));
            boolean carry = extractCarryFromOpcodeAndFlags(opcode);
            setRegAndFlags(Reg.A, Alu.sub(left, right, carry));
        } break;
        case DEC_R8: {
            Reg r = extractReg(opcode, 3);
            int res = Alu.sub(reg8bits.get(r), 1);
            setRegFromAlu(r, res);
            combineAluFlags(res, FlagSrc.ALU, FlagSrc.TRUE, FlagSrc.ALU, FlagSrc.CPU);
        } break;
        case DEC_HLR: {
            int res = Alu.sub(read8AtHL(), 1);
            write8AtHL(Bits.extract(res, Byte.SIZE, Byte.SIZE));
            combineAluFlags(res, FlagSrc.ALU, FlagSrc.TRUE, FlagSrc.ALU, FlagSrc.CPU);
        } break;
        case CP_A_N8: {
            int right = read8AfterOpcode();
            setFlagsFromAlu(Alu.sub(reg8bits.get(Reg.A), right));
        } break;
        case CP_A_R8: {
            int right = reg8bits.get(extractReg(opcode, 0));
            setFlagsFromAlu(Alu.sub(reg8bits.get(Reg.A), right));
        } break;
        case CP_A_HLR: {
            int right = read8(getReg16(Reg16.HL));
            setFlagsFromAlu(Alu.sub(reg8bits.get(Reg.A), right));
        } break;
        case DEC_R16SP: {
            Reg16 r = extractReg16(opcode);
            setReg16SP(r, Bits.clip(Byte.SIZE * 2, getReg16SP(r) - 1));
        } break;
        // And, or, xor, complement Instructions :
        case AND_A_N8: {
            int res = Alu.and(reg8bits.get(Reg.A), read8AfterOpcode());
            setRegAndFlags(Reg.A, res);
        } break;
        case AND_A_R8: {
            int res = Alu.and(reg8bits.get(Reg.A), reg8bits.get(extractReg(opcode, 0)));
            setRegAndFlags(Reg.A, res);
        } break;
        case AND_A_HLR: {
            int res = Alu.and(reg8bits.get(Reg.A), read8AtHL());
            setRegAndFlags(Reg.A, res);
        } break;
        case OR_A_N8: {
            int res = Alu.or(reg8bits.get(Reg.A), read8AfterOpcode());
            setRegAndFlags(Reg.A, res);
        } break;
        case OR_A_R8: {
            int res = Alu.or(reg8bits.get(Reg.A), reg8bits.get(extractReg(opcode, 0)));
            setRegAndFlags(Reg.A, res);
        } break;
        case OR_A_HLR: {
            int res = Alu.or(reg8bits.get(Reg.A), read8AtHL());
            setRegAndFlags(Reg.A, res);
        } break;
        case XOR_A_N8: {
            int res = Alu.xor(reg8bits.get(Reg.A), read8AfterOpcode());
            setRegAndFlags(Reg.A, res);
        } break;
        case XOR_A_R8: {
            int res = Alu.xor(reg8bits.get(Reg.A), reg8bits.get(extractReg(opcode, 0)));
            setRegAndFlags(Reg.A, res);
        } break;
        case XOR_A_HLR: {
            int res = Alu.xor(reg8bits.get(Reg.A), read8AtHL());
            setRegAndFlags(Reg.A, res);
        } break;
        case CPL: {
            reg8bits.set(Reg.A, Bits.complement8(reg8bits.get(Reg.A)));
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.TRUE, FlagSrc.TRUE, FlagSrc.CPU);
        } break;
        // Rotate, shift Instructions :
        case SLA_R8: {
            Reg r = extractReg(opcode, 0);
            int res = Alu.shiftLeft(reg8bits.get(r));
            setRegAndFlags(r, res);
        } break;
        case SLA_HLR: {
            int res = Alu.shiftLeft(read8AtHL());
            write8AtHLAndSetFlags(res);
        } break;
        case SRA_R8: {
            Reg r = extractReg(opcode, 0);
            int res = Alu.shiftRightA(reg8bits.get(r));
            setRegAndFlags(r, res);
        } break;
        case SRA_HLR: {
            int res = Alu.shiftRightA(read8AtHL());
            write8AtHLAndSetFlags(res);
        } break;
        case SRL_R8: {
            Reg r = extractReg(opcode, 0);
            int res = Alu.shiftRightL(reg8bits.get(r));
            setRegAndFlags(r, res);
        } break;
        case SRL_HLR: {
            int res = Alu.shiftRightL(read8AtHL());
            write8AtHLAndSetFlags(res);
        } break;
        case ROTCA: {
            int res = Alu.rotate(extractRotDirFrom(opcode), reg8bits.get(Reg.A));
            setRegFromAlu(Reg.A, res);
            combineAluFlags(res, FlagSrc.FALSE, FlagSrc.FALSE, FlagSrc.FALSE, FlagSrc.ALU);
        } break;
        case ROTA: {
            int res = Alu.rotate(extractRotDirFrom(opcode), reg8bits.get(Reg.A), getFlagValue(Flag.C));
            setRegFromAlu(Reg.A, res);
            combineAluFlags(res, FlagSrc.FALSE, FlagSrc.FALSE, FlagSrc.FALSE, FlagSrc.ALU);
        } break;
        case ROTC_R8: {
            Reg r = extractReg(opcode, 0);
            int res = Alu.rotate(extractRotDirFrom(opcode), reg8bits.get(r));
            setRegAndFlags(r, res);
        } break;
        case ROT_R8: {
            Reg r = extractReg(opcode, 0);
            int res = Alu.rotate(extractRotDirFrom(opcode), reg8bits.get(r), getFlagValue(Flag.C));
            setRegAndFlags(r, res);
        } break;
        case ROTC_HLR: {
            int res = Alu.rotate(extractRotDirFrom(opcode), read8AtHL());
            write8AtHLAndSetFlags(res);
        } break;
        case ROT_HLR: {
            int res = Alu.rotate(extractRotDirFrom(opcode), read8AtHL(), getFlagValue(Flag.C));
            write8AtHLAndSetFlags(res);
        } break;
        case SWAP_R8: {
            Reg r = extractReg(opcode, 0);
            int res = Alu.swap(reg8bits.get(r));
            setRegAndFlags(r, res);
        } break;
        case SWAP_HLR: {
            int res = Alu.swap(read8AtHL());
            write8AtHLAndSetFlags(res);
        } break;
        // Bit test and set Instructions :
        case BIT_U3_R8: {
            int res = Alu.testBit(reg8bits.get(extractReg(opcode, 0)), Bits.extract(opcode.encoding, 3, REG8_CODE_SIZE));
            combineAluFlags(res, FlagSrc.ALU, FlagSrc.FALSE, FlagSrc.TRUE, FlagSrc.CPU);
        } break;
        case BIT_U3_HLR: {
            int res = Alu.testBit(read8AtHL(), Bits.extract(opcode.encoding, 3, REG8_CODE_SIZE));
            combineAluFlags(res, FlagSrc.ALU, FlagSrc.FALSE, FlagSrc.TRUE, FlagSrc.CPU);
        } break;
        case CHG_U3_R8: {
            Reg r = extractReg(opcode, 0);
            int bits = Bits.set(reg8bits.get(r), Bits.extract(opcode.encoding, 3, REG8_CODE_SIZE), isBitSetOperation(opcode));
            reg8bits.set(r, bits);
        } break;
        case CHG_U3_HLR: {
            int bits = Bits.set(read8AtHL(), Bits.extract(opcode.encoding, 3, REG8_CODE_SIZE), isBitSetOperation(opcode));
            write8AtHL(bits);
        } break;
        // Misc. ALU Instructions :
        case DAA: {
            int res = Alu.bcdAdjust(reg8bits.get(Reg.A), getFlagValue(Flag.N), getFlagValue(Flag.H), getFlagValue(Flag.C));
            setRegAndFlags(Reg.A, res);
        } break;
        case SCCF: {
            boolean newFlagC = !extractCarryFromOpcodeAndFlags(opcode);
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.FALSE, FlagSrc.FALSE, newFlagC ? FlagSrc.TRUE : FlagSrc.FALSE);
        } break;
        // Jumps
        case JP_N16: {
            nextPC = read16AfterOpcode();
        } break;
        case JP_CC_N16: {
            if (readAndTestCondition(opcode)) {
                nextPC = read16AfterOpcode();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        } break;
        case JP_HL: {
            nextPC = getReg16(Reg16.HL);
        } break;
        case JR_E8: {
            int e8 = Bits.signExtend8(read8AfterOpcode());
            nextPC =  Bits.clip(Byte.SIZE * 2, nextPC + e8);
        } break;
        case JR_CC_E8: {
            if (readAndTestCondition(opcode)) {
                int e8 = Bits.signExtend8(read8AfterOpcode());
                nextPC =  Bits.clip(Byte.SIZE * 2, nextPC + e8);
                nextNonIdleCycle += opcode.additionalCycles;
            }
        } break;
        // Calls and returns
        case CALL_N16: {
            push16(nextPC);
            nextPC = read16AfterOpcode();
        } break;
        case CALL_CC_N16: {
            if (readAndTestCondition(opcode)) {
                push16(nextPC);
                nextPC = read16AfterOpcode();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        } break;
        case RST_U3: {
            push16(nextPC);
            nextPC = AddressMap.RESETS[Bits.extract(opcode.encoding, 3, REG8_CODE_SIZE)];
        } break;
        case RET: {
            nextPC = pop16();
        } break;
        case RET_CC: {
            if (readAndTestCondition(opcode)) {
                nextPC = pop16();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        } break;
        // Interrupts
        case EDI: {
            IME = Bits.test(opcode.encoding, 3);
        } break;
        case RETI: {
            IME = true;
            nextPC = pop16();
        } break;
        // Misc control
        case HALT: {
            isHalted = true;
        } break;
        case STOP:
            throw new Error("STOP is not implemented");
        default:
            throw new Error("Family opcode non supported : " + opcode.family
                    + ".\nOpcode : 0x" + Integer.toHexString(opcode.encoding));
        }
        PC = nextPC;
        nextNonIdleCycle += opcode.cycles;
    }

    private int read8(int address) {
        return bus.read(address);
    }
    private int read8AtHL() {
        return read8(getReg16(Reg16.HL));
    }
    private int read8AfterOpcode() {
        return read8(Bits.clip(2 * Byte.SIZE, PC + 1));
    }

    private void write8(int address, int v8) {
        bus.write(address, v8);
    }
    private void write8AtHL(int v8) {
        write8(getReg16(Reg16.HL), v8);
    }

    private int read16(int address) {
        return Bits.make16(bus.read(Bits.clip(2 * Byte.SIZE, address + 1)), bus.read(address));
    }
    private int read16AfterOpcode() {
        return read16(Bits.clip(2 * Byte.SIZE, PC + 1));
    }

    private void write16(int address, int v16) {
        bus.write(address, Bits.extract(v16, 0, Byte.SIZE));
        bus.write(Bits.clip(2 * Byte.SIZE, address + 1), Bits.extract(v16, Byte.SIZE, Byte.SIZE));
    }

    private void push16(int v16) {
        SP = Bits.clip(2 * Byte.SIZE, SP - 2);
        write16(SP, v16);
    }
    private int pop16() {
        int v16 = read16(SP);
        SP = Bits.clip(2 * Byte.SIZE, SP + 2);
        return v16;
    }

    private int getReg16(Reg16 r) {
        Reg rMSB = Reg.ALL.get(r.index() * 2);
        Reg rLSB = Reg.ALL.get(r.index() * 2 + 1);
        return Bits.make16(reg8bits.get(rMSB), reg8bits.get(rLSB));
    }
    private int getReg16SP(Reg16 r) {
        if (r == Reg16.AF)
            return SP;
        return getReg16(r);
    }

    private void setReg16(Reg16 r, int newValue) {
        Reg rMSB = Reg.ALL.get(r.index() * 2);
        Reg rLSB = Reg.ALL.get(r.index() * 2 + 1);
        if (r == Reg16.AF) {
            reg8bits.set(rLSB, newValue & 0xF0);
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

        int flags = computeFlagMask(z, Flag.Z, vf, regF) | computeFlagMask(n, Flag.N, vf, regF)
                | computeFlagMask(h, Flag.H, vf, regF) | computeFlagMask(c, Flag.C, vf, regF);
        reg8bits.set(Reg.F, flags);
    }
    private int computeFlagMask(FlagSrc src, Flag flag, int aluVF, int cpuF) {
        switch (src) {
        case TRUE:
            return flag.mask();
        case ALU:
            return aluVF & flag.mask();
        case CPU:
            return cpuF & flag.mask();
        default:
            return 0;
        }
    }

    private int extractHLIncrement(Opcode opcode) {
        return (opcode.encoding & Bits.mask(INDEX_INCREM_HL)) != 0 ? -1 : 1;
    }
    private boolean extractCarryFromOpcodeAndFlags(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) && Bits.test(reg8bits.get(Reg.F), Flag.C);
    }
    private RotDir extractRotDirFrom(Opcode opcode) {
        return Bits.test(opcode.encoding, 3) ? RotDir.RIGHT : RotDir.LEFT;
    }
    private boolean getFlagValue(Flag flag) {
        return reg8bits.testBit(Reg.F, flag);
    }
    private boolean isBitSetOperation(Opcode opcode) {
        return Bits.test(opcode.encoding, 6);
    }

    private boolean readAndTestCondition(Opcode opcode) {
        int condition = Bits.extract(opcode.encoding, 3, 2);
        switch (condition) {
        case 0b00: // Non Z
            return !getFlagValue(Flag.Z);
        case 0b01: // Z
            return getFlagValue(Flag.Z);
        case 0b10: // Non C
            return !getFlagValue(Flag.C);
        default: // C
            return getFlagValue(Flag.C);
        }
    }

    private int getLowestInterrupt() {
        int all = interruptionReg.get(RegI.IE) & interruptionReg.get(RegI.IF);
        return all == 0 ? -1 : Integer.numberOfTrailingZeros(all);
    }
} 
