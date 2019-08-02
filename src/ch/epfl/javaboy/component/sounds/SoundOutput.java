package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.GameBoy;

import javax.sound.sampled.AudioFormat;

public interface SoundOutput {

    void start();
    void stop();
    void play(int left, int right);

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
