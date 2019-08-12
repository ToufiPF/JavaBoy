package ch.epfl.javaboy.gui.savestates;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.component.lcd.LcdController;
import ch.epfl.javaboy.component.lcd.LcdImage;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

public final class StatesDialog extends Dialog<String> {

    private static final String AUTO_STATE = "auto";
    private static final String QUICK_STATE = "quick";
    private static final String REGULAR_STATE = "save";

    private final String statesPath;
    private final String autoStatePath, quickStatePath, regularStatePath;

    /**
     * Creates a new SaveLoadDialog for the given rom
     * @param romName (String) name of the rom
     */
    public StatesDialog(String romName) {
        // Dialog()
        super();
        statesPath = State.getStatesPath(romName);
        File statesFile = new File(statesPath);
        if (!statesFile.exists()) {
            try {
                Files.createDirectories(statesFile.toPath());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        autoStatePath = statesPath + AUTO_STATE;
        quickStatePath = statesPath + QUICK_STATE;
        regularStatePath = statesPath + REGULAR_STATE;
        setTitle("Save and Load States");

        // Button Types
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Grid

    }

    public boolean autoSaveFilesExist() {
        return new File(autoStatePath + ".dat").exists()
                && new File(autoStatePath + ".meta").exists();
    }
    public void autoSave(GameBoy gb) throws IOException {
        autoSave(new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }
    public void autoSave(State state) throws IOException {
        State.saveState(autoStatePath, state);
        System.out.println("AutoSaved : " + state.getDateAndTime());
    }
    public State autoLoad() throws IOException {
        State state = State.loadState(autoStatePath);
        System.out.println("AutoLoaded : " + state.getDateAndTime());
        return state;
    }

    public boolean quickSaveFilesExist() {
        return new File(quickStatePath + ".dat").exists()
                && new File(quickStatePath + ".meta").exists();
    }
    public void quickSave(GameBoy gb) throws IOException {
        quickSave(new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }
    public void quickSave(State state) throws IOException {
        State.saveState(quickStatePath, state);
        System.out.println("QuickSaved : " + state.getDateAndTime());
    }
    public State quickLoad() throws IOException {
        State state = State.loadState(quickStatePath);
        System.out.println("QuickLoaded : " + state.getDateAndTime());
        return state;
    }

    public boolean regularSaveFilesExist(int slot) {
        return new File(regularStatePath + slot + ".dat").exists()
                && new File(regularStatePath + slot + ".meta").exists();
    }
    public void regularSave(GameBoy gb, int slot) throws IOException {
        regularSave(new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()), slot);
    }
    public void regularSave(State state, int slot) throws IOException {
        State.saveState(regularStatePath + slot, state);
        System.out.println("Saved in slot " + slot + " : " + state.getDateAndTime());
    }
    public State regularLoad(int slot) throws IOException {
        State state = State.loadState(regularStatePath + slot);
        System.out.println("Loaded in slot " + slot + " : " + state.getDateAndTime());
        return state;
    }
}
