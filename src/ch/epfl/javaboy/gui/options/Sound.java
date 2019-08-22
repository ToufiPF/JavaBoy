package ch.epfl.javaboy.gui.options;

public enum Sound implements Option {
    BUFFER_LENGTH("Buffer_Length:", "2048");

    public static final String TAG = "<SOUND>";

    private final String tag, def;
    Sound(String tag, String defaultString) {
        this.tag = tag;
        this.def = defaultString;
    }
    public String tag() { return tag; }
    public String defaultString() { return def; }
}
