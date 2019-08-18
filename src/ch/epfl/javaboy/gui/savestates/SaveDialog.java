package ch.epfl.javaboy.gui.savestates;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public final class SaveDialog extends StatesDialog<String> {

    private final Node newSaveNode;

    private Node createNewSaveNode() {
        HBox lay = new HBox();
        lay.setMinHeight(50);
        lay.setMaxHeight(50);
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

    public SaveDialog() {
        super();
        setTitle("Save States");
        newSaveNode = createNewSaveNode();
    }
    @Override
    public void refreshStateNodes() {
        super.refreshStateNodes();

        layout.getChildren().clear();
        for (StateNode n : specialNodes) {
            if (n.getTitle().equals("AutoSave")) {
                n.setOnMouseClicked(e -> {
                    setResult(AUTO_STATE);
                    close();
                });
            } else if (n.getTitle().equals("QuickSave")) {
                n.setOnMouseClicked(e -> {
                    setResult(QUICK_STATE);
                    close();
                });
            }
            layout.getChildren().add(n);
        }
        for (int i = 0 ; i < regularNodes.size() ; ++i) {
            int finalI = i;
            regularNodes.get(i).setOnMouseClicked(e -> {
                setResult(REGULAR_STATE + finalI);
                close();
            });
            layout.getChildren().add(regularNodes.get(i));
        }
        if (regularNodes.size() < REGULAR_SAVE_SLOTS)
            layout.getChildren().add(newSaveNode);
    }
}
