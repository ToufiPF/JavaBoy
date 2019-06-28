package ch.epfl.javaboy.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.component.Joypad;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

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
            String[] params = { "Roms/Games/" + listGames.get(0) };
            launch(params);
        }
    }

    private static void displayErrorAndExit(String... msg) {
        for (String s : msg)
            System.err.println(s);
        System.exit(1);
    }

    private Map<KeyCode, Joypad.Key> mapKeys;

    private Stage primaryStage;
    private GameBoy gb;
    private AnimationTimer timer;

    private ImageView view;
    private BorderPane root;
    private Scene scene;
    private MenuBar menuBar;
    
    public Main() {
        mapKeys = null;
        loadKeyMap();
        primaryStage = null;
        gb = null;

        createSceneRootView();
        createMenuBar();
    }

    @Override
    public void start(Stage arg0) throws Exception {
        List<String> args = getParameters().getRaw();

        if (args.size() != 1)
            displayErrorAndExit("Argument invalide.", 
                    "Veuillez spécifier exactement un argument : le nom de la rom à lancer.");
        File romFile = new File(args.get(0));
        if (!romFile.exists())
            displayErrorAndExit("Argument invalide.",
                    "Le fichier spécifié n'existe pas. (" + romFile.getAbsolutePath() + ").");

        gb = new GameBoy(Cartridge.ofFile(romFile));
        timer = createAnimationTimer(System.nanoTime());
        timer.start();

        primaryStage = arg0;
        primaryStage.setMinWidth(view.getFitWidth() + 30);
        primaryStage.setMinHeight(view.getFitHeight() + 80);
        primaryStage.setTitle("JavaBoy - A GameBoy Emulator by Toufi");
        primaryStage.setScene(scene);
        primaryStage.show();
        view.requestFocus();
    }

    private void createSceneRootView() {
        view = new ImageView();
        view.setFitWidth(LcdController.LCD_WIDTH * 3);
        view.setFitHeight(LcdController.LCD_HEIGHT * 3);

        view.setOnKeyPressed(e -> {
            if (mapKeys.containsKey(e.getCode()))
                gb.joypad().keyPressed(mapKeys.get(e.getCode()));
        });
        view.setOnKeyReleased(e -> {
            if (mapKeys.containsKey(e.getCode()))
                gb.joypad().keyReleased(mapKeys.get(e.getCode()));
        });

        root = new BorderPane(view);

        scene =  new Scene(root);
    }

    private AnimationTimer createAnimationTimer(long startTime) {
        return new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = now - startTime;
                long cycles = (long) (elapsed * GameBoy.CYCLES_PER_NANO_SECOND);
                gb.runUntil(cycles);
                view.setImage(ImageConverter.convert(
                        gb.lcdController().currentImage()));
            }
        };
    }

    private void createMenuBar() {
        menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem romLoad = new MenuItem("Load Rom");
        romLoad.setOnAction(e -> {

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
        fileMenu.getItems().addAll(romLoad, save, load, quickSave, quickLoad, quit);

        Menu optionsMenu = new Menu("Options");
        MenuItem controls = new MenuItem("Controls");
        controls.setOnAction(e -> {
            JoypadMapDialog dial = new JoypadMapDialog(mapKeys);
            dial.showAndWait();
            mapKeys = dial.getResult();
            saveKeyMap();
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
    
    private void saveKeyMap() {
        File save = new File("keymap.ini");
        try (Writer writer = new FileWriter(save)) {
            String str = JoypadMapDialog.serializeKeyMap(mapKeys);
            writer.write(str);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadKeyMap() {
        File load = new File("keymap.ini");
        try (Reader reader = new FileReader(load)) {
            StringBuilder b = new StringBuilder();
            
            char[] buff = new char[20];
            int read;
            while ((read = reader.read(buff)) != -1)
                b.append(buff, 0, read);
            
            mapKeys = JoypadMapDialog.deserializeKeyMap(b.toString());
            
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mapKeys = JoypadMapDialog.defaultKeyMap();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            mapKeys = JoypadMapDialog.defaultKeyMap();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } 
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
