package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.*;
import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;
import ch.epfl.javaboy.component.sounds.channel.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the SoundController of a GameBoy
 * @author Bryan Johnson (https://github.com/bryanjjohnson/Java-Gameboy-Emulator)
 * @author Toufi
 */
public class SoundController implements Component, Clocked {

    private enum NR implements Register {
        NR10, NR11, NR12, NR13, NR14,
        UNUSED20, NR21, NR22, NR23, NR24,
        NR30, NR31, NR32, NR33, NR34,
        UNUSED40, NR41, NR42, NR43, NR44,
        NR50, NR51, NR52;
        public static final List<NR> ALL = Arrays.asList(values());
    }

    private static final int[] WAVE_RAM_DEFAULT_VALUES = {
            0x84, 0x40, 0x43, 0xAA, 0x2D, 0x78, 0x92, 0x3C,
            0x60, 0x59, 0x59, 0xB0, 0x34, 0xB8, 0x2E, 0xDA
    };

    private static final int[] NR_REGS_MASKS = {
            0x80, 0x3F, 0x00, 0xFF, 0xBF,
            0xFF, 0x3F, 0x00, 0xFF, 0xBF,
            0x7F, 0xFF, 0x9F, 0xFF, 0xBF,
            0xFF, 0xFF, 0x00, 0x00, 0xBF,
            0x00, 0x00, 0x70
    };

    private static final int TICKS_PER_SECOND = AudioLineSoundOutput.SAMPLE_RATE;
    private static final int PERIOD = (int) (GameBoy.CYCLES_PER_SECOND / TICKS_PER_SECOND);
    private static final int CHANNEL_COUNT = 4;

    private static final int STATE_LENGTH = AddressMap.WAVE_RAM_SIZE + NR.ALL.size()
            + Integer.BYTES + Long.BYTES;

    private final SquareWaveChannel channel1;
    private final SquareWaveChannel channel2;
    private final WaveChannel channel3;
    private final NoiseChannel channel4;
    private final ArrayList<BaseChannel> channelList;

    private final RegisterFile<NR> regs;
    private final int[] waveRam;

    private final SoundOutput soundOutput;
    private final byte[] soundBuffer;
    private int left, right;

    private int soundTimer;
    private long lastCycle;

    /**
     * Constructs a SoundController
     * with the given output
     * @param output (SoundOutput)
     */
    public SoundController(SoundOutput output) {
        soundOutput = output;
        soundBuffer = new byte[CHANNEL_COUNT];
        left = right = 0;
        soundTimer = 0;
        lastCycle = 0;

        channel1 = new SquareWaveChannel();
        channel2 = new SquareWaveChannel();
        channel3 = new WaveChannel();
        channel4 = new NoiseChannel();
        channelList = new ArrayList<>(CHANNEL_COUNT);
        channelList.add(channel1);
        channelList.add(channel2);
        channelList.add(channel3);
        channelList.add(channel4);

        regs = new RegisterFile<>(NR.values());
        waveRam = Arrays.copyOf(WAVE_RAM_DEFAULT_VALUES, WAVE_RAM_DEFAULT_VALUES.length);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (AddressMap.REGS_NR_START <= address && address < AddressMap.REGS_NR_END) {
            final int id = address - AddressMap.REGS_NR_START;
            return regs.get(NR.ALL.get(id)) | NR_REGS_MASKS[id];
        }
        if (AddressMap.WAVE_RAM_START <= address && address < AddressMap.WAVE_RAM_END)
            return waveRam[address - AddressMap.WAVE_RAM_START];

        return NO_DATA;
    }
    @Override
    public void write(int address, int value) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(value);

        if (AddressMap.REGS_NR_START <= address && address < AddressMap.REGS_NR_END) {
            final NR reg = NR.ALL.get(address - AddressMap.REGS_NR_START);
            if (reg == NR.NR52)
                regs.set(NR.NR52, (value & 0xF0) | (regs.get(NR.NR52) & 0xF));
            else
                regs.set(reg, value);
        }
        else if (AddressMap.WAVE_RAM_START <= address && address < AddressMap.WAVE_RAM_END) {
            final int id = address - AddressMap.WAVE_RAM_START;
            waveRam[id] = value;
        }
    }

    @Override
    public byte[] saveState() {
        byte[] state = new byte[STATE_LENGTH];

        int baseIndex = 0;
        for (int i = 0 ; i < Integer.BYTES ; ++i)
            state[baseIndex + i] = (byte) Bits.extract(soundTimer, i * Byte.SIZE, Byte.SIZE);
        baseIndex += Integer.BYTES;

        for (int i = 0 ; i < Long.BYTES ; ++i)
            state[baseIndex + i] = (byte) Bits.extract(lastCycle, i * Byte.SIZE, Byte.SIZE);
        baseIndex += Long.BYTES;

        for (int i = 0 ; i < NR.ALL.size() ; ++i)
            state[baseIndex + i] = (byte) regs.get(NR.ALL.get(i));
        baseIndex += NR.ALL.size();

        for (int i = 0 ; i < AddressMap.WAVE_RAM_SIZE ; ++i)
            state[baseIndex + i] = (byte) waveRam[i];

        return state;
    }

    @Override
    public void loadState(byte[] state) {
        if (state.length != STATE_LENGTH)
            throw new IllegalStateException("Invalid state.");

        int baseIndex = 0;
        soundTimer = 0;
        for (int i = 0 ; i < Integer.BYTES ; ++i)
            soundTimer |= Byte.toUnsignedInt(state[baseIndex + i]) << (i * Byte.SIZE);
        baseIndex += Integer.BYTES;

        lastCycle = 0;
        for (int i = 0 ; i < Long.BYTES ; ++i)
            lastCycle |= Byte.toUnsignedLong(state[baseIndex + i]) << (i * Byte.SIZE);
        baseIndex += Long.BYTES;

        for (int i = 0 ; i < NR.ALL.size() ; ++i)
            regs.set(NR.ALL.get(i), Byte.toUnsignedInt(state[baseIndex + i]));
        baseIndex += NR.ALL.size();

        for (int i = 0 ; i < AddressMap.WAVE_RAM_SIZE ; ++i)
            waveRam[i] = Byte.toUnsignedInt(state[baseIndex + i]);
    }

    @Override
    public void cycle(long cycle) {
        soundTimer += cycle - lastCycle;
        lastCycle = cycle;

        if (soundTimer >= PERIOD) {
            soundTimer -= PERIOD;
            initChannels();

            if (isSoundControllerOn()) {
                updateChannel1();
                updateChannel2();
                updateChannel3();
                updateChannel4();

                mixSound();
            } else {
                left = right = 0;
            }
            soundOutput.play(left, right);
        }
    }

    /**
     * Starts the audio
     */
    public void startAudio() {
        soundTimer = 0;
        soundOutput.start();
        for (BaseChannel c : channelList)
            c.setOn(false);
    }

    /**
     * Stops the audio
     */
    public void stopAudio() {
        soundOutput.stop();
    }

    private void initChannels() {
        initChannel1();
        initChannel2();
        initChannel3();
        initChannel4();
    }

    private void initChannel1() {
        if (isChannelTriggered(1)) {
            removeChannelTrigger(1);
            setChannelOn(1);

            int nr10 = regs.get(NR.NR10);
            int nr11 = regs.get(NR.NR11);
            int nr12 = regs.get(NR.NR12);
            int nr13 = regs.get(NR.NR13);
            int nr14 = regs.get(NR.NR14);

            channel1.setOn(true);
            channel1.setWaveDuty((nr11 >> 6) & 0x3);
            channel1.setIndex(0);

            channel1.setGbFreq((nr13 | ((nr14 & 0x7) << 8)) & 0x7FF);
            channel1.setFreq((float)131072 / (2048 - channel1.getGbFreq()));

            if ((nr14 & 0x40) == 0x40) // stop output at length
            {
                channel1.setCount(true);
                channel1.setLength(((64 - (nr11 & 0x3F)) * TICKS_PER_SECOND) / 256);
            }
            else
            {
                channel1.setCount(false);
            }

            Envelope volume = new Envelope();
            volume.setBase((nr12 >> 4) & 0x0F);
            volume.setIncrementing((nr12 & 0x8) == 0x8);
            volume.setStepLength((nr12 & 0x7) * TICKS_PER_SECOND / 64);
            volume.setIndex(volume.getStepLength());
            channel1.setVolume(volume);

            channel1.setSweepLength(((nr10 >> 4) & 0x7) * TICKS_PER_SECOND / 128);
            channel1.setSweepIndex(channel1.getSweepLength());
            channel1.setSweepDirection((nr10 & 0x8) == 0x8 ? -1 : 1);
            channel1.setSweepShift(nr10 & 0x7);
        }
    }
    private void updateChannel1() {
        if (channel1.isOn())
        {
            channel1.incIndex();

            int i = (int) ((32 * channel1.getFreq() * channel1.getIndex()) / TICKS_PER_SECOND) % 32;
            int value = channel1.getWave()[i];
            soundBuffer[0] = (byte) (value * channel1.getVolume().getBase());

            if (channel1.isCount() && channel1.getLength() > 0) {
                channel1.decLength();
                if (channel1.getLength() == 0) {
                    channel1.setOn(false);
                    setChannelOff(1);
                }
            }

            channel1.getVolume().handleSweep();

            if (channel1.getSweepIndex() > 0 && channel1.getSweepLength() > 0) {
                channel1.decSweepIndex();

                if (channel1.getSweepIndex() == 0) {
                    channel1.setSweepIndex(channel1.getSweepLength());
                    channel1.setGbFreq(channel1.getGbFreq() + (channel1.getGbFreq() >> channel1.getSweepShift()) * channel1.getSweepDirection());
                    if (channel1.getGbFreq() > 2047) {
                        channel1.setOn(false);
                        setChannelOff(1);
                    }
                    else {
                        regs.set(NR.NR13, channel1.getGbFreq() & 0xFF);
                        regs.set(NR.NR14, (regs.get(NR.NR14) & 0xF8) | ((channel1.getGbFreq() >> 8) & 0x7));
                        channel1.setFreq((float)131072 / (2048 - channel1.getGbFreq()));
                    }
                }
            }
        }
    }

    private void initChannel2() {
        if (isChannelTriggered(2)) {
            removeChannelTrigger(2);
            setChannelOn(2);

            int nr21 = regs.get(NR.NR21);
            int nr22 = regs.get(NR.NR22);
            int nr23 = regs.get(NR.NR23);
            int nr24 = regs.get(NR.NR24);

            channel2.setOn(true);
            channel2.setWaveDuty((nr21 >> 6) & 0x3);
            channel2.setIndex(0);

            int freqX = nr23 | ((nr24 & 0x7) << 8);
            channel2.setFreq((float)131072 / (2048 - freqX));

            if ((nr24 & 0x40) == 0x40) // stop output at length
            {
                channel2.setCount(true);
                channel2.setLength(((64 - (nr21 & 0x3F)) * TICKS_PER_SECOND) / 256);
            }
            else
            {
                channel2.setCount(false);
            }

            Envelope volume = new Envelope();
            volume.setBase((nr22 >> 4) & 0x0F);
            volume.setIncrementing((nr22 & 0x8) == 0x8);
            volume.setStepLength((nr22 & 0x7) * TICKS_PER_SECOND / 64);
            volume.setIndex(volume.getStepLength());
            channel2.setVolume(volume);
        }
    }
    private void updateChannel2() {
        if (channel2.isOn())
        {
            channel2.incIndex();

            int i = (int) ((32 * channel2.getFreq() * channel2.getIndex()) / TICKS_PER_SECOND) % 32;
            int value = channel2.getWave()[i];
            soundBuffer[1] = (byte) (value * channel2.getVolume().getBase());

            if (channel2.isCount() && channel2.getLength() > 0)
            {
                channel2.decLength();
                if (channel2.getLength() == 0)
                {
                    channel2.setOn(false);
                    setChannelOff(2);
                }
            }
            channel2.getVolume().handleSweep();
        }
    }

    private void initChannel3() {
        if (isChannelTriggered(3)) {
            removeChannelTrigger(3);
            setChannelOn(3);

            channel3.setIndex(0);

            int nr30 = regs.get(NR.NR30);
            int nr31 = regs.get(NR.NR31);
            int nr33 = regs.get(NR.NR33);
            int nr34 = regs.get(NR.NR34);

            channel3.setOn((nr30 & 0x80) == 0x80);

            int freqX3 = nr33 | ((nr34 & 0x7) << 8);
            channel3.setFreq((float)65536 / (2048 - freqX3));

            int[] channel3wav = new int[32];
            for (int i = 0; i < AddressMap.WAVE_RAM_END - AddressMap.WAVE_RAM_START; i++)
            {
                channel3wav[i * 2] = (waveRam[i] >> 4) & 0xF;
                channel3wav[i * 2 + 1] = waveRam[i] & 0xF;
            }
            channel3.setWave(channel3wav);

            if ((nr34 & 0x40) == 0x40) // stop output at length
            {
                channel3.setCount(true);
                channel3.setLength((256 - nr31) * TICKS_PER_SECOND / 256);
            }
            else
            {
                channel3.setCount(false);
            }
        }
    }
    private void updateChannel3() {
        if (channel3.isOn())
        {
            int nr30 = regs.get(NR.NR30);
            int nr32 = regs.get(NR.NR32);

            channel3.incIndex();

            int i = (int) ((32 * channel3.getFreq() * channel3.getIndex()) / 44100) % 32;
            int value = channel3.getWave()[i];
            if ((nr32 & 0x60) != 0x0) {
                value >>= (((nr32 >> 5) & 0x3) - 1);
            } else {
                value = 0;
            }
            value <<= 1;

            if ((nr30 & 0x80) == 0x80)
                soundBuffer[2] = (byte) (value - 0xF);
            else
                soundBuffer[2] = 0;

            if (channel3.isCount() && channel3.getLength() > 0)
            {
                channel3.decLength();
                if (channel3.getLength() == 0)
                {
                    channel3.setOn(false);
                    setChannelOff(3);
                }
            }
        }
    }

    private void initChannel4() {
        if (isChannelTriggered(4)) {
            removeChannelTrigger(4);
            setChannelOn(4);

            int nr41 = regs.get(NR.NR41);
            int nr42 = regs.get(NR.NR42);
            int nr43 = regs.get(NR.NR43);
            int nr44 = regs.get(NR.NR44);

            channel4.setOn(true);
            channel4.setIndex(0);

            if ((nr44 & 0x40) == 0x40) // stop output at length
            {
                channel4.setCount(true);
                channel4.setLength((64 - (nr41 & 0x3F)) * TICKS_PER_SECOND / 256);
            }
            else
            {
                channel4.setCount(false);
            }

            Envelope volume = new Envelope();
            volume.setBase((nr42 >> 4) & 0x0F);
            volume.setIncrementing((nr42 & 0x8) == 0x8);
            volume.setStepLength((nr42 & 0x7) * TICKS_PER_SECOND / 64);
            volume.setIndex(volume.getStepLength());
            channel4.setVolume(volume);

            channel4.setShiftFreq(((nr43 >> 4) & 0xF) + 1);
            channel4.setCounterStep((nr43 & 0x8) == 0x8 ? 1 : 0);
            channel4.setDivRatio(nr43 & 0x7);
            if (channel4.getDivRatio() == 0)
                channel4.setDivRatio(0.5F);
            channel4.setFreq((int) (524288 / channel4.getDivRatio()) >> channel4.getShiftFreq());
        }
    }
    private void updateChannel4() {
        if (channel4.isOn())
        {
            channel4.incIndex();

            byte value;
            if (channel4.getCounterStep() == 1) {
                int i = (int) ((channel4.getFreq() * channel4.getIndex()) / TICKS_PER_SECOND) % 0x7F;
                value = (byte) ((NoiseChannel.noise7[i >> 3] >> (i & 0x7)) & 0x1);
            } else {
                int i = (int) ((channel4.getFreq() * channel4.getIndex()) / TICKS_PER_SECOND) % 0x7FFF;
                value = (byte) ((NoiseChannel.noise15[i >> 3] >> (i & 0x7)) & 0x1);
            }
            soundBuffer[3] = (byte) ((value * 2 - 1) * channel4.getVolume().getBase());

            if (channel4.isCount() && channel4.getLength() > 0)
            {
                channel4.decLength();
                if (channel4.getLength() == 0)
                {
                    channel4.setOn(false);
                    setChannelOff(4);
                }
            }
            channel4.getVolume().handleSweep();
        }
    }

    private void mixSound() {
        int leftAmp = 0;
        for (int i = 0 ; i < 4 ; ++i)
            if (isChannelToLeftMixer(i + 1) && channelList.get(i).isOn())
                leftAmp += soundBuffer[i];
        leftAmp *= getLeftSoundLevel();
        leftAmp /= 4;

        int rightAmp = 0;
        for (int i = 0 ; i < 4 ; ++i)
            if (isChannelToRightMixer(i + 1) && channelList.get(i).isOn())
                rightAmp += soundBuffer[i];
        rightAmp *= getRightSoundLevel();
        rightAmp /= 4;

        if (leftAmp > 127)
            leftAmp = 127;
        if (rightAmp > 127)
            rightAmp = 127;
        if (leftAmp < -127)
            leftAmp = -127;
        if (rightAmp < -127)
            rightAmp = -127;

        left = leftAmp;
        right = rightAmp;
    }

    private boolean isSoundControllerOn() {
        return (regs.get(NR.NR52) & 0x80) != 0;
    }

    private boolean isChannelTriggered(int channelNum) {
        NR reg;
        switch (channelNum) {
            case 1:
                reg = NR.NR14;
                break;
            case 2:
                reg = NR.NR24;
                break;
            case 3:
                reg = NR.NR34;
                break;
            case 4:
                reg = NR.NR44;
                break;
            default:
                throw new IllegalStateException();
        }
        return (regs.get(reg) & 0x80) != 0;
    }
    private void removeChannelTrigger(int channelNum) {
        NR reg;
        switch (channelNum) {
            case 1:
                reg = NR.NR14;
                break;
            case 2:
                reg = NR.NR24;
                break;
            case 3:
                reg = NR.NR34;
                break;
            case 4:
                reg = NR.NR44;
                break;
            default:
                throw new IllegalStateException();
        }
        regs.set(reg, regs.get(reg) & 0x7F);
    }

    private void setChannelOn(int channelNum) {
        int mask = 1 << (channelNum - 1);
        regs.set(NR.NR52, regs.get(NR.NR52) | mask);
    }
    private void setChannelOff(int channelNum) {
        int mask = 1 << (channelNum - 1);
        regs.set(NR.NR52, regs.get(NR.NR52) & ~mask);
    }

    private boolean isChannelToLeftMixer(int channelNum) {
        int mask = 1 << (4 + channelNum - 1);
        return (regs.get(NR.NR51) & mask) != 0;
    }
    private boolean isChannelToRightMixer(int channelNum) {
        int mask = 1 << (channelNum - 1);
        return (regs.get(NR.NR51) & mask) != 0;
    }

    private int getLeftSoundLevel() {
        return (regs.get(NR.NR50) >> 4) & 0x7;
    }
    private int getRightSoundLevel() {
        return regs.get(NR.NR50) & 0x7;
    }
}
