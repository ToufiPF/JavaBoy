package ch.epfl.javaboy.component.sounds.subcomponent;

public final class LengthCounter {

    private int countdown = 0;

    public void reload(int value) {
        countdown = value;
    }
    
    public void tick() {
        --countdown;
        if (countdown < 0)
            countdown = 0;
    }

    public boolean isEnabled() {
        return countdown != 0;
    }
}
