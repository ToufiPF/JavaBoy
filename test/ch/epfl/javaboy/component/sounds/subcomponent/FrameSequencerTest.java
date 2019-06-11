package ch.epfl.javaboy.component.sounds.subcomponent;

import org.junit.jupiter.api.Test;

public class FrameSequencerTest {
    @Test
    void test() {
        FrameSequencer fs = new FrameSequencer();
        for (long i = 0 ; i < 100_000L ; ++i) {
            fs.cycle(i);
            if (fs.enable512Hz()) {
                System.out.println(fs.enable512Hz() + " " + fs.enable256Hz() + " "
                        + fs.enable128Hz() + " " + fs.enable64Hz());
            }
        }
    }
}
