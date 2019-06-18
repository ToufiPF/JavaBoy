package ch.epfl.javaboy.component.sounds.subcomponent;

public final class VolumeEnvelope {

    private static final int MIN_VOLUME = 0, MAX_VOLUME = 15;

    private int currVol;
    private boolean mustInc;

    public VolumeEnvelope(int startingVol) {
        currVol = startingVol;
        mustInc = false;
    }

    public void tick() {
        if (mustInc) {
            ++currVol;
            if (currVol > MAX_VOLUME)
                currVol = MAX_VOLUME;
        } else {
            --currVol;
            if (currVol < MIN_VOLUME)
                currVol = MIN_VOLUME;
        }
    }

    public void setIncrement(boolean inc) {
        mustInc = inc;
    }

    public int getVolume() {
        return currVol;
    }
}
