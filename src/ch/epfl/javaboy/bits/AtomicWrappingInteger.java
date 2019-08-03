package ch.epfl.javaboy.bits;

import ch.epfl.javaboy.Preconditions;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicWrappingInteger {
    private final AtomicLong counter = new AtomicLong(0);
    private final int modulo;

    public AtomicWrappingInteger(int maximum) {
        Preconditions.checkArgument(maximum > 0);
        modulo = maximum;
    }

    public int get() {
        return (int) (counter.get() % modulo);
    }
    public void set(int value) {
        counter.set(value);
    }

    public int incrementAndGet() {
        return (int) (counter.incrementAndGet() % modulo);
    }
}
