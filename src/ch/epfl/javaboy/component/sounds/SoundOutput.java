package ch.epfl.javaboy.component.sounds;

/**
 * SoundOutput
 * Represents a system for the output
 * of sound.
 * @author Toufi
 */
public interface SoundOutput {

    /**
     * Starts the sound output
     */
    void start();

    /**
     * Stops the sound output
     */
    void stop();

    /**
     * Plays the two given bytes
     * @param left (int) 8bits value
     * @param right (int) 8bits value
     */
    void play(int left, int right);

    /**
     * Represents a blank output that
     * does nothing.
     */
    @SuppressWarnings("unused")
    SoundOutput NULL_OUTPUT = new SoundOutput() {
        @Override
        public void start() {
        }
        @Override
        public void stop() {
        }
        @Override
        public void play(int left, int right) {
        }
    };
}
