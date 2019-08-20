package ch.epfl.javaboy.gui.savestates;

import javafx.beans.binding.ListBinding;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;

public class StateNodePane extends FlowPane {
    private final ToggleGroup group;

    public StateNodePane() {
        super();

        group = new ToggleGroup();
        setPadding(new Insets(5));
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

    public StateNode getSelection() {
        return (StateNode) group.getSelectedToggle();
    }
}
