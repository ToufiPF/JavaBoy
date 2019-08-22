package ch.epfl.javaboy.gui.options;

public interface Option {
    String OPTIONS_FILE_PATH = "", OPTIONS_FILE_NAME = "options.ini";
    String KEYMAP_FILE_PATH = "", KEYMAP_FILE_NAME = "keymap.ini";

    String tag();
    String defaultString();
}
