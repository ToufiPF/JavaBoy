package ch.epfl.javaboy.component.sounds.subcomponent;

public class FrequencySweeper implements Ticked {

    public static boolean overflowCheck(int freq) {
        return 0 <= freq && freq <= 2047;
    }

    // Regs values
    private int channelFrequency = 0;
    private int sweepPeriod = 0;
    private boolean sweepNegate = false;
    private int sweepShift = 0;

    // Internal Flags
    private int timer = 0;
    private boolean sweepEnabled = true;
    private int shadowReg = 0;

    @Override
    public void tick() {
        --timer;
        if (timer <= 0) {
            timer = sweepPeriod;

            if (sweepEnabled && sweepPeriod != 0)
                channelFrequency = computeNewFrequency();
        }
    }

    private int computeNewFrequency() {
        int delta = shadowReg >>> sweepShift;
        return shadowReg + (sweepNegate ? -delta : delta);
    }

    public void setChannelFrequency(int freq) {
        channelFrequency = freq;
    }
    public void setSweepPeriod(int period) {
        sweepPeriod = period;
    }
    public void setSweepNegate(boolean negate) {
        sweepNegate = negate;
    }
    public void setSweepShift(int shift) {
        sweepShift = shift;
    }

    public void trigger() {
        shadowReg = channelFrequency;
        timer = sweepPeriod;
        sweepEnabled = sweepPeriod != 0 || sweepShift != 0;
        if (sweepShift != 0)
            channelFrequency = computeNewFrequency();
    }

    public int getChannelFrequency() {
        return channelFrequency;
    }
}
