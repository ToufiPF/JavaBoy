package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;

public interface Channel extends Component, Clocked {
    void reset();
    void trigger();

    int getOutput();
}
