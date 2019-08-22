package ch.epfl.javaboy.component.lcd;

import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Used to convert an LcdImage to
 * different formats
 * @author Toufi
 */
public final class ImageConverter {

    private static final int[] COLOR_MAP = {
            0xFFFF_FFFF,
            0xFFD3_D3D3,
            0xFFA9_A9A9,
            0xFF00_0000
    };

    /**
     * Convert the given LcdImage to a JavaFX Image
     * @param lcdImg (LcdImage) toConvert
     * @return (Image) JavaFX Image
     */
    public static Image convert(LcdImage lcdImg) {
        WritableImage img = new WritableImage(lcdImg.width(), lcdImg.height());
        PixelWriter writer = img.getPixelWriter();

        for (int y = 0 ; y < lcdImg.height() ; ++y)
            for (int x = 0 ; x < lcdImg.width() ; ++x)
                writer.setArgb(x, y, COLOR_MAP[lcdImg.getColor(x, y)]);

        return img;
    }

    /**
     * Converts a LcdImage to an array of bytes.
     * Note : Does not save the dimensions of the image,
     * please save them in another way
     * Expected array length : height * (2 * width / Byte.SIZE).
     * @param img (LcdImage) image to convert
     * @return (byte[]) array representing the LcdImage
     * @throws IOException if a problem occurs when writing
     * in the ByteArrayOutputStream
     */
    public static byte[] toByteArray(LcdImage img) throws IOException{
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        for (LcdImageLine line : img.getLines()) {
            os.write(line.msb().getBytes());
            os.write(line.lsb().getBytes());
        }
        return os.toByteArray();
    }

    /**
     * Turns back a byte array to a LcdImage
     * @param array (byte[]) array to convert, given by
     *              ImageConverter.toByteArray()
     * @param width (int) width of the image
     * @param height (int) height of the image
     * @return (LcdImage) the rebuilt image
     */
    public static LcdImage fromByteArray(byte[] array, int width, int height) {
        return builderFromByteArray(array, width, height).build();
    }

    public static LcdImage.Builder builderFromByteArray(byte[] array, int width, int height) {
        LcdImage.Builder builder = new LcdImage.Builder(width, height);
        final int bytesInLine = width / Byte.SIZE;

        byte[] msbs = new byte[bytesInLine];
        byte[] lsbs = new byte[bytesInLine];
        int from = 0;
        for (int y = 0 ; y < height ; ++y) {
            LcdImageLine.Builder b = new LcdImageLine.Builder(width);
            System.arraycopy(array, from, msbs, 0, bytesInLine);
            from += bytesInLine;
            System.arraycopy(array, from, lsbs, 0, bytesInLine);
            from += bytesInLine;

            for (int i = 0 ; i < bytesInLine ; ++i)
                b.setBytes(i, Byte.toUnsignedInt(msbs[i]), Byte.toUnsignedInt(lsbs[i]));
            builder.setLine(y, b.build());
        }
        return builder;
    }

    private ImageConverter() {
    }
}
