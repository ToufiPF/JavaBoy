package ch.epfl.javaboy.gui.savestates;

/**
 * LoadDialog
 * A Dialog made to display available saves slots,
 * and select the one to use (for loading).
 * Returns an Optionnal<String> containing
 * the name of the save chosen by the user
 * after a call to showAndWait()
 */
public final class LoadDialog extends StatesDialog {

    /**
     * Creates a new LoadDialog
     */
    public LoadDialog() {
        super();
        setTitle("Load States");
    }

    @Override
    public void refreshStateNodes() {
        super.refreshStateNodes();

        content.getChildren().clear();
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
            content.getChildren().add(n);
        }
        for (int i = 0 ; i < regularNodes.size() ; ++i) {
            int finalI = i;
            regularNodes.get(i).setOnMouseClicked(e -> {
                setResult(REGULAR_STATE + finalI);
                close();
            });
            content.getChildren().add(regularNodes.get(i));
        }
    }
}
