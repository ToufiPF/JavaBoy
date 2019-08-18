package ch.epfl.javaboy.gui.savestates;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;

/**
 * SaveDialog
 * A Dialog made to display open saves slots,
 * and select the one to use (for saving).
 * Returns an Optionnal<String> containing
 * the name of the save chosen by the user
 * after a call to showAndWait()
 */
public final class SaveDialog extends StatesDialog {

    private final Node newSaveNode;

    private Node createNewSaveNode() {
        HBox lay = new HBox();
        lay.setMinSize(StateNode.WIDTH, StateNode.HEIGTH);
        lay.setMaxSize(StateNode.WIDTH, StateNode.HEIGTH);
        lay.setAlignment(Pos.CENTER);
        Button txt = new Button("Add New Save");
        txt.setStyle("-fx-font: 24 arial;");
        txt.setOnMouseClicked(e -> {
            System.out.println("New Save request n°" + regularNodes.size());
            setResult(REGULAR_STATE + regularNodes.size());
            close();
        });
        lay.getChildren().add(txt);
        return lay;
    }

    /**
     * Creates a new SaveDialog
     */
    public SaveDialog() {
        super();
        setTitle("Save States");
        newSaveNode = createNewSaveNode();
    }
    @Override
    public void refreshStateNodes() {
        super.refreshStateNodes();

        content.getChildren().clear();
        for (int i = 0 ; i < regularNodes.size() ; ++i) {
            int finalI = i;
            regularNodes.get(i).setOnMouseClicked(e -> {
                setResult(REGULAR_STATE + finalI);
                close();
            });
            content.getChildren().add(regularNodes.get(i));
        }
        if (regularNodes.size() < REGULAR_SAVE_SLOTS)
            content.getChildren().add(newSaveNode);
    }
}
