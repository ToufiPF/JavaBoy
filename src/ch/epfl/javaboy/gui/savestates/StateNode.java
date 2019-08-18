package ch.epfl.javaboy.gui.savestates;

import ch.epfl.javaboy.gui.ImageConverter;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class StateNode extends Parent {

    private final String title;

    public StateNode(String title, State.Metadata metadata) {
        this.title = title;
        VBox txtLay = new VBox();
        Text titleTxt = new Text(title);
        titleTxt.setUnderline(true);
        Text dateTxt = new Text(metadata.getDateAndTime());
        txtLay.getChildren().addAll(titleTxt, dateTxt);

        ImageView preview = new ImageView();
        preview.setImage(ImageConverter.convert(metadata.getScreenshot()));

        HBox layout = new HBox();
        layout.getChildren().addAll(txtLay, preview);
        getChildren().add(layout);
    }

    public String getTitle() {
        return title;
    }
}
