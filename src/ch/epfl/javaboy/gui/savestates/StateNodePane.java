package ch.epfl.javaboy.gui.savestates;

import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;

class StateNodePane extends FlowPane {
    private final ToggleGroup group;

    StateNodePane() {
        super();

        group = new ToggleGroup();
        setHgap(30);
        setVgap(25);
        setPrefWrapLength(StatesDialog.DIALOG_WIDTH);
        setOrientation(Orientation.HORIZONTAL);

        setOnMouseClicked(e -> {
            if (e.getTarget() instanceof StateNode)
                group.selectToggle((StateNode) e.getTarget());
            else
                group.selectToggle(null);
            
            e.consume();
        });

        getChildren().addListener((ListChangeListener<Node>) change -> {
            group.getToggles().clear();
            for (Node n : change.getList())
                if (n instanceof StateNode)
                    group.getToggles().add((StateNode) n);
        });
    }

    StateNode getSelection() {
        return (StateNode) group.getSelectedToggle();
    }
}
