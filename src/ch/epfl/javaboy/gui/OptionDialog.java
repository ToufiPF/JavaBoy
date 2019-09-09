package ch.epfl.javaboy.gui;

import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class OptionDialog extends Stage {
    private final Scene scene;
    private final VBox principal;
    private final TabPane pane;

    public OptionDialog() {
        pane = new TabPane();
        Tab general = new Tab("General");
        pane.getTabs().add(general);

        Tab controls = new Tab("Controls");
        pane.getTabs().add(controls);

        principal = new VBox();
        principal.getChildren().add(pane);
        scene = new Scene(principal);
    }
}
