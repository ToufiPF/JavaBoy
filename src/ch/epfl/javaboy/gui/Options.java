package ch.epfl.javaboy.gui;

final class Options {

    final static String OPTIONS_FILE_PATH = "";
    final static String OPTIONS_FILE_NAME = "options.ini";

    final static String GENERAL_TAG = "<GENERAL>";
    final static String ROMS_PATH_TAG = "Roms_Path:", DEFAULT_ROMS_PATH = "/Roms/";
    final static String LAST_PLAYED_ROM_TAG = "Last_Rom:";
    final static String AUTO_LOAD_TAG = "Auto_Load_When_Launching_Rom:";

    final static String SOUND_TAG = "<SOUND>";

    final static String KEYMAP_FILE_PATH = "";
    final static String KEYMAP_FILE_NAME = "keymap.ini";

    private Options() {
    }
}
