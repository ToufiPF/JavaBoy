package ch.epfl.javaboy.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

class OptionsDialog {
    private final Stage stage;
    private final Tab general, controls;

    private final Main main;
    private final CheckBox autoLoad;
    private KeyboardToGBJoypadNode keyboardControls;

    OptionsDialog(Main main) {
        this.main = main;
        stage = new Stage();

        TabPane pane = new TabPane();
        pane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        pane.setTabDragPolicy(TabPane.TabDragPolicy.FIXED);

        general = new Tab("General");
        {
            GridPane grid = new GridPane();
            grid.setPadding(new Insets(5));
            grid.setHgap(5);
            grid.setVgap(10);

            autoLoad = new CheckBox();
            autoLoad.setSelected(main.autoLoadWhenLaunchingRom);
            Label lblAutoLoad = new Label("Auto load when launching a Rom");
            lblAutoLoad.setPadding(new Insets(5, 0, 0, 5));
            autoLoad.setGraphic(lblAutoLoad);

            grid.add(autoLoad, 0, 0);

            general.setContent(grid);
        }
        pane.getTabs().add(general);

        controls = new Tab("Controls");
        pane.getTabs().add(controls);

        HBox buttonLay = new HBox();
        Button apply = new Button("Apply");
        apply.setOnAction(e -> save());

        Button cancel = new Button("Cancel");
        cancel.setOnAction(e -> stage.close());

        Button ok = new Button("Ok");
        ok.setOnAction(e -> {
            save();
            stage.close();
        });
        buttonLay.getChildren().addAll(ok, cancel, apply);
        buttonLay.setAlignment(Pos.CENTER_RIGHT);
        buttonLay.setSpacing(10);
        buttonLay.setPadding(new Insets(5));

        VBox principal = new VBox();
        principal.getChildren().addAll(pane, buttonLay);
        stage.setScene(new Scene(principal));
        stage.setTitle("Options");
    }

    private void save() {
        // General
        main.autoLoadWhenLaunchingRom = autoLoad.isSelected();

        // KeyMap
        main.keysMap = keyboardControls.getResult();
    }

    void showAndWait() {
        // Refreshing all the Options
        autoLoad.setSelected(main.autoLoadWhenLaunchingRom);

        keyboardControls = new KeyboardToGBJoypadNode(main.keysMap);
        controls.setContent(keyboardControls);

        // Showing the Stage
        stage.showAndWait();
    }
}
