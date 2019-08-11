package ch.epfl.javaboy.gui;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.component.Joypad;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import static ch.epfl.javaboy.gui.Options.*;

/**
 * The Main class of the JavaBoy emulator.
 * Creates the GameBoy and the gui, 
 * saves and loads save states and keyMaps...
 * @author Toufi
 */
public final class Main extends Application {

    public static void main(String[] args) {
        if (args.length != 0) {
            launch(args);
        } else {
            List<String> listGames = new ArrayList<>();
            listGames.add("tetris.gb");
            listGames.add("2048.gb");
            listGames.add("flappyboy.gb");
            listGames.add("snake.gb");
            String[] params = { "Roms/" + listGames.get(0) };
            launch(params);
        }
    }

    private static void displayErrorAndExit(String... msg) {
        for (String s : msg)
            System.err.println(s);
        System.exit(1);
    }

    private Map<KeyCode, Joypad.Key> keysMap;

    private Stage primaryStage;
    private GameBoy gb;
    private AnimationTimer timer;

    private ImageView view;
    private BorderPane root;
    private Scene scene;
    private MenuBar menuBar;

    private String romsPath;
    private final ObservableList<String> romsList;
    private String lastPlayedRom;

    private String savesPath;
    
    public Main() {
        keysMap = null;
        primaryStage = null;
        gb = null;
        romsList = FXCollections.observableList(new LinkedList<>());

        loadAllOptions();

        createSceneRootView();
        createMenuBar();

        reloadRomsInActivePath();
    }

    @Override
    public void start(Stage arg0) {
        if (!lastPlayedRom.isEmpty())
            launchGame(lastPlayedRom);

        primaryStage = arg0;
        primaryStage.setMinWidth(view.getFitWidth() + 30);
        primaryStage.setMinHeight(view.getFitHeight() + 80);
        primaryStage.setTitle("JavaBoy - A GameBoy Emulator by Toufi");
        primaryStage.setScene(scene);
        primaryStage.show();
        view.requestFocus();
    }

    private void launchGame(String path) {
        File romFile = new File(path);
        if (!romFile.exists())
            return;

        if (gb != null) {
            gb.soundController().stopAudio();
        }
        try {
            gb = new GameBoy(Cartridge.ofFile(romFile));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        timer = createAnimationTimer(gb, System.nanoTime());
        timer.start();
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
        menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        Menu romLoad = new Menu("Choose Rom");
        romsList.addListener((ListChangeListener<? super String>) (change) -> {
            romLoad.getItems().clear();
            for (String str : change.getList()) {
                MenuItem rom = new MenuItem(str);
                rom.setOnAction(e -> {
                    launchGame(romsPath + rom.getText());
                    lastPlayedRom = rom.getText();
                    saveAllOptions();
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
                romsPath += '\\';
            reloadRomsInActivePath();
            saveAllOptions();
        });

        MenuItem save = new MenuItem("Save");
        save.setOnAction(e -> {

        });
        MenuItem load = new MenuItem("Load");
        save.setOnAction(e -> {

        });
        MenuItem quickSave = new MenuItem("QuickSave");
        quickSave.setOnAction(e -> {
            
        });
        MenuItem quickLoad = new MenuItem("QuickLoad");
        quickLoad.setOnAction(e -> {
            
        });
        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(romLoad, romPath, new SeparatorMenuItem(),
                save, load, quickSave, quickLoad, new SeparatorMenuItem(), quit);

        Menu optionsMenu = new Menu("Options");
        MenuItem controls = new MenuItem("Controls");
        controls.setOnAction(e -> {
            JoypadMapDialog dial = new JoypadMapDialog(keysMap);
            dial.showAndWait();
            keysMap = dial.getResult();
            saveAllOptions();
        });
        optionsMenu.getItems().addAll(controls);

        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> {
            AboutDialog dial = new AboutDialog();
            dial.showAndWait();
        });
        helpMenu.getItems().addAll(about);

        menuBar.getMenus().addAll(fileMenu, optionsMenu, helpMenu);

        root.setTop(menuBar);
    }

    private void saveAllOptions() {
        File options = new File(OPTIONS_FILE_PATH + OPTIONS_FILE_NAME);
        try (Writer writer = new FileWriter(options)) {
            // General
            writer.write(GENERAL_TAG + '\n');
            writer.write(ROMS_PATH_TAG + romsPath + '\n');
            writer.write(LAST_PLAYED_ROM_TAG + lastPlayedRom + '\n');
            writer.write(SAVES_PATH_TAG + savesPath + '\n');
            // Key Map
            writer.write(KEY_MAP_TAG + '\n');
            String str = JoypadMapDialog.serializeKeyMap(keysMap);
            writer.write(str);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
    private void loadAllOptions() {
        allOptionsToDefault();
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
                } else if (line.trim().equals(KEY_MAP_TAG)) {
                    if (lastTag != null)
                        loadOption(lastTag, content);
                    lastTag = KEY_MAP_TAG;
                    content.clear();
                } else if (line.startsWith("#")) {
                    continue;
                } else {
                    content.add(line);
                }
            }
            if (lastTag != null)
                loadOption(lastTag, content);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            allOptionsToDefault();
        }catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        saveAllOptions();
    }
    private void loadOption(String optionTag, List<String> content) {
        switch (optionTag) {
            case GENERAL_TAG:
                for (String line : content) {
                    if (line.startsWith(ROMS_PATH_TAG))
                        romsPath = line.substring(ROMS_PATH_TAG.length()).trim();
                    else if (line.startsWith(SAVES_PATH_TAG))
                        savesPath = line.substring(SAVES_PATH_TAG.length()).trim();
                    else if (line.startsWith(LAST_PLAYED_ROM_TAG))
                        lastPlayedRom = line.substring(LAST_PLAYED_ROM_TAG.length()).trim();
                }
                break;
            case KEY_MAP_TAG:
                StringBuilder b = new StringBuilder();
                for (String s : content)
                    b.append(s).append('\n');
                keysMap = JoypadMapDialog.deserializeKeyMap(b.toString());
                break;
            default:
                throw new IllegalArgumentException();
        }
    }
    private void allOptionsToDefault() {
        // General
        romsPath = DEFAULT_ROMS_PATH;
        lastPlayedRom = "";
        savesPath = DEFAULT_SAVE_PATH;
        // Key Map
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
    
    private boolean saveData(byte[] data, File dest) {
        try (OutputStream os = new FileOutputStream(dest)) {
            os.write(data);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    private byte[] loadData(File source) {
        try (InputStream is = new FileInputStream(source)) {
            byte[] data = new byte[8192];
            int read = is.read(data);
            if (read != data.length)
                return null;
            return data;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
