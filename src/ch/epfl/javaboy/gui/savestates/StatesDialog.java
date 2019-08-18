package ch.epfl.javaboy.gui.savestates;

import ch.epfl.javaboy.GameBoy;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Screen;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * StatesDialog
 * Base class for the classes LoadDialog and SaveDialog
 */
@SuppressWarnings("WeakerAccess")
abstract class StatesDialog extends Dialog<String> {
    private static final String ABSOLUTE_SAVE_PATH = new File("").getAbsolutePath() + "/Saves/";
    static final String AUTO_STATE = "auto";
    static final String QUICK_STATE = "quick";
    static final String REGULAR_STATE = "save";

    static final int REGULAR_SAVE_SLOTS = 10;
    static final int SPECIAL_SAVE_SLOTS = 2;

    private final static int DIALOG_WIDTH = 2 * StateNode.WIDTH + 70;
    private final static int DIALOG_HEIGHT = 3 * StateNode.HEIGTH;

    private static String statesPathForRom(String romName) {
        return ABSOLUTE_SAVE_PATH + romName.replace('.', '-') + '/';
    }

    private String statesPath;

    final FlowPane content;
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

        // Position and Size
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        setX((bounds.getWidth() - DIALOG_WIDTH) / 2);
        setY((bounds.getHeight() - DIALOG_HEIGHT) / 2);
        getDialogPane().setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        getDialogPane().setMaxSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        getDialogPane().setMinSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        // ScrollPane
        content = new FlowPane();
        content.setPadding(new Insets(5));
        content.setHgap(30);
        content.setVgap(25);
        content.setPrefWrapLength(DIALOG_WIDTH);
        content.setOrientation(Orientation.HORIZONTAL);

        ScrollPane pane = new ScrollPane();
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pane.setContent(content);
        getDialogPane().setContent(pane);
    }

    /**
     * Sets the name of the actual rom,
     * used to generate the save/load paths,
     * and refresh the lists of StateNode
     * @param romName (String) the name of the current rom
     */
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

    /**
     * Refresh the lists of StateNode
     */
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

    /**
     * Generates the State of the GameBoy,
     * ie. the date, screenshot and save state,
     * and then saves it with the given save name
     * @param saveName (String) the NAME (NOT PATH) of the save (ex: "save1")
     * @param gb (GameBoy) the GameBoy to save
     * @throws IOException if a problem occurs during the creation of
     * the State, or during its saving
     */
    public void save(String saveName, GameBoy gb) throws IOException {
        save(saveName, new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }

    /**
     * Saves the given State with the given save name
     * @param saveName (String) the NAME (NOT PATH) of the save (ex: "save1")
     * @param state (State) state to save
     * @throws IOException if a problem occurs during the saving of the State
     */
    public void save(String saveName, State state) throws IOException {
        State.saveState(statesPath + saveName, state);
        System.out.println("Saved : " + saveName + ", " + state.getDateAndTime());
    }

    /**
     * Loads the State corresponding to the given save name
     * @param saveName (String) the NAME (NOT PATH) of the save (ex: "save1")
     * @return (State) the loaded State
     * @throws IOException if a problem occurs during the loading of the State
     */
    public State load(String saveName) throws IOException {
        State state = State.loadState(statesPath + saveName);
        System.out.println("Loaded : " + saveName + ", " + state.getDateAndTime());
        return state;
    }

    /**
     * Returns true if both files auto.meta and auto.dat exist
     * @return (boolean) true if autosave files exist
     */
    public boolean autoSaveFilesExist() {
        return new File(statesPath + AUTO_STATE + ".dat").exists()
                && new File(statesPath + AUTO_STATE + ".meta").exists();
    }
    /**
     * Generates the State of the GameBoy,
     * ie. the date, screenshot and save state,
     * and then autosaves it
     * @param gb (GameBoy) gameboy to save
     * @throws IOException if a problem occurs during the creation of
     * the State, or during its saving
     */
    public void autoSave(GameBoy gb) throws IOException {
        autoSave(new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }
    /**
     * Autosaves the given State
     * @param state (State) state to save
     * @throws IOException if a problem occurs during the saving of the State
     */
    public void autoSave(State state) throws IOException {
        save(AUTO_STATE, state);
    }
    /**
     * Loads the autosave State
     * @return (State) the loaded State
     * @throws IOException if a problem occurs during the loading of the State
     */
    public State autoLoad() throws IOException {
        return load(AUTO_STATE);
    }

    /**
     * Returns true if both files quick.meta and quick.dat exist
     * @return (boolean) true if quicksave files exist
     */
    public boolean quickSaveFilesExist() {
        return new File(statesPath + QUICK_STATE + ".dat").exists()
                && new File(statesPath + QUICK_STATE + ".meta").exists();
    }
    /**
     * Generates the State of the GameBoy,
     * ie. the date, screenshot and save state,
     * and then quicksaves it
     * @param gb (GameBoy) gameboy to save
     * @throws IOException if a problem occurs during the creation of
     * the State, or during its saving
     */
    public void quickSave(GameBoy gb) throws IOException {
        quickSave(new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }
    /**
     * Quicksaves the given State
     * @param state (State) state to save
     * @throws IOException if a problem occurs during the saving of the State
     */
    public void quickSave(State state) throws IOException {
        save(QUICK_STATE, state);
    }
    /**
     * Loads the quicksave State
     * @return (State) the loaded State
     * @throws IOException if a problem occurs during the loading of the State
     */
    public State quickLoad() throws IOException {
        return load(QUICK_STATE);
    }

    /**
     * Returns true if both files saveN.meta and quickN.dat exist,
     * with N = slot
     * @param slot (int) the slot of the regular save to test
     * @return (boolean) true if quicksave files exist
     */
    public boolean regularSaveFilesExist(int slot) {
        return new File(statesPath + REGULAR_STATE + slot + ".dat").exists()
                && new File(statesPath + REGULAR_STATE + slot + ".meta").exists();
    }
}
