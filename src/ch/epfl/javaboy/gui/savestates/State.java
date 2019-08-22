package ch.epfl.javaboy.gui.savestates;

import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.lcd.ImageConverter;
import ch.epfl.javaboy.component.lcd.LcdImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@SuppressWarnings({"unused", "WeakerAccess"})
public final class State {
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static void saveState(String pathAndStateName, State state) throws IOException {
        {
            File metadataFile = new File(pathAndStateName + ".meta");
            FileOutputStream os = new FileOutputStream(metadataFile);
            final LocalDateTime ldt = state.metadata.dateAndTime;
            os.write(Bits.decomposeInteger(ldt.getYear()));
            os.write(ldt.getMonthValue());
            os.write(ldt.getDayOfMonth());
            os.write(ldt.getHour());
            os.write(ldt.getMinute());
            os.write(ldt.getSecond());

            final LcdImage img = state.metadata.screenshot;
            os.write(Bits.decomposeInteger(img.height()));
            os.write(Bits.decomposeInteger(img.width()));

            os.write(ImageConverter.toByteArray(img));
        }
        {
            File stateFile = new File(pathAndStateName + ".dat");
            FileOutputStream os = new FileOutputStream(stateFile);
            os.write(state.gbState);
        }
    }
    public static State loadState(String pathAndStateName) throws IOException {
        Metadata meta = loadMetadata(pathAndStateName);
        byte[] data = loadData(pathAndStateName);
        return new State(meta, data);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Metadata loadMetadata(String pathAndStateName) throws IOException {
        File metadataFile = new File(pathAndStateName + ".meta");
        FileInputStream is  = new FileInputStream(metadataFile);

        byte[] buff = new byte[4];
        is.read(buff);
        final int year = Bits.recomposeInteger(buff);
        final LocalDateTime ldt = LocalDateTime.of(year, is.read(), is.read(), is.read(), is.read(), is.read());

        is.read(buff);
        final int height = Bits.recomposeInteger(buff);
        is.read(buff);
        final int width = Bits.recomposeInteger(buff);

        buff = new byte[height * (2 * width / Byte.SIZE)];
        is.read(buff);
        return new Metadata(ldt, ImageConverter.fromByteArray(buff, width, height));
    }
    public static byte[] loadData(String pathAndStateName) throws IOException {
        File stateFile = new File(pathAndStateName + ".dat");
        FileInputStream is = new FileInputStream(stateFile);
        return is.readAllBytes();
    }

    public static final class Metadata {
        private final LocalDateTime dateAndTime;
        private final LcdImage screenshot;

        private Metadata(LocalDateTime dateAndTime, LcdImage screenshot) {
            this.dateAndTime = dateAndTime;
            this.screenshot = screenshot;
        }
        String getDateAndTime() {
            return dateAndTime.format(DATE_TIME_FORMAT);
        }
        LcdImage getScreenshot() {
            return screenshot;
        }
    }

    private final Metadata metadata;
    private final byte[] gbState;

    State(LocalDateTime dateAndTime, LcdImage screenshot, byte[] gbState) {
        metadata = new Metadata(dateAndTime, screenshot);
        this.gbState = gbState;
    }
    State(Metadata meta, byte[] gbState) {
        this.metadata = meta;
        this.gbState = gbState;
    }

    public String getDateAndTime() {
        return metadata.getDateAndTime();
    }
    public LcdImage getScreenshot() {
        return metadata.getScreenshot();
    }
    public byte[] getGbState() {
        return gbState;
    }
}
