package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.component.Component;

public final class Speaker implements Component {
    
    private final Square1Channel square1;
    private final Square2Channel square2;
    private final WaveChannel wave;
    private final NoiseChannel noise;
    
    public Speaker() {
        square1 = new Square1Channel();
        square2 = new Square2Channel();
        wave = new WaveChannel();
        noise = new NoiseChannel();
    }
    
    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {        
    }
}
