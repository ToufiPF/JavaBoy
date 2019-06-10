package ch.epfl.javaboy.gui;

import ch.epfl.javaboy.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public final class ImageConverter {
    
    private static final int[] COLOR_MAP = {
            0xFFFF_FFFF,
            0xFFD3_D3D3,
            0xFFA9_A9A9,
            0xFF00_0000
    };
    
    public static Image convert(LcdImage lcdImg) {
        
        WritableImage img = new WritableImage(lcdImg.width(), lcdImg.height());
        PixelWriter writer = img.getPixelWriter();
        
        for (int y = 0 ; y < lcdImg.height() ; ++y)
            for (int x = 0 ; x < lcdImg.width() ; ++x)
                writer.setArgb(x, y, COLOR_MAP[lcdImg.getColor(x, y)]);
        
        return img;
    }
}
