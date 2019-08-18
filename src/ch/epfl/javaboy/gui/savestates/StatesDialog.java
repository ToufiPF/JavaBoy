package ch.epfl.javaboy.gui.savestates;

import ch.epfl.javaboy.GameBoy;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("WeakerAccess")
abstract class StatesDialog<T> extends Dialog<T> {
    private static final String ABSOLUTE_SAVE_PATH = new File("").getAbsolutePath() + "/Saves/";
    static final String AUTO_STATE = "auto";
    static final String QUICK_STATE = "quick";
    static final String REGULAR_STATE = "save";

    static final int REGULAR_SAVE_SLOTS = 5;
    static final int SPECIAL_SAVE_SLOTS = 2;

    private final static int DIALOG_WIDTH = 300;
    private final static int DIALOG_HEIGHT = 500;

    private static String statesPathForRom(String romName) {
        return ABSOLUTE_SAVE_PATH + romName.replace('.', '-') + '/';
    }

    private String statesPath;

    final VBox layout;
    final List<StateNode> specialNodes, regularNodes;

    /**
     * Creates a new StateDialog
     */
    StatesDialog() {
        // Dialog()
        super();
        setResizable(false);
        specialNodes = new ArrayList<>(SPECIAL_SAVE_SLOTS);
        regularNodes = new ArrayList<>(REGULAR_SAVE_SLOTS);

        // Button Types
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        setResultConverter(buttonType -> null);
        getDialogPane().setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        getDialogPane().setMaxSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        getDialogPane().setMinSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            setX((bounds.getWidth() - DIALOG_WIDTH) / 2);
            setY((bounds.getHeight() - DIALOG_HEIGHT) / 2);
        }

        // ScrollPane
        layout = new VBox();
        layout.setSpacing(10);
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setContent(layout);
        getDialogPane().setContent(scrollPane);
    }

    public void setRomName(String romName) {
        statesPath = statesPathForRom(romName);
        File statesFile = new File(statesPath);
        if (!statesFile.exists()) {
            try {
                Files.createDirectories(statesFile.toPath());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        refreshStateNodes();
    }
    protected void refreshStateNodes() {
        // Auto & QuickSaves
        try {
            specialNodes.clear();
            if (autoSaveFilesExist())
                specialNodes.add(new StateNode("AutoSave", State.loadMetadata(statesPath + AUTO_STATE)));
            if (quickSaveFilesExist())
                specialNodes.add(new StateNode("QuickSave", State.loadMetadata(statesPath + QUICK_STATE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Regular Saves
        try {
            regularNodes.clear();
            for (int i = 0; i < REGULAR_SAVE_SLOTS; ++i)
                if (regularSaveFilesExist(i))
                    regularNodes.add(new StateNode("Save " + i, State.loadMetadata(statesPath + REGULAR_STATE + i)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save(String saveName, GameBoy gb) throws IOException {
        save(saveName, new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }
    private void save(String saveName, State state) throws IOException {
        State.saveState(statesPath + saveName, state);
        System.out.println("Saved : " + saveName + ", " + state.getDateAndTime());
        refreshStateNodes();
    }
    public State load(String saveName) throws IOException {
        State state = State.loadState(statesPath + saveName);
        System.out.println("Loaded : " + saveName + ", " + state.getDateAndTime());
        return state;
    }

    public boolean autoSaveFilesExist() {
        return new File(statesPath + AUTO_STATE + ".dat").exists()
                && new File(statesPath + AUTO_STATE + ".meta").exists();
    }
    public void autoSave(GameBoy gb) throws IOException {
        autoSave(new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }
    public void autoSave(State state) throws IOException {
        save(AUTO_STATE, state);
    }
    public State autoLoad() throws IOException {
        return load(AUTO_STATE);
    }

    public boolean quickSaveFilesExist() {
        return new File(statesPath + QUICK_STATE + ".dat").exists()
                && new File(statesPath + QUICK_STATE + ".meta").exists();
    }
    public void quickSave(GameBoy gb) throws IOException {
        quickSave(new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }
    public void quickSave(State state) throws IOException {
        save(QUICK_STATE, state);
    }
    public State quickLoad() throws IOException {
        return load(QUICK_STATE);
    }

    public boolean regularSaveFilesExist(int slot) {
        return new File(statesPath + REGULAR_STATE + slot + ".dat").exists()
                && new File(statesPath + REGULAR_STATE + slot + ".meta").exists();
    }
}
