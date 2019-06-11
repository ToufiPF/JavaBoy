package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.component.sounds.Enabled;

public final class VolumeEnvelope implements Enabled {
    
    private static final int MIN_VOLUME = 0, MAX_VOLUME = 15;
    
    private int currVol;
    private boolean mustInc;
    
    public VolumeEnvelope(int startingVol) {
        currVol = startingVol;
        mustInc = false;
    }
    
    @Override
    public void cycleIfEnabled(boolean enabled) {
        if (enabled) {
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
    }
    
    public void setIncrement(boolean inc) {
        mustInc = inc;
    }
    
    public int getVolume() {
        return currVol;
    }
}
