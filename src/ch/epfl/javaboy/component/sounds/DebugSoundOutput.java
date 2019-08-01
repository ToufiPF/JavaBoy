package ch.epfl.javaboy.component.sounds;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class DebugSoundOutput implements SoundOutput {

    private FileWriter fos = null;

    @Override
    public void start() {
        System.out.println("Sound starting !");
        try {
            if (fos != null)
                fos.close();
            fos = new FileWriter("log.txt");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        System.out.println("Sound stopping !");
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void play(int left, int right) {
        if (left >= 256 || right >= 256)
            System.err.println("Left/Right outside boundaries : " + left + "/" + right);
        /*
        try {
            fos.write(left + "/" + right + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
    }
}
