package ch.epfl.javaboy.component.sounds;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioLineSoundOutput implements SoundOutput {
    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLES_PER_FRAME = 2;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, Byte.SIZE, SAMPLES_PER_FRAME, true, false);

    private static final int LINE_BUFFER_SIZE = 14700;
    private static final int BUFFER_SIZE = 2048;

    private SourceDataLine line;

    private byte[] soundBufferMix;
    private int index;

    public AudioLineSoundOutput() throws LineUnavailableException {
        line = AudioSystem.getSourceDataLine(FORMAT);
        line.open(FORMAT, LINE_BUFFER_SIZE);

        soundBufferMix = new byte[BUFFER_SIZE];
    }

    @Override
    public void start() {
        line.start();

        index = 0;
    }

    @Override
    public void stop() {
        line.drain();
        line.stop();
    }

    @Override
    public void play(int left, int right) {
        soundBufferMix[index++] = (byte) left;
        soundBufferMix[index++] = (byte) right;

        if (index >= BUFFER_SIZE) {
            int samples = Math.min(BUFFER_SIZE, line.available());
            line.write(soundBufferMix, 0, samples);

            index = 0;
        }
    }
}
