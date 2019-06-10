package ch.epfl.javaboy.gui;

import java.io.File;
import java.util.List;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public final class Main extends Application {

    public static void main(String[] args) {
        if (args.length != 0) {
            launch(args);
        } else {
            String[] params = { "Roms/Games/tetris.gb" };
            launch(params);
        }
    }

    private static void displayErrorAndExit(String... msg) {
        for (String s : msg)
            System.err.println(s);
        System.exit(1);
    }

    private Stage primaryStage;
    private GameBoy gb;
    
    private ImageView view;
    private BorderPane root;
    private Scene scene;
    private MenuBar menuBar;
    
    public Main() {
        primaryStage = null;
        gb = null;
        
        view = null;
        scene = null;
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
        
        createScene();
        createMenuBar();
        
        createAnimationTimer(System.nanoTime()).start();
        
        primaryStage = arg0;
        primaryStage.setMinWidth(view.getFitWidth() + 30);
        primaryStage.setMinHeight(view.getFitHeight() + 80);
        primaryStage.setTitle("JavaBoy - A GameBoy Emulator by Toufi");
        primaryStage.setScene(scene);
        primaryStage.show();
        view.requestFocus();
    }
    
    private void createScene() {
        view = new ImageView();
        view.setFitWidth(LcdController.LCD_WIDTH * 3);
        view.setFitHeight(LcdController.LCD_HEIGHT * 3);
        
        view.setOnKeyPressed(e -> {
            
        });
        view.setOnKeyReleased(e -> {
            
        });
        
        root = new BorderPane(view);
        
        scene =  new Scene(root);
    }
    
    private AnimationTimer createAnimationTimer(long startTime) {
        return new AnimationTimer() {
            private static final long CYCLES_PER_SECOND = 1L << 20;
            @Override
            public void handle(long now) {
                double elapsed = (double) (now - startTime) / 1e9;
                long cycles = (long) (elapsed * CYCLES_PER_SECOND);
                gb.runUntil(cycles);
                view.setImage(ImageConverter.convert(
                        gb.lcdController().currentImage()));
            }
        };
    }
    
    private void createMenuBar() {
        menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem quit = new MenuItem("Quit");
        quit.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(quit);
        
        Menu optionsMenu = new Menu("Options");
        MenuItem control = new MenuItem("Control");
        control.setOnAction(e -> {
            
        });
        optionsMenu.getItems().addAll(control);
        
        Menu helpMenu = new Menu("Help");
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> {
            
        });
        helpMenu.getItems().addAll(about);
        
        menuBar.getMenus().addAll(fileMenu, optionsMenu, helpMenu);
        
        root.setTop(menuBar);
    }
}
