package ch.epfl.javaboy.gui.options;

public enum General implements Option {
    ROMS_PATH("Roms_Path:", "/Roms/"),
    LAST_PLAYED_ROM("Last_Rom:", ""),
    AUTO_LOAD("Auto_Load_When_Launching_Rom:", "false");

    public static final String TAG = "<GENERAL>";

    private final String tag, def;
    General(String tag, String defaultValue) {
        this.tag = tag;
        this.def = defaultValue;
    }
    @Override
    public String tag() { return tag; }
    @Override
    public String defaultString() { return def; }
}
