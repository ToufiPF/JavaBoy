package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.bits.AtomicWrappingInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class AudioLineSoundOutput implements SoundOutput {
    public static final int SAMPLE_RATE = 44100;
    private static final int SAMPLES_PER_FRAME = 2;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, Byte.SIZE, SAMPLES_PER_FRAME, true, false);
    private static final int LINE_BUFFER_SIZE = 8820;

    private static final int BUFFER_SIZE = 4000;
    private static final int BUFFER_COUNT = 2;

    private SourceDataLine line;
    private boolean playing;

    private byte[][] buffers;
    private final ArrayList<AtomicInteger> indexes;
    private final AtomicWrappingInteger sel;

    private Thread playback;

    public AudioLineSoundOutput() throws LineUnavailableException {
        line = AudioSystem.getSourceDataLine(FORMAT);
        line.open(FORMAT, LINE_BUFFER_SIZE);

        playback = new Thread(() -> {
            while (playing) {
                emptyBuffersIfFull();
            }
        });
        playback.setDaemon(true);
        buffers = new byte[BUFFER_COUNT][BUFFER_SIZE];
        indexes = new ArrayList<>();
        for (int i = 0 ; i < BUFFER_COUNT ; ++i)
            indexes.add(new AtomicInteger(0));
        sel = new AtomicWrappingInteger(BUFFER_COUNT);
    }

    @Override
    public void start() {
        line.start();
        playing = true;
        playback.start();

        for (AtomicInteger i : indexes)
            i.set(0);
        sel.set(0);
    }

    @Override
    public void stop() {
        playing = false;

        line.drain();
        line.stop();
    }

    @Override
    public void play(int left, int right) {
        if (!playing)
            return;

        int selec = sel.get();
        if (indexes.get(selec).get() >= BUFFER_SIZE)
            return;

        buffers[selec][indexes.get(selec).getAndIncrement()] = (byte) left;
        buffers[selec][indexes.get(selec).getAndIncrement()] = (byte) right;

        if (indexes.get(selec).get() >= BUFFER_SIZE)
            sel.incrementAndGet();
    }

    private void emptyBuffersIfFull() {
        for (int i = 0 ; i < BUFFER_COUNT ; ++i) {
            if (indexes.get(i).get() >= BUFFER_SIZE) {
                line.write(buffers[i], 0, BUFFER_SIZE);
                indexes.get(i).set(0);
            }
        }
    }
}
