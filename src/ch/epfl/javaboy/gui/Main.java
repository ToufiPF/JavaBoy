package ch.epfl.javaboy.gui;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ch.epfl.javaboy.GameBoy;
import ch.epfl.javaboy.component.cartridge.Cartridge;
import ch.epfl.javaboy.component.lcd.LcdController;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
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
    
    private Stage primaryStage;
    private GameBoy gb;
    
    private ImageView view;
    private Scene scene;
    
    public Main() {
        primaryStage = null;
        gb = null;
    }
    
    @Override
    public void start(Stage arg0) throws IOException {
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
        
        AnimationTimer timer = createAnimationTimer(System.nanoTime());
        timer.start();
        
        primaryStage = arg0;
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
        
        BorderPane pane = new BorderPane(view);
        
        scene =  new Scene(pane);
    }
    
    private AnimationTimer createAnimationTimer(long startTime) {
        return new AnimationTimer() {
            private final static long CYCLES_PER_SECOND = 1L << 20;
            private final long start = startTime;
            @Override
            public void handle(long now) {
                double elapsed = (double) (now - start) / 1e9;
                long cycles = (long) (elapsed * CYCLES_PER_SECOND);
                gb.runUntil(cycles);
                view.setImage(ImageConverter.convert(
                        gb.lcdController().currentImage()));
            }
        };
    }
    
    private static void displayErrorAndExit(String... msg) {
        for (String s : msg)
            System.err.println(s);
        System.exit(1);
    }
}
