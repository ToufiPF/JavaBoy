package ch.epfl.javaboy.component.sounds.subcomponent;

public class LengthCounter implements Ticked {

    // Regs values
    private boolean lcEnabled = false;

    // Internal Flags
    private final int maxLength;
    private int counter;

    public LengthCounter(int maximumLength) {
        maxLength = maximumLength;
        counter = 0;
    }

    @Override
    public void tick() {
        if (!lcEnabled)
            return;
        if (counter > 0)
            --counter;
    }

    public void setCountingEnabled(boolean enabled) {
        lcEnabled = enabled;
    }
    public void reload(int period) {
        counter = period;
    }

    public void trigger() {
        if (counter == 0)
            counter = maxLength;
    }

    public boolean channelEnabled() {
        return counter != 0;
    }
}
