package ch.epfl.javaboy;

import ch.epfl.javaboy.component.Component;

public class DebugPrinter implements Component {
    @Override
    public int read(int address) {
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        if (address == 0xFF01)
            System.out.print((char)data);
    }
}
