package ch.epfl.javaboy.component.sounds;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioLineSoundOutput implements SoundOutput {
    public static final int SAMPLE_RATE = 44100;
    private static final int SAMPLES_PER_FRAME = 2;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, Byte.SIZE, SAMPLES_PER_FRAME, true, false);
    private static final int LINE_BUFFER_SIZE = 11025;

    private static final int BUFFER_SIZE = 2048;
    private static final int BUFFER_COUNT = 2;

    private SourceDataLine line;
    private boolean playing;

    private byte[][] buffers;
    private int[] indexes;
    int sel;

    private Thread playback;

    public AudioLineSoundOutput() throws LineUnavailableException {
        line = AudioSystem.getSourceDataLine(FORMAT);
        line.open(FORMAT, LINE_BUFFER_SIZE);

        playback = new Thread(() -> {
            while (playing) {
                emptyBuffer();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                }
            }
        });
        playback.setDaemon(true);
    }

    @Override
    public void start() {
        line.start();
        playing = true;
        playback.start();


        buffers = new byte[BUFFER_COUNT][BUFFER_SIZE];
        indexes = new int[] { 0, 0 };
        sel = 0;
    }

    @Override
    public void stop() {
        playing = false;

        line.drain();
        line.stop();
    }

    @Override
    public void play(int left, int right) {
        if (!playing || indexes[sel] >= BUFFER_SIZE) {
            //System.err.println("Buffer Overflow !");
            return;
        }
        buffers[sel][indexes[sel]++] = (byte) left;
        buffers[sel][indexes[sel]++] = (byte) right;
    }

    private void emptyBuffer() {
        int prev = sel;
        ++sel;
        if (sel == BUFFER_COUNT)
            sel = 0;
        line.write(buffers[prev], 0, indexes[prev]);
        indexes[prev] = 0;
    }
}
