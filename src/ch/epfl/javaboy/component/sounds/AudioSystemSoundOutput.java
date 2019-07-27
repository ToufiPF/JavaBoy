package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.Preconditions;
import ch.epfl.javaboy.component.sounds.SoundOutput;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioSystemSoundOutput implements SoundOutput {
    private final static int SAMPLE_RATE = 22050;
    private final static int BUFFER_SIZE = 1024;

    private final static AudioFormat FORMAT = new AudioFormat(AudioFormat.Encoding.PCM_UNSIGNED, SAMPLE_RATE, 8, 2, 2, SAMPLE_RATE, false);

    private SourceDataLine line;
    private byte[] buffer;

    private final int period;
    private int timer;
    private int i;

    public AudioSystemSoundOutput() {
        line = null;
        buffer = null;
        period = (int) (GameBoy.CYCLES_PER_SECOND / FORMAT.getSampleRate());
        timer = period;
        i = 0;
    }

    @Override
    public void start() {
        if (line != null) {
            System.err.println("Sound already started !");
            return;
        }
        System.out.println("Starting sound.");
        try {
            line = AudioSystem.getSourceDataLine(FORMAT);
            line.open(FORMAT, BUFFER_SIZE);
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        line.start();
        buffer = new byte[line.getBufferSize()];
    }
    @Override
    public void stop() {
        if (line == null) {
            System.err.println("Sound was not started !");
            return;
        }
        System.out.println("Stopping sound.");
        line.drain();
        line.stop();
        line = null;
        timer = period;
        buffer = null;
        i = 0;
    }
    @Override
    public void play(int left, int right) {
        --timer;
        if (timer <= 0) {
            timer = period;
            Preconditions.checkBits8(left);
            Preconditions.checkBits8(right);
            buffer[i++] = (byte) left;
            buffer[i++] = (byte) right;
            if (i > BUFFER_SIZE / 2) {
                line.write(buffer, 0, i);
                i = 0;
            }
        }
    }
}
