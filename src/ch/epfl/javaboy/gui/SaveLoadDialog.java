package ch.epfl.javaboy.gui;

import ch.epfl.javaboy.GameBoy;
import javafx.scene.control.Dialog;

import java.io.File;

public final class SaveLoadDialog extends Dialog<String> {

    public static final String AUTO_SAVE_NAME = "auto.dat";
    public static final String QUICK_SAVE_NAME = "quick.dat";

    public static String getAbsoluteSaveStatePath(String romName) {
        romName = romName.replace('.', '-');
        File f = new File("");
        return f.getAbsolutePath() + "/Saves/" + romName + '/';
    }

    private final String absolutePath = new File("").getAbsolutePath() + "/Saves/";
    private String rom = "";

    public void setRomName(String romName) {
        rom = romName.replace('.', '-');
    }

    public String getSavePath(int saveSlot) {
        return absolutePath + rom + "/save" + saveSlot + ".dat";
    }
    public String getQuickSavePath() {
        return absolutePath + rom + '/' + QUICK_SAVE_NAME;
    }
    public String getAutoSavePath() {
        return absolutePath + rom + '/' + AUTO_SAVE_NAME;
    }
}
