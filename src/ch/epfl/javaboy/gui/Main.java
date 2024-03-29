package ch.epfl.javaboy.gui;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.component.Joypad;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.lcd.ImageConverter;
import ch.epfl.javaboy.component.lcd.LcdController;
import ch.epfl.javaboy.gui.options.General;
import ch.epfl.javaboy.gui.options.Sound;
import ch.epfl.javaboy.gui.savestates.State;
import ch.epfl.javaboy.gui.savestates.StatesDialog;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.Mnemonic;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static ch.epfl.javaboy.gui.options.Option.*;

/**
 * The Main class of the JavaBoy emulator.
 * Creates the GameBoy and the gui, 
 * saves and loads save states and keyMaps...
 * @author Toufi
 */
public final class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @SuppressWarnings("unused")
    private static void displayErrorAndExit(String... msg) {
        for (String s : msg)
            System.err.println(s);
        System.exit(1);
    }

    private static String getRomNameFromPath(File romPath) {
        String name = romPath.getName();
        return name.substring(0, name.lastIndexOf('.'));
    }

    private ImageView view;
    private BorderPane root;
    private Scene scene;
    private Stage primaryStage;

    @SuppressWarnings("FieldCanBeLocal")
    private Button qckLoad, qckSave;
    @SuppressWarnings("FieldCanBeLocal")
    private Button regLoad, regSave;

    private final OptionsDialog optionsDial;
    private final StatesDialog statesDial;

    private File lastPlayedRom;

    private GameBoy gb;
    private AnimationTimer timer;
    private final AtomicBoolean paused;

    // Options
    Map<KeyCode, Joypad.Key> keysMap;
    boolean autoLoadWhenLaunchingRom;


    public Main() {
        primaryStage = null;
        lastPlayedRom = null;

        keysMap = null;
        autoLoadWhenLaunchingRom = false;

        gb = null;
        timer = null;
        paused = new AtomicBoolean(false);

        loadOptions();
        loadKeyMap();

        optionsDial = new OptionsDialog(this);
        statesDial = new StatesDialog();
        statesDial.setRomName(getRomNameFromPath(lastPlayedRom));

        createSceneRootView();
        createToolBar();
    }

    @Override
    public void start(Stage arg0) {
        primaryStage = arg0;
        primaryStage.setMinWidth(view.getFitWidth() + 30);
        primaryStage.setMinHeight(view.getFitHeight() + 80);
        primaryStage.setTitle("JavaBoy - A GameBoy Emulator by Toufi");
        primaryStage.setScene(scene);
        primaryStage.show();
        view.requestFocus();

        primaryStage.setOnCloseRequest(e -> {
            if (gb != null) {
                try {
                    statesDial.autoSave(gb);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        launchRom(lastPlayedRom);
    }

    private void launchRom(File romPath) {
        if (!romPath.exists()) {
            System.err.println("Error : Rom not found.\n" + "Path : " + romPath);
            return;
        }

        try {
            //Stopping last GameBoy if needed
            if (gb != null) {
                timer.stop();
                gb.soundController().stopAudio();
            }
            gb = new GameBoy(Cartridge.ofFile(romPath));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        timer = createAnimationTimer(gb, System.nanoTime());
        timer.start();

        statesDial.setRomName(getRomNameFromPath(romPath));

        qckLoad.setDisable(!statesDial.quickSaveFilesExist());
        if (autoLoadWhenLaunchingRom && statesDial.autoSaveFilesExist()) {
            try {
                State auto = statesDial.autoLoad();
                gb.loadState(auto.getGbState());
                actualizeAnimationTimer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private AnimationTimer createAnimationTimer(GameBoy gameBoy, long startTime) {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (paused.get())
                    return;
                long elapsed = now - startTime;
                long cycles = (long) (elapsed * GameBoy.CYCLES_PER_NANO_SECOND);
                gameBoy.runUntil(cycles);
                view.setImage(ImageConverter.convert(
                        gameBoy.lcdController().currentImage()));
            }
        };
    }
    private void actualizeAnimationTimer() {
        long elapsedNanoSec = (long) (gb.cycles() / GameBoy.CYCLES_PER_NANO_SECOND);
        timer.stop();
        timer = createAnimationTimer(gb, System.nanoTime() - elapsedNanoSec);
        timer.start();
    }

    private void createSceneRootView() {
        view = new ImageView();
        view.setFitWidth(LcdController.LCD_WIDTH * 3);
        view.setFitHeight(LcdController.LCD_HEIGHT * 3);

        view.setOnKeyPressed(e -> {
            if (keysMap.containsKey(e.getCode()))
                gb.joypad().keyPressed(keysMap.get(e.getCode()));
            e.consume();
        });
        view.setOnKeyReleased(e -> {
            if (keysMap.containsKey(e.getCode()))
                gb.joypad().keyReleased(keysMap.get(e.getCode()));
            e.consume();
        });

        root = new BorderPane(view);
        scene =  new Scene(root);
    }
    private void createToolBar() {
        ToolBar toolBar = new ToolBar();
        // File Menu
        {
            Button romPath = new Button("Change Rom");
            romPath.setOnAction(e -> {
                FileChooser chooser = new FileChooser();
                chooser.setInitialDirectory(lastPlayedRom.getParentFile());
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("GameBoy Roms", "*.gb"));
                File rom = chooser.showOpenDialog(primaryStage);
                if (rom != null && rom.exists()) {
                    lastPlayedRom = rom;
                    saveOptions();
                    launchRom(rom);
                }
                view.requestFocus();
            });

            qckLoad = new Button("QuickLoad");
            scene.addMnemonic(new Mnemonic(qckLoad, new KeyCodeCombination(KeyCode.F5)));
            qckLoad.setOnAction(e -> {
                try {
                    State state = statesDial.quickLoad();
                    gb.loadState(state.getGbState());
                    actualizeAnimationTimer();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                view.requestFocus();
            });
            qckSave = new Button("QuickSave");
            scene.addMnemonic(new Mnemonic(qckSave, new KeyCodeCombination(KeyCode.F6)));
            qckSave.setOnAction(e -> {
                try {
                    statesDial.quickSave(gb);
                    qckLoad.setDisable(false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                view.requestFocus();
            });

            regLoad = new Button("Load");
            scene.addMnemonic(new Mnemonic(regLoad, new KeyCodeCombination(KeyCode.F7)));
            regLoad.setOnAction(e -> {
                setPaused(true);
                Optional<String> result = statesDial.showAndWait(StatesDialog.Mode.LOADING);
                if (result.isPresent()) {
                    String loadName = result.get();
                    try {
                        State st = statesDial.load(loadName);
                        gb.loadState(st.getGbState());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                setPaused(false);
                view.requestFocus();
            });
            regSave = new Button("Save");
            scene.addMnemonic(new Mnemonic(regSave, new KeyCodeCombination(KeyCode.F8)));
            regSave.setOnAction(e -> {
                setPaused(true);
                Optional<String> result = statesDial.showAndWait(StatesDialog.Mode.SAVING);
                if (result.isPresent()) {
                    String saveName = result.get();
                    try {
                        statesDial.save(saveName, gb);
                        qckLoad.setDisable(!statesDial.quickSaveFilesExist());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
                setPaused(false);
                view.requestFocus();
            });

            toolBar.getItems().add(romPath);
            toolBar.getItems().add(new Separator(Orientation.VERTICAL));
            toolBar.getItems().addAll(qckLoad, qckSave, regLoad, regSave);
            toolBar.getItems().add(new Separator(Orientation.VERTICAL));
        }

        // Options Menu
        {
            Button options = new Button("Options");
            options.setOnAction(e -> {
                setPaused(true);
                optionsDial.showAndWait();
                saveOptions();
                setPaused(false);
                view.requestFocus();
            });

            toolBar.getItems().add(options);
        }

        root.setTop(toolBar);
    }

    private void setPaused(boolean pause) {
        paused.set(pause);
        if (pause) {
            gb.soundController().stopAudio();
        } else {
            gb.soundController().startAudio();
            actualizeAnimationTimer();
        }
    }

    private void saveOptions() {
        File options = new File(OPTIONS_FILE_PATH + OPTIONS_FILE_NAME);
        try (Writer writer = new FileWriter(options)) {
            // General
            writer.write(General.TAG + '\n');
            writer.write(General.LAST_PLAYED_ROM.tag() + lastPlayedRom + '\n');
            writer.write(General.AUTO_LOAD.tag() + autoLoadWhenLaunchingRom + '\n');

            //Sound Options
            writer.write(Sound.TAG + '\n');

        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    private void loadOptions() {
        setOptionsToDefault();

        File options = new File(OPTIONS_FILE_PATH + OPTIONS_FILE_NAME);
        try (BufferedReader reader = new BufferedReader(new FileReader(options))) {
            String line, lastTag = null;
            LinkedList<String> content = new LinkedList<>();
            while ((line = reader.readLine()) != null) {
                if (line.trim().equals(General.TAG)) {
                    if (lastTag != null)
                        loadOption(lastTag, content);
                    lastTag = General.TAG;
                    content.clear();
                } else if (!line.startsWith("#")) {
                    content.add(line);
                }
            }
            if (lastTag != null)
                loadOption(lastTag, content);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            setOptionsToDefault();
        }
        saveOptions();
    }
    private void loadOption(String optionTag, List<String> content) {
        switch (optionTag) {
            case General.TAG:
                for (String line : content) {
                    if (line.startsWith(General.LAST_PLAYED_ROM.tag()))
                        lastPlayedRom =
                                new File(line.substring(General.LAST_PLAYED_ROM.tag().length()).trim());
                    else if (line.startsWith(General.AUTO_LOAD.tag()))
                        autoLoadWhenLaunchingRom =
                                Boolean.parseBoolean(line.substring(General.AUTO_LOAD.tag().length()).trim());
                }
                break;
            case Sound.TAG:
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
    private void setOptionsToDefault() {
        // General
        lastPlayedRom = new File(General.LAST_PLAYED_ROM.defaultString());
        autoLoadWhenLaunchingRom = Boolean.parseBoolean(General.AUTO_LOAD.defaultString());
    }

    private void saveKeyMap() {
        File file = new File(KEYMAP_FILE_PATH + KEYMAP_FILE_NAME);
        try (Writer writer = new FileWriter(file)) {
            String str = KeyboardToGBJoypadNode.serializeKeyMap(keysMap);
            writer.write(str);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    private void loadKeyMap() {
        File file = new File(KEYMAP_FILE_PATH + KEYMAP_FILE_NAME);
        try (FileReader reader = new FileReader(file)) {
            StringBuilder b = new StringBuilder();
            char[] buffer = new char[50];
            int read;
            while ((read = reader.read(buffer)) != -1)
                b.append(buffer, 0, read);
            keysMap = KeyboardToGBJoypadNode.deserializeKeyMap(b.toString());
        } catch (IOException e) {
            setKeyMapToDefault();
            saveKeyMap();
            e.printStackTrace();
        }
    }
    private void setKeyMapToDefault() {
        keysMap = KeyboardToGBJoypadNode.defaultKeyMap();
    }
}
