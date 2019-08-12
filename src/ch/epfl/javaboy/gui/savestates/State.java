package ch.epfl.javaboy.gui.savestates;

import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.lcd.LcdImage;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class State {
    public static final String ABSOLUTE_SAVE_PATH = new File("").getAbsolutePath() + "/Saves/";

    private static final String AUTO_SAVE_NAME = "auto";
    private static final String QUICK_SAVE_NAME = "quick";

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    public static String getStatesPath(String romName) {
        return State.ABSOLUTE_SAVE_PATH + romName.replace('.', '-') + '/';
    }

    static void saveState(String pathAndStateName, State state) throws IOException {
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
            //TODO: os.write(img);
        }
        {
            File stateFile = new File(pathAndStateName + ".dat");
            FileOutputStream os = new FileOutputStream(stateFile);
            os.write(state.gbState);
        }
    }
    static State loadState(String pathAndStateName) throws IOException {
        Metadata meta = loadMetadata(pathAndStateName);
        byte[] data = loadData(pathAndStateName);
        return new State(meta, data);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static Metadata loadMetadata(String pathAndStateName) throws IOException {
        File metadataFile = new File(pathAndStateName + ".meta");
        try (FileInputStream is  = new FileInputStream(metadataFile)) {
            byte[] buff = new byte[4];
            is.read(buff);
            final int year = Bits.recomposeInteger(buff);
            final LocalDateTime ldt = LocalDateTime.of(year, is.read(), is.read(), is.read(), is.read(), is.read());

            is.read(buff);
            final int height = Bits.recomposeInteger(buff);
            is.read(buff);
            final int width = Bits.recomposeInteger(buff);
            //TODO: is.read(LcdImage);
            return new Metadata(ldt, null);
        } catch (IOException e) {
            throw e;
        }
    }
    static byte[] loadData(String pathAndStateName) throws IOException {
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
    private State(Metadata meta, byte[] gbState) {
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
