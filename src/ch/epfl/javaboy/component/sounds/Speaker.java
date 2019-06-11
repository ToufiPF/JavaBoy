package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.component.Component;

public final class Speaker implements Component {

    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int value) {        
    }
}
