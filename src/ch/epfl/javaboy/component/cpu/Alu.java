package ch.epfl.javaboy.component.cpu;

import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.bits.Bit;
import ch.epfl.javaboy.bits.Bits;

/**
 * Alu
 * Contains static methods to emulate an
 * Arithmetic Logic Unit, the part of the CPU
 * in charge of additions and substractions
 *
 * @author Toufi
 */
final class Alu {

    /**
     * Flag
     * Represents the flags that an ALU can
     * send :
     * - first 4 are unused,
     * - C = true if there is a end_carry
     * - H = true if there is a half_carry
     * - N = true if operation == substraction
     * - Z = true if result == 0
     *
     * @author Toufi
     */
    public enum Flag implements Bit {
        UNUSED_0, UNUSED_1, UNUSED_2, UNUSED_3,
        C, H, N, Z
    }
    /**
     * RotDir
     * Specifies the direction of the rotation
     * for Alu methods
     *
     * @author Toufi
     */
    public enum RotDir {
        LEFT, RIGHT
    }

    private static final int FLAGS_START = 0;
    private static final int VALUE_START = 8;
    private static final int VALUE_SIZE = 16;

    /**
     * Applies bitwise and operation to the given vectors
     * Flags : Z010
     *
     * @param l (int) 8 bits vector
     * @param r (int) 8 bits vector
     * @return (int) result and flags
     */
    static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int res = l & r;
        return packValueZNHC(res, res == 0, false, true, false);
    }

    /**
     * Applies bitwise or operation to the given vectors
     * Flags : Z000
     *
     * @param l (int) 8 bits vector
     * @param r (int) 8 bits vector
     * @return (int) result and flags
     */
    static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int res = l | r;
        return packValueZNHC(res, res == 0, false, false, false);
    }

    /**
     * Adds the two given 8 bits vectors,
     * taking in account the initial carry0
     * and returns the packed result (value and flags)
     * Flags : Z0HC
     *
     * @param left8  (int) 8 bits vector
     * @param right8 (int) 8 bits vector
     * @param carry0 (boolean) initial carry
     * @return (int) packed sum
     */
    static int add(int left8, int right8, boolean carry0) {
        Preconditions.checkBits8(left8);
        Preconditions.checkBits8(right8);
        int sum = left8 + right8 + (carry0 ? 1 : 0);
        sum &= Bits.fullmask(Byte.SIZE);
        boolean h = (left8 & Bits.fullmask(4)) + (right8 & Bits.fullmask(4)) + (carry0 ? 1 : 0) > 0xF;
        boolean c = left8 + right8 + (carry0 ? 1 : 0) > 0xFF;
        return packValueZNHC(sum, sum == 0, false, h, c);
    }

    /**
     * Adds the two given 8 bits vectors,
     * and returns the packed result (value and flags)
     * Flags : Z0HC
     *
     * @param left8  (int) 8 bits vector
     * @param right8 (int) 8 bits vector
     * @return (int) packed sum
     */
    static int add(int left8, int right8) {
        return add(left8, right8, false);
    }

    /**
     * Returns the sum of the two 16 bits vectors,
     * with the flags of the 8 LSBs
     * Flags : 00HC
     *
     * @param left16  (int) 16 bits vector
     * @param right16 (int) 16 bits vector
     * @return (int) packed sum
     */
    static int add16L(int left16, int right16) {
        Preconditions.checkBits16(left16);
        Preconditions.checkBits16(right16);
        int packedSumLow = add(left16 & Bits.fullmask(Byte.SIZE), right16 & Bits.fullmask(Byte.SIZE));
        int packedSumHigh = add(Bits.extract(left16, Byte.SIZE, Byte.SIZE),
                Bits.extract(right16, Byte.SIZE, Byte.SIZE), (packedSumLow & Flag.C.mask()) != 0);
        int unpackedSum = (unpackValue(packedSumHigh) << Byte.SIZE) | unpackValue(packedSumLow);
        boolean h = (packedSumLow & Flag.H.mask()) != 0;
        boolean c = (packedSumLow & Flag.C.mask()) != 0;
        return packValueZNHC(unpackedSum, false, false, h, c);
    }

    /**
     * Returns the sum of the two 16 bits vectors,
     * with the flags of the 8 MSBs
     * Flags : 00HC
     *
     * @param left16  (int) 16 bits vector
     * @param right16 (int) 16 bits vector
     * @return (int) packed sum
     */
    static int add16H(int left16, int right16) {
        Preconditions.checkBits16(left16);
        Preconditions.checkBits16(right16);
        int packedSumLow = add(left16 & Bits.fullmask(Byte.SIZE), right16 & Bits.fullmask(Byte.SIZE));
        int packedSumHigh = add(Bits.extract(left16, Byte.SIZE, Byte.SIZE),
                Bits.extract(right16, Byte.SIZE, Byte.SIZE), (packedSumLow & Flag.C.mask()) != 0);
        int unpackedSum = (unpackValue(packedSumHigh) << Byte.SIZE) | unpackValue(packedSumLow);
        boolean h = (packedSumHigh & Flag.H.mask()) != 0;
        boolean c = (packedSumHigh & Flag.C.mask()) != 0;
        return packValueZNHC(unpackedSum, false, false, h, c);
    }

    /**
     * Substracts the two given 8 bits vectors,
     * taking in account the initial borrow0
     * and returns the packed result (value and flags)
     * Flags : Z1HC
     *
     * @param left8   (int) 8 bits vector
     * @param right8  (int) 8 bits vector
     * @param borrow0 (boolean) initial borrow
     * @return (int) packed difference
     */
    static int sub(int left8, int right8, boolean borrow0) {
        Preconditions.checkBits8(left8);
        Preconditions.checkBits8(right8);

        int sub = left8 - (right8 + (borrow0 ? 1 : 0));
        sub &= Bits.fullmask(Byte.SIZE);
        boolean h = (left8 & Bits.fullmask(4))
                < (right8 & Bits.fullmask(4)) + (borrow0 ? 1 : 0);
        boolean c = left8 < right8 + (borrow0 ? 1 : 0);
        return packValueZNHC(sub, sub == 0, true, h, c);
    }

    /**
     * Substracts the two given 8 bits vectors,
     * and returns the packed result (value and flags)
     * Flags : Z1HC
     *
     * @param left8  (int) 8 bits vector
     * @param right8 (int) 8 bits vector
     * @return (int) packed difference
     */
    static int sub(int left8, int right8) {
        return sub(left8, right8, false);
    }

    /**
     * Adjust the given 8 bits value so that it is in DCB form
     * Flags : ZN0C
     *
     * @param notDCBVal (int) 8 bits vector
     * @param n         (boolean) substraction flag
     * @param h         (boolean) half_carry flag
     * @param c         (boolean) end_carry flag
     * @return (int) packed and adjusted value
     */
    static int bcdAdjust(int notDCBVal, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(notDCBVal);
        boolean fixL = h | (!n && ((notDCBVal & Bits.fullmask(4)) > 9));
        boolean fixH = c | (!n && (notDCBVal > 0x99));
        int fix = 0x60 * (fixH ? 1 : 0) + 0x06 * (fixL ? 1 : 0);
        int vAdjusted = n ? notDCBVal - fix : notDCBVal + fix;
        vAdjusted &= Bits.fullmask(Byte.SIZE);
        return packValueZNHC(vAdjusted, vAdjusted == 0, n, false, fixH);
    }

    /**
     * Applies bitwise xor operation to the given vectors
     * Flags : Z000
     *
     * @param l (int) 8 bits vector
     * @param r (int) 8 bits vector
     * @return (int) result and flags
     */
    static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int res = l ^ r;
        return packValueZNHC(res, res == 0, false, false, false);
    }

    /**
     * Applies shift_left operation (<<) to the given vector
     * Flags : Z00C
     *
     * @param v (int) 8 bits vector
     * @return (int) result and flags
     */
    static int shiftLeft(int v) {
        Preconditions.checkBits8(v);
        int res = (v << 1) & Bits.fullmask(Byte.SIZE);
        return packValueZNHC(res, res == 0, false, false, (v & 0b1000_0000) != 0);
    }

    /**
     * Applies shift_right_arithmetic operation (>>)
     * to the given vector
     * Flags : Z00C
     *
     * @param v (int) 8 bits vector
     * @return (int) result and flags
     */
    static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        int res = (Bits.signExtend8(v) >>> 1) & Bits.fullmask(Byte.SIZE);
        return packValueZNHC(res, res == 0, false, false, (v & 0b0000_0001) != 0);
    }

    /**
     * Applies shift_right_logic operation (>>>)
     * to the given vector
     * Flags : Z00C
     *
     * @param v (int) 8 bits vector
     * @return (int) result and flags
     */
    static int shiftRightL(int v) {
        Preconditions.checkBits8(v);
        int res = v >>> 1;
        return packValueZNHC(res, res == 0, false, false, (v & 0b0000_0001) != 0);
    }

    /**
     * Rotates the given vector in the
     * given direction
     * Flags : Z00C
     *
     * @param d (RotDir) direction
     * @param v (int) 8 bits vector
     * @return (int) result and flags
     */
    static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);
        int res;
        boolean c;
        if (d == RotDir.LEFT) {
            res = Bits.rotate(Byte.SIZE, v, 1);
            c = (res & 1) == 1;
        } else {
            res = Bits.rotate(Byte.SIZE, v, -1);
            c = (v & 1) == 1;
        }
        return packValueZNHC(res, res == 0, false, false, c);
    }

    /**
     * Applies the "rotation through carry"
     * in the given direction
     * Flags : Z00C
     *
     * @param d (RotDir) direction
     * @param v (int) 8 bits vector
     * @param c (boolean) carry
     * @return (int) result and flags
     */
    static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);
        int bits9 = (c ? 0x100 : 0) | v;

        if (d == RotDir.LEFT)
            bits9 = Bits.rotate(9, bits9, 1);
        else
            bits9 = Bits.rotate(9, bits9, -1);

        int res = bits9 & Bits.fullmask(Byte.SIZE);
        boolean c2 = (bits9 & Bits.mask(Byte.SIZE)) != 0;
        return packValueZNHC(res, res == 0, false, false, c2);
    }

    /**
     * Swaps the position of the 4 LSBs
     * with the 4 MSBs
     * Flags : Z000
     *
     * @param v (int) 8 bits vector
     * @return (int) swapped vector and flags
     */
    static int swap(int v) {
        Preconditions.checkBits8(v);
        int res = ((v << 4) | (v >> 4)) & Bits.fullmask(Byte.SIZE);
        return packValueZNHC(res, res == 0, false, false, false);
    }

    /**
     * Return 1 in the position of flag z
     * if the bit at the given index == 0
     * Flags : Z010
     *
     * @param v        (int) 8 bits vector
     * @param bitIndex (int) index of the bit
     * @return (int) value = 0 and flags
     */
    static int testBit(int v, int bitIndex) {
        Preconditions.checkIndex(bitIndex, Byte.SIZE);
        Preconditions.checkBits8(v);
        boolean z = (v & Bits.mask(bitIndex)) == 0;
        return packValueZNHC(0, z, false, true, false);
    }

    /**
     * Returns a vector constitued of the bits
     * corresponding to the flags, with bits
     * at true if the corresponding boolean is true
     *
     * @param z (boolean) zero flag
     * @param n (boolean) substraction flag
     * @param h (boolean) half_carry flag
     * @param c (boolean) end_carry flag
     * @return (int) 8 bits vector
     */
    private static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        return (z ? Flag.Z.mask() : 0) | (n ? Flag.N.mask() : 0)
                | (h ? Flag.H.mask() : 0) | (c ? Flag.C.mask() : 0);
    }

    /**
     * Returns the value packed in the givan
     * value/flags vector
     *
     * @param valueAndFlags (int) value/flags vector
     * @return (int) 8 or 16 bits value
     */
    private static int unpackValue(int valueAndFlags) {
        return Bits.extract(valueAndFlags, VALUE_START, VALUE_SIZE);
    }

    private static int packValueZNHC(int value,
                                     boolean z, boolean n, boolean h, boolean c) {
        return packValueZNHC(value, maskZNHC(z, n, h, c));
    }

    private static int packValueZNHC(int value, int maskZNHC) {
        return (value << VALUE_START) | (maskZNHC << FLAGS_START);
    }

    private Alu() {
    }
}
