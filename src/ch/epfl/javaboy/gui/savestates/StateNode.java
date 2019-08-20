package ch.epfl.javaboy.gui.savestates;

import ch.epfl.javaboy.component.lcd.ImageConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

class StateNode extends Parent implements Toggle {

    private final static String DEFAULT_STYLE = "-fx-background-color: #D8D8D8; " +
            "-fx-background-radius: 5 5 5 5; -fx-border-radius: 5 5 5 5;";
    private final static String SELECTED_STYLE = "-fx-background-color: #31acf6; " +
            "-fx-background-radius: 5 5 5 5; -fx-border-radius: 5 5 5 5;";

    static final int WIDTH = 290;
    static final int HEIGTH = 150;

    private final ObjectProperty<ToggleGroup> group;
    private final BooleanProperty selected;
    private final String saveName;

    StateNode(String title, String saveName, State.Metadata metadata) {
        group = new SimpleObjectProperty<>();
        selected = new SimpleBooleanProperty(false);
        this.saveName = saveName;

        addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getTarget() != this) {
                e.consume();
                Event.fireEvent(this, new MouseEvent(this, this, MouseEvent.MOUSE_CLICKED, e.getX(), e.getY(),
                        e.getScreenX(), e.getScreenY(), e.getButton(), e.getClickCount(),
                        e.isShiftDown(), e.isControlDown(), e.isAltDown(), e.isMetaDown(),
                        e.isPrimaryButtonDown(), e.isMiddleButtonDown(), e.isSecondaryButtonDown(),
                        e.isSynthesized(), e.isPopupTrigger(), e.isStillSincePress(),
                        new PickResult(this, e.getSceneX(), e.getSceneY())));
            }
        });
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
        layout.setSpacing(10);
        layout.setAlignment(Pos.CENTER);
        layout.setMinSize(WIDTH, HEIGTH);
        layout.setMaxSize(WIDTH, HEIGTH);
        layout.getChildren().addAll(txtLay, preview);
        layout.setStyle(DEFAULT_STYLE);
        getChildren().add(layout);
        selected.addListener((obs, oldV, newV) -> {
            if (newV) {
                layout.setStyle(SELECTED_STYLE);
            } else {
                layout.setStyle(DEFAULT_STYLE);
            }
        });
    }

    @Override
    public ToggleGroup getToggleGroup() {
        return group.get();
    }
    @Override
    public void setToggleGroup(ToggleGroup toggleGroup) {
        group.setValue(toggleGroup);
    }
    @Override
    public ObjectProperty<ToggleGroup> toggleGroupProperty() {
        return group;
    }

    @Override
    public boolean isSelected() {
        return selected.get();
    }
    @Override
    public void setSelected(boolean b) {
        selected.set(b);
    }
    @Override
    public BooleanProperty selectedProperty() {
        return selected;
    }

    String getSaveName() {
        return saveName;
    }
}
