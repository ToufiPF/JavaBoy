package ch.epfl.javaboy.gui;

import ch.epfl.javaboy.component.Joypad;
import ch.epfl.javaboy.component.Joypad.Key;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * KeyboardToGBJoypadNode
 * Node to use when asking the user to enter custom
 * key mapping (keyboard supported)
 * Also contains various utilities linked to the mapping
 * of the Joypad keys
 * @author Toufi
 */
class KeyboardToGBJoypadNode extends Parent {
    
    /**
     * Serializes the given keyMap
     * @param keyMap (Map<KeyCode, Joypad.Key>) map to serialize
     * @return (String) serialized map
     */
    static String serializeKeyMap(Map<KeyCode, Joypad.Key> keyMap) {
        StringBuilder b = new StringBuilder();
        for (Map.Entry<KeyCode, Joypad.Key> e : keyMap.entrySet())
            b.append(e.getKey().toString()).append("=>")
            .append(e.getValue().toString()).append('\n');
        return b.toString();
    }
    /**
     * Deserialize the given String and
     * returns the corresponding keyMap
     * @param str (String) serialized keyMap
     * @return (Map<KeyCode, Joypad.Key>) keyMap
     */
    static Map<KeyCode, Joypad.Key> deserializeKeyMap(String str) {
        Map<KeyCode, Joypad.Key> keyMap = new HashMap<>();
        String[] lines = str.split("\n");
        for (int i = 0 ; i < lines.length ; ++i) {
            lines[i] = lines[i].trim();
            if (lines[i].isEmpty())
                continue;

            String[] subSplit = lines[i].split("=>");
            if (subSplit.length != 2)
                throw new IllegalArgumentException("DeserializeKeyMap : Invalid String : " + lines[i]);

            KeyCode key = KeyCode.valueOf(subSplit[0]);
            Joypad.Key val = Joypad.Key.valueOf(subSplit[1]);
            keyMap.put(key, val);
        }
        return keyMap;
    }
    /**
     * Returns the default keyMap
     * @return Map<KeyCode, Joypad.Key> default keyMap
     */
    static Map<KeyCode, Joypad.Key> defaultKeyMap() {
        Map<KeyCode, Joypad.Key> keyMap = new HashMap<>();

        keyMap.put(KeyCode.D, Key.RIGHT);
        keyMap.put(KeyCode.Q, Key.LEFT);
        keyMap.put(KeyCode.Z, Key.UP);
        keyMap.put(KeyCode.S, Key.DOWN);

        keyMap.put(KeyCode.E, Key.A);
        keyMap.put(KeyCode.A, Key.B);
        keyMap.put(KeyCode.TAB, Key.SELECT);
        keyMap.put(KeyCode.ESCAPE, Key.START);
        keyMap.put(KeyCode.ENTER, Key.START);

        return keyMap;
    }

    private static Map<KeyCode, Joypad.Key> toKeyMap(ObservableMap<Joypad.Key, ObservableSet<KeyCode>> keyCodes) {
        Map<KeyCode, Joypad.Key> keysMap = new HashMap<>();
        for (Map.Entry<Joypad.Key, ObservableSet<KeyCode>> e : keyCodes.entrySet()) {
            for (KeyCode k : e.getValue())
                keysMap.put(k, e.getKey());
        }
        return keysMap;
    }
    private static ObservableMap<Joypad.Key, ObservableSet<KeyCode>> toKeyCodes(Map<KeyCode, Joypad.Key> keysMap) {
        ObservableMap<Joypad.Key, ObservableSet<KeyCode>> keyCodes = FXCollections.observableMap(new HashMap<>());
        for (Map.Entry<KeyCode, Joypad.Key> e : keysMap.entrySet()) {
            ObservableSet<KeyCode> codesForKey = keyCodes.containsKey(e.getValue()) ?
                    keyCodes.get(e.getValue()) : FXCollections.observableSet();
            codesForKey.add(e.getKey());
            keyCodes.put(e.getValue(), codesForKey);
        }
        return keyCodes;
    }

    private final ObservableMap<Joypad.Key, ObservableSet<KeyCode>> keyCodes;
    /**
     * Constructs a new JoypadMapDialog,
     * with the given keyMap
     * @param keyMap (Map<KeyCode, Joypad.Key>) actual keyMap
     */
    KeyboardToGBJoypadNode(Map<KeyCode, Joypad.Key> keyMap) {
        super();

        // Grid
        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(15);
        {
            Label right = new Label("Right : ");
            Label left = new Label("Left : ");
            Label up = new Label("Up : ");
            Label down = new Label("Down : ");

            Label A = new Label("A : ");
            Label B = new Label("B : ");
            Label select = new Label("Select : ");
            Label start = new Label("Start : ");

            grid.add(right, 0, 0);
            grid.add(left, 0, 1);
            grid.add(up, 0, 2);
            grid.add(down, 0, 3);

            grid.add(new Label(" "), 2, 0, 1, 4);

            grid.add(A, 3, 0);
            grid.add(B, 3, 1);
            grid.add(select, 3, 2);
            grid.add(start, 3, 3);
        }

        keyCodes = toKeyCodes(keyMap);
        
        for (int i = 0 ; i < Joypad.Key.COUNT ; ++i) {
            final Joypad.Key id = Joypad.Key.ALL.get(i);
            Button buttonKeys = new Button();
            buttonKeys.setMinWidth(150);
            buttonKeys.setMaxWidth(200);
            buttonKeys.textProperty().bind(
                    Bindings.createStringBinding(() -> keyCodes.get(id).toString(),
                            keyCodes, keyCodes.get(id)));

            buttonKeys.setOnMouseClicked(e -> {
                keyCodes.get(id).clear();
                buttonKeys.requestFocus();
                e.consume();
            });
            buttonKeys.addEventFilter(KeyEvent.KEY_PRESSED, Event::consume);
            buttonKeys.addEventFilter(KeyEvent.KEY_RELEASED, e -> {
                for (Set<KeyCode> set : keyCodes.values())
                    if (set.contains(e.getCode()))
                        return;

                keyCodes.get(id).add(e.getCode());
                e.consume();
            });
            
            int col = i < 4 ? 1 : 4;
            grid.add(buttonKeys, col, i % 4);
        }

        // Reset Button
        Button resetDefault = new Button("Default");
        resetDefault.setOnMouseClicked(e -> {
            Map<Joypad.Key, ObservableSet<KeyCode>> defaultKeysCode = toKeyCodes(defaultKeyMap());
            for (Map.Entry<Joypad.Key, ObservableSet<KeyCode>> entry : keyCodes.entrySet()) {
                entry.getValue().clear();
                entry.getValue().addAll(defaultKeysCode.get(entry.getKey()));
            }
        });
        grid.add(resetDefault, 2, 5);
        grid.setPadding(new Insets(5));
        getChildren().add(grid);
    }

    Map<KeyCode, Joypad.Key> getResult() {
        return toKeyMap(keyCodes);
    }
}
