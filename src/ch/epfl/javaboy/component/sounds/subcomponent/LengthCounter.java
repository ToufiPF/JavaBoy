package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.component.sounds.Enabled;

public final class LengthCounter implements Enabled {

    private int countdown = 0;

    public void reload(int value) {
        countdown = value;
    }

    @Override
    public void cycleIfEnabled(boolean enabled) {
        if (enabled) {
            --countdown;
            if (countdown < 0)
                countdown = 0;
        }
    }

    public boolean isEnabled() {
        return countdown != 0;
    }
}
