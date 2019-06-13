package ch.epfl.javaboy.gui;

import javafx.scene.control.Alert;

/**
 * Dialog to show when the "about" menu is clicked
 * @author Toufi
 */
public class AboutDialog extends Alert {
    public AboutDialog() {
        super(AlertType.INFORMATION);
        setTitle("About Javaboy - GameBoy Emulator");
        setHeaderText(null);
        
        StringBuilder b = new StringBuilder();
        b.append("Javaboy is a GameBoy emulator made with Java 8.\n");
        b.append("This is an augmented version of the EPFL emulator Gameboj, ");
        b.append("project of the year 2018 (cs108 - Practice of OOP).\n");
        b.append("Made in 2019 by Toufi, for training purpose.\n");
        setContentText(b.toString());
    }
}
