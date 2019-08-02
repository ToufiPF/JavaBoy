package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.GameBoy;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioLineSoundOutput implements SoundOutput {
    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLES_PER_FRAME = 2;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, Byte.SIZE, SAMPLES_PER_FRAME, true, false);

    private static final int BUFFER_SIZE = 2048;

    private SourceDataLine line;

    private byte[] soundBufferMix;
    private int soundBufferIndex;

    public AudioLineSoundOutput() throws LineUnavailableException {
        line = AudioSystem.getSourceDataLine(FORMAT);
        line.open(FORMAT, 11025);

        soundBufferMix = new byte[BUFFER_SIZE];
    }

    @Override
    public void start() {
        line.start();
        soundBufferIndex = 0;
    }

    @Override
    public void stop() {
        line.drain();
        line.stop();
    }

    @Override
    public void play(int left, int right) {
        soundBufferMix[(soundBufferIndex * 2)] = (byte) left;
        soundBufferMix[(soundBufferIndex * 2) + 1] = (byte) right;

        ++soundBufferIndex;
        if (soundBufferIndex >= BUFFER_SIZE / 2) {
            int samples = Math.min(BUFFER_SIZE, line.available());
            line.write(soundBufferMix, 0, samples);
            soundBufferIndex = 0;
        }
    }
}
