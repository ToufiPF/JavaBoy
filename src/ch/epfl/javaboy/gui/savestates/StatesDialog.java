package ch.epfl.javaboy.gui.savestates;

import ch.epfl.javaboy.GameBoy;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.stage.Screen;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * StatesDialog
 * Base class for the classes LoadDialog and SaveDialog
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class StatesDialog {

    public enum Mode {
        SAVING, LOADING
    }

    private static final String ABSOLUTE_SAVE_PATH = new File("").getAbsolutePath() + "/Saves/";
    private static final String AUTO_STATE = "auto";
    private static final String QUICK_STATE = "quick";
    private static final String REGULAR_STATE = "save";

    private static final int REGULAR_SAVE_SLOTS = 10;
    private static final int SPECIAL_SAVE_SLOTS = 2;

    public final static int DIALOG_WIDTH = 2 * StateNode.WIDTH + 70;
    public final static int DIALOG_HEIGHT = 3 * StateNode.HEIGTH;

    private static String statesPathForRom(String romName) {
        return ABSOLUTE_SAVE_PATH + romName.replace('.', '-') + '/';
    }

    private final Dialog<String> dialog;
    private final StateNodePane content;

    private final List<StateNode> specialNodes, regularNodes;
    private final Node newSaveNode;

    private String statesPath;

    /**
     * Creates a new StateDialog
     */
    public StatesDialog() {

        specialNodes = new ArrayList<>(SPECIAL_SAVE_SLOTS);
        regularNodes = new ArrayList<>(REGULAR_SAVE_SLOTS);
        newSaveNode = createNewSaveNode();
        content = new StateNodePane();

        // Dialog()
        dialog = new Dialog<>();
        dialog.setResizable(false);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setResultConverter(buttonType -> {
            if (buttonType.equals(ButtonType.OK) && content.getSelection() != null)
                return content.getSelection().getSaveName();
            return null;
        });
        dialog.getDialogPane().setStyle("-fx-focus-color: transparent;\n" +
                        "-fx-faint-focus-color: transparent;");

        // Position and Size
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        dialog.setX((bounds.getWidth() - DIALOG_WIDTH) / 2);
        dialog.setY((bounds.getHeight() - DIALOG_HEIGHT) / 2);
        dialog.getDialogPane().setPrefSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        dialog.getDialogPane().setMaxSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        dialog.getDialogPane().setMinSize(DIALOG_WIDTH, DIALOG_HEIGHT);

        ScrollPane pane = new ScrollPane();
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pane.setContent(content);
        dialog.getDialogPane().setContent(pane);
    }

    /**
     * Shows the Dialog and returns the result
     * in an Optional<String>
     * @param mode (Mode) the mode of the dialog (saving or loading)
     * @return (Optional<String>) containing a String if the user
     * selected a slot, a empty Optionnal otherwise
     */
    public Optional<String> showAndWait(Mode mode) {
        refreshContent(mode);
        return dialog.showAndWait();
    }

    /**
     * Sets the name of the actual rom,
     * used to generate the save/load paths,
     * and refresh the lists of StateNode
     *
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
     * Generates the State of the GameBoy,
     * ie. the date, screenshot and save state,
     * and then saves it with the given save name
     *
     * @param saveName (String) the NAME (NOT PATH) of the save (ex: "save1")
     * @param gb       (GameBoy) the GameBoy to save
     * @throws IOException if a problem occurs during the creation of
     *                     the State, or during its saving
     */
    public void save(String saveName, GameBoy gb) throws IOException {
        save(saveName, new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }

    /**
     * Saves the given State with the given save name
     *
     * @param saveName (String) the NAME (NOT PATH) of the save (ex: "save1")
     * @param state    (State) state to save
     * @throws IOException if a problem occurs during the saving of the State
     */
    public void save(String saveName, State state) throws IOException {
        State.saveState(statesPath + saveName, state);
        refreshStateNodes();
        System.out.println("Saved : " + saveName + ", " + state.getDateAndTime());
    }

    /**
     * Loads the State corresponding to the given save name
     *
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
     *
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
     *
     * @param gb (GameBoy) gameboy to save
     * @throws IOException if a problem occurs during the creation of
     *                     the State, or during its saving
     */
    public void autoSave(GameBoy gb) throws IOException {
        autoSave(new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }

    /**
     * Autosaves the given State
     *
     * @param state (State) state to save
     * @throws IOException if a problem occurs during the saving of the State
     */
    public void autoSave(State state) throws IOException {
        save(AUTO_STATE, state);
    }

    /**
     * Loads the autosave State
     *
     * @return (State) the loaded State
     * @throws IOException if a problem occurs during the loading of the State
     */
    public State autoLoad() throws IOException {
        return load(AUTO_STATE);
    }

    /**
     * Returns true if both files quick.meta and quick.dat exist
     *
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
     *
     * @param gb (GameBoy) gameboy to save
     * @throws IOException if a problem occurs during the creation of
     *                     the State, or during its saving
     */
    public void quickSave(GameBoy gb) throws IOException {
        quickSave(new State(LocalDateTime.now(), gb.lcdController().currentImage(), gb.saveState()));
    }

    /**
     * Quicksaves the given State
     *
     * @param state (State) state to save
     * @throws IOException if a problem occurs during the saving of the State
     */
    public void quickSave(State state) throws IOException {
        save(QUICK_STATE, state);
    }

    /**
     * Loads the quicksave State
     *
     * @return (State) the loaded State
     * @throws IOException if a problem occurs during the loading of the State
     */
    public State quickLoad() throws IOException {
        return load(QUICK_STATE);
    }

    /**
     * Returns true if both files saveN.meta and quickN.dat exist,
     * with N = slot
     *
     * @param slot (int) the slot of the regular save to test
     * @return (boolean) true if quicksave files exist
     */
    public boolean regularSaveFilesExist(int slot) {
        return new File(statesPath + REGULAR_STATE + slot + ".dat").exists()
                && new File(statesPath + REGULAR_STATE + slot + ".meta").exists();
    }

    private Node createNewSaveNode() {
        HBox lay = new HBox();
        lay.setMinSize(StateNode.WIDTH, StateNode.HEIGTH);
        lay.setMaxSize(StateNode.WIDTH, StateNode.HEIGTH);
        lay.setAlignment(Pos.CENTER);
        Button txt = new Button("Add New Save");
        txt.setStyle("-fx-font: 24 arial;");
        txt.setOnMouseClicked(e -> {
            System.out.println("New Save request n°" + regularNodes.size());
            dialog.setResult(REGULAR_STATE + regularNodes.size());
            dialog.close();
        });
        lay.getChildren().add(txt);
        return lay;
    }

    private void refreshStateNodes() {
        // Auto & QuickSaves
        try {
            specialNodes.clear();
            if (autoSaveFilesExist())
                specialNodes.add(new StateNode("AutoSave", AUTO_STATE, State.loadMetadata(statesPath + AUTO_STATE)));
            if (quickSaveFilesExist())
                specialNodes.add(new StateNode("QuickSave", QUICK_STATE, State.loadMetadata(statesPath + QUICK_STATE)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Regular Saves
        try {
            regularNodes.clear();
            for (int i = 0; i < REGULAR_SAVE_SLOTS; ++i)
                if (regularSaveFilesExist(i))
                    regularNodes.add(new StateNode("Save " + i, REGULAR_STATE + i, State.loadMetadata(statesPath + REGULAR_STATE + i)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void refreshContent(Mode mode) {
        content.getChildren().clear();
        if (mode.equals(Mode.SAVING)) {
            dialog.setTitle("Save State");

            for (StateNode regularNode : regularNodes)
                content.getChildren().add(regularNode);
            if (regularNodes.size() < REGULAR_SAVE_SLOTS)
                content.getChildren().add(newSaveNode);

        } else {
            dialog.setTitle("Load State");

            for (StateNode n : specialNodes)
                content.getChildren().add(n);
            for (StateNode regularNode : regularNodes)
                content.getChildren().add(regularNode);
        }
    }
}
