package ch.epfl.javaboy.component.sounds.subcomponent;

import ch.epfl.javaboy.AddressMap;

public class ProgrammableWave implements Ticked {

    private final int[] waveRam;
    private int sample, position;
    private int shiftVolume;

    private int lastWritten;

    public ProgrammableWave(int[] waveRam) {
        this.waveRam = waveRam;
        sample = position = 0;
        shiftVolume = 0;
    }

    public void reset() {
        sample = 0;
    }

    @Override
    public void tick() {
        ++position;
        if (position >= waveRam.length * 2)
            position = 0;
        boolean low = (position & 1) == 1;

        sample = low ? (waveRam[position / 2] & 0x0F) : (waveRam[position / 2] & 0xF0);
    }

    public void setVolumeCode(int volumeCode) {
        switch (volumeCode) {
            case 0b00:
                shiftVolume = 4;
                break;
            case 0b01:
                shiftVolume = 0;
                break;
            case 0b10:
                shiftVolume = 1;
                break;
            case 0b11:
                shiftVolume = 2;
                break;
            default:
                throw new IllegalArgumentException();
        }
    }

    public void trigger() {
        position = 0;
    }

    public int getOutput() {
        return sample >>> shiftVolume;
    }
}
