package ch.epfl.javaboy.component.sounds.channel;

/**
 * Represents a VolumeEnvelope,
 * a subcomponent of a Channel
 * @author Bryan Johnson (https://github.com/bryanjjohnson/Java-Gameboy-Emulator)
 */
@SuppressWarnings("WeakerAccess")
public class Envelope {
    private int base;
    private boolean increment;
    private int stepLength;
    private int index;

    public int getBase() {
        return base;
    }
    public void setBase(int base) {
        this.base = base;
    }

    public boolean isIncrementing() {
        return increment;
    }
    public void setIncrementing(boolean incrementing) {
        this.increment = incrementing;
    }

    public int getStepLength() {
        return stepLength;
    }
    public void setStepLength(int stepLength) {
        this.stepLength = stepLength;
    }

    public int getIndex() {
        return index;
    }
    public void setIndex(int index) {
        this.index = index;
    }

    public void handleSweep() {
        if (index > 0)
        {
            --index;
            if (index == 0)
            {
                index = stepLength;

                if (isIncrementing() && base < 0xF) {
                    ++base;
                } else if (!isIncrementing() && base > 0) {
                    --base;
                }
            }
        }
    }
}
