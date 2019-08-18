package ch.epfl.javaboy.gui.savestates;

public final class LoadDialog extends StatesDialog<String> {

    public LoadDialog() {
        super();
        setTitle("Load States");
    }

    @Override
    protected void refreshStateNodes() {
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
    }
}
