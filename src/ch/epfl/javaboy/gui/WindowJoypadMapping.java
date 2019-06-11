package ch.epfl.javaboy.gui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import ch.epfl.javaboy.component.Joypad;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;

public class WindowJoypadMapping extends Dialog<Map<KeyCode, Joypad.Key>> {
    
    public WindowJoypadMapping(Map<KeyCode, Joypad.Key> keyMap) {
        super();
        // Title
        setTitle("Configuration Keyboard -> Joypad");
        
        // Set Buttons
        getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        getDialogPane().setMinSize(550, 350);
        getDialogPane().setMaxSize(550, 350);
        
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
        
        Map<Joypad.Key, HashSet<KeyCode>> keyCodes = toKeyCodes(keyMap);
        
        Button[] buttonsKeys = new Button[Joypad.Key.COUNT];
        for (int i = 0 ; i < buttonsKeys.length ; ++i) {
            final Joypad.Key id = Joypad.Key.ALL.get(i);
            final int ordinal = i;
            buttonsKeys[ordinal] = new Button(keyCodes.get(id).toString());
            buttonsKeys[ordinal].setMinWidth(150);
            buttonsKeys[ordinal].setMaxWidth(200);
            buttonsKeys[ordinal].setOnMouseClicked(e -> {
                keyCodes.get(id).clear();
                buttonsKeys[ordinal].setText(keyCodes.get(id).toString());
                buttonsKeys[ordinal].requestFocus();
            });
            buttonsKeys[ordinal].setOnKeyReleased(e -> {
                for (HashSet<KeyCode> set : keyCodes.values())
                    if (set.contains(e.getCode()))
                        return;
                
                keyCodes.get(id).add(e.getCode());
                buttonsKeys[ordinal].setText(keyCodes.get(id).toString());
            });
            int col = ordinal < 4 ? 1 : 4;
            grid.add(buttonsKeys[ordinal], col, ordinal % 4);
        }
        
        getDialogPane().setContent(grid);
        
        setResultConverter(btn -> {
            if (btn.equals(ButtonType.OK))
                return toKeyMap(keyCodes);
            return keyMap;
        });
    }
    
    private Map<Joypad.Key, HashSet<KeyCode>> toKeyCodes(Map<KeyCode, Joypad.Key> keysMap) {
        Map<Joypad.Key, HashSet<KeyCode>> keyCodes = new HashMap<>();
        for (Map.Entry<KeyCode, Joypad.Key> e : keysMap.entrySet()) {
            HashSet<KeyCode> codesForKey = keyCodes.containsKey(e.getValue()) ?
                    keyCodes.get(e.getValue()) : new HashSet<>();
            codesForKey.add(e.getKey());
            keyCodes.put(e.getValue(), codesForKey);
        }
        return keyCodes;
    }
    
    private Map<KeyCode, Joypad.Key> toKeyMap(Map<Joypad.Key, HashSet<KeyCode>> keyCodes) {
        Map<KeyCode, Joypad.Key> keysMap = new HashMap<>();
        for (Map.Entry<Joypad.Key, HashSet<KeyCode>> e : keyCodes.entrySet()) {
            for (KeyCode k : e.getValue())
                keysMap.put(k, e.getKey());
        }
        return keysMap;
    }
}
