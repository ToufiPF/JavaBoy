package ch.epfl.javaboy.gui;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.component.Joypad;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.lcd.ImageConverter;
import ch.epfl.javaboy.component.lcd.LcdController;
import ch.epfl.javaboy.gui.savestates.State;
import ch.epfl.javaboy.gui.savestates.StatesDialog;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

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

import static ch.epfl.javaboy.gui.Options.*;

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

    private ImageView view;
    private BorderPane root;
    private Scene scene;
    private Stage primaryStage;

    private MenuItem quickLoad;

    private final StatesDialog statesDial;

    private String romsPath;
    private final ObservableList<String> romsList;
    private String lastPlayedRom;

    private Map<KeyCode, Joypad.Key> keysMap;
    private boolean autoLoadWhenLaunchingRom;

    private GameBoy gb;
    private AnimationTimer timer;

    public Main() {
        primaryStage = null;

        romsPath = null;
        romsList = FXCollections.observableList(new LinkedList<>());
        lastPlayedRom = null;

        keysMap = null;
        autoLoadWhenLaunchingRom = false;

        gb = null;
        timer = null;

        loadOptions();
        loadKeyMap();
        statesDial = new StatesDialog();
        statesDial.setRomName(lastPlayedRom);

        createSceneRootView();
        createMenuBar();

        reloadRomsInActivePath();
    }

    @Override
    public void start(Stage arg0) {
        if (!lastPlayedRom.isEmpty()) {
            launchRom(lastPlayedRom);
        }

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
    }

    private void launchRom(String name) {
        File romFile = new File(romsPath + name);
        if (!romFile.exists()) {
            System.err.println("Last rom used not found.\n" + "Path : " + romsPath + name);
            return;
        }

        //Stopping last GameBoy if needed
        if (gb != null) {
            timer.stop();
            gb.soundController().stopAudio();
        }
        try {
            gb = new GameBoy(Cartridge.ofFile(romFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        timer = createAnimationTimer(gb, System.nanoTime());
        timer.start();
        statesDial.setRomName(name);

        quickLoad.setDisable(!statesDial.quickSaveFilesExist());
        if (autoLoadWhenLaunchingRom && statesDial.autoSaveFilesExist()) {
            try {
                State auto = statesDial.autoLoad();
                gb.loadState(auto.getGbState(), auto.getScreenshot());
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
        });
        view.setOnKeyReleased(e -> {
            if (keysMap.containsKey(e.getCode()))
                gb.joypad().keyReleased(keysMap.get(e.getCode()));
        });

        root = new BorderPane(view);

        scene =  new Scene(root);
    }
    private void createMenuBar() {
        MenuBar menuBar = new MenuBar();
        // File Menu
        {
            Menu fileMenu = new Menu("File");
            Menu romLoad = new Menu("Choose Rom");
            romsList.addListener((ListChangeListener<? super String>) (change) -> {
                romLoad.getItems().clear();
                for (String str : change.getList()) {
                    MenuItem rom = new MenuItem(str);
                    rom.setOnAction(e -> {
                        lastPlayedRom = rom.getText();
                        launchRom(rom.getText());
                        saveOptions();
                    });
                    romLoad.getItems().add(rom);
                }
            });
            MenuItem romPath = new MenuItem("Roms Folder...");
            romPath.setOnAction(e -> {
                DirectoryChooser dirChos = new DirectoryChooser();
                dirChos.setInitialDirectory(new File(romsPath));
                File romsFolder = dirChos.showDialog(null);
                if (romsFolder != null && romsFolder.exists())
                    romsPath = romsFolder.getPath();
                if (!(romsPath.endsWith("/") || romsPath.endsWith("\\")))
                    romsPath += '/';
                reloadRomsInActivePath();
                saveOptions();
            });

            MenuItem load = new MenuItem("Load");
            load.setAccelerator(new KeyCodeCombination(KeyCode.F7));
            load.setOnAction(e -> {
                Optional<String> result = statesDial.showAndWait(StatesDialog.Mode.LOADING);
                if (result.isPresent()) {
                    String loadName = result.get();
                    try {
                        State st = statesDial.load(loadName);
                        gb.loadState(st.getGbState(), st.getScreenshot());
                        actualizeAnimationTimer();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            MenuItem save = new MenuItem("Save");
            save.setAccelerator(new KeyCodeCombination(KeyCode.F8));
            save.setOnAction(e -> {
                Optional<String> result = statesDial.showAndWait(StatesDialog.Mode.SAVING);
                if (result.isPresent()) {
                    String saveName = result.get();
                    try {
                        statesDial.save(saveName, gb);
                        quickLoad.setDisable(!statesDial.quickSaveFilesExist());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            quickLoad = new MenuItem("QuickLoad");
            quickLoad.setAccelerator(new KeyCodeCombination(KeyCode.F5));
            quickLoad.setOnAction(e -> {
                try {
                    State state = statesDial.quickLoad();
                    gb.loadState(state.getGbState(), state.getScreenshot());
                    actualizeAnimationTimer();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            MenuItem quickSave = new MenuItem("QuickSave");
            quickSave.setAccelerator(new KeyCodeCombination(KeyCode.F6));
            quickSave.setOnAction(e -> {
                try {
                    statesDial.quickSave(gb);
                    quickLoad.setDisable(false);
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });

            MenuItem quit = new MenuItem("Quit");
            quit.setOnAction(e -> primaryStage.fireEvent(
                    new WindowEvent(primaryStage, WindowEvent.WINDOW_CLOSE_REQUEST)));

            fileMenu.getItems().addAll(romLoad, romPath, new SeparatorMenuItem(),
                    save, load, quickSave, quickLoad, new SeparatorMenuItem(), quit);

            menuBar.getMenus().add(fileMenu);
        }

        // Options Menu
        {
            Menu optionsMenu = new Menu("Options");
            MenuItem controls = new MenuItem("Controls");
            controls.setOnAction(e -> {
                JoypadMapDialog dial = new JoypadMapDialog(keysMap);
                dial.showAndWait();
                keysMap = dial.getResult();
                saveKeyMap();
            });
            CheckMenuItem autoLoad = new CheckMenuItem("Auto Load when starting Rom");
            autoLoad.setSelected(autoLoadWhenLaunchingRom);
            autoLoad.selectedProperty().addListener((obs, oldV, newV) -> {
                autoLoadWhenLaunchingRom = newV;
                saveOptions();
            });

            optionsMenu.getItems().addAll(controls, autoLoad);

            menuBar.getMenus().add(optionsMenu);
        }

        // Help Menu
        {
            Menu helpMenu = new Menu("Help");
            MenuItem about = new MenuItem("About");
            about.setOnAction(e -> {
                AboutDialog dial = new AboutDialog();
                dial.showAndWait();
            });
            helpMenu.getItems().addAll(about);

            menuBar.getMenus().add(helpMenu);
        }

        root.setTop(menuBar);
    }

    private void saveOptions() {
        File options = new File(OPTIONS_FILE_PATH + OPTIONS_FILE_NAME);
        try (Writer writer = new FileWriter(options)) {
            // General
            writer.write(GENERAL_TAG + '\n');
            writer.write(ROMS_PATH_TAG + romsPath + '\n');
            writer.write(LAST_PLAYED_ROM_TAG + lastPlayedRom + '\n');
            writer.write(AUTO_LOAD_TAG + autoLoadWhenLaunchingRom + '\n');

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
                if (line.trim().equals(GENERAL_TAG)) {
                    if (lastTag != null)
                        loadOption(lastTag, content);
                    lastTag = GENERAL_TAG;
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
            case GENERAL_TAG:
                for (String line : content) {
                    if (line.startsWith(ROMS_PATH_TAG))
                        romsPath = line.substring(ROMS_PATH_TAG.length()).trim();
                    else if (line.startsWith(LAST_PLAYED_ROM_TAG))
                        lastPlayedRom = line.substring(LAST_PLAYED_ROM_TAG.length()).trim();
                    else if (line.startsWith(AUTO_LOAD_TAG))
                        autoLoadWhenLaunchingRom = Boolean.parseBoolean(line.substring(AUTO_LOAD_TAG.length()).trim());
                }
                break;
            case SOUND_TAG:
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
    private void setOptionsToDefault() {
        // General
        romsPath = DEFAULT_ROMS_PATH;
        lastPlayedRom = "";
        autoLoadWhenLaunchingRom = false;
    }

    private void saveKeyMap() {
        File file = new File(KEYMAP_FILE_PATH + KEYMAP_FILE_NAME);
        try (Writer writer = new FileWriter(file)) {
            String str = JoypadMapDialog.serializeKeyMap(keysMap);
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
            keysMap = JoypadMapDialog.deserializeKeyMap(b.toString());
        } catch (IOException e) {
            setKeyMapToDefault();
            saveKeyMap();
            e.printStackTrace();
        }
    }
    private void setKeyMapToDefault() {
        keysMap = JoypadMapDialog.defaultKeyMap();
    }

    private void reloadRomsInActivePath() {
        romsList.clear();
        if (romsPath == null)
            return;
        File romsFolder = new File(romsPath);
        if (!romsFolder.exists())
            return;
        String[] roms = romsFolder.list((dir, name) -> name.toLowerCase().endsWith(".gb"));
        romsList.addAll(roms);
    }
}
