package ch.epfl.javaboy.component.sounds.subcomponent;

public class VolumeEnvelope implements Ticked {

    private final static int MIN_VOL = 0, MAX_VOL = 0xF;

    // Reg values
    private int startVol = 0;
    private boolean envIncrement = false;
    private int envPeriod = 0;

    // Internal flags
    private int volume = 0, timer = 0;
    private boolean autoIncDec = false;

    @Override
    public void tick() {
        if (envPeriod == 0)
            return;

        --timer;
        if (timer == 0) {
            timer = envPeriod;

            if (!autoIncDec)
                return;

            if (envIncrement) {
                ++volume;
                if (volume > MAX_VOL) {
                    volume = MAX_VOL;
                    autoIncDec = false;
                }
            } else {
                --volume;
                if (volume < MIN_VOL) {
                    volume = MIN_VOL;
                    autoIncDec = false;
                }
            }
        }
    }

    public void setEnvelopePeriod(int period) {
        envPeriod = period;
    }
    public void setIncrementMode(boolean increment) {
        envIncrement = increment;
    }
    public void setStartingVolume(int startingVolume) {
        startVol = startingVolume;
    }

    public void trigger() {
        volume = startVol;
        timer = envPeriod;
        //TODO: Zombie mode
    }

    public int getVolume() {
        return volume;
    }
}
