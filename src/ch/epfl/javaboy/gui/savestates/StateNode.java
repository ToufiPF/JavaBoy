package ch.epfl.javaboy.gui.savestates;

import ch.epfl.javaboy.component.lcd.ImageConverter;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

class StateNode extends Parent {

    static final int WIDTH = 300;
    static final int HEIGTH = 150;

    private final String title;

    StateNode(String title, State.Metadata metadata) {
        this.title = title;
        VBox txtLay = new VBox();
        txtLay.setAlignment(Pos.CENTER_RIGHT);
        txtLay.setSpacing(20);
        Text titleTxt = new Text(title);
        titleTxt.setUnderline(true);
        titleTxt.setStyle("-fx-font: 20 arial;");
        Text dateTxt = new Text(metadata.getDateAndTime());
        txtLay.getChildren().addAll(titleTxt, dateTxt);

        ImageView preview = new ImageView();
        preview.setImage(ImageConverter.convert(metadata.getScreenshot()));

        HBox layout = new HBox();
        layout.setSpacing(20);
        layout.setMinSize(WIDTH, HEIGTH);
        layout.setMaxSize(WIDTH, HEIGTH);
        layout.getChildren().addAll(txtLay, preview);
        layout.setStyle("-fx-background-color: DAE6F3;");
        getChildren().add(layout);
    }
    String getTitle() {
        return title;
    }
}
