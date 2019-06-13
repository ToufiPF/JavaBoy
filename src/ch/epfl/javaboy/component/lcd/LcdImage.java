package ch.epfl.javaboy.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a Image computed
 * by the LcdController
 * @author Toufi
 */
public final class LcdImage {
    
    /**
     * Builder for an LcdImage
     * @author Toufi
     */
    public static final class Builder {
        private final List<LcdImageLine> lines;
        
        /**
         * Constructs a new LcdImage.Builder,
         * with a blank LcdImage of the given dimensions
         * @param width (int) width of the image to build
         * @param height (int) height of the image to build
         */
        public Builder(int width, int height) {
            lines = new ArrayList<>(height);
            for (int i = 0 ; i < height ; ++i)
                lines.add(new LcdImageLine(width));
        }
        
        /**
         * Sets the line at the given index
         * @param y (int) index of the line
         * @param line (LcdImageLine) the line to set
         * @return (Builder) this builder
         */
        public Builder setLine(int y, LcdImageLine line) {
            Objects.requireNonNull(line);
            lines.set(y, line);
            return this;
        }
        
        /**
         * Builds the LcdImage
         * @return (LcdImage) the LcdImage built
         */
        public LcdImage build() {
            return new LcdImage(lines);
        }
    }
    
    private final List<LcdImageLine> lines;
    
    /**
     * Constructs a new LcdImage from
     * the given list of lines
     * @param lines (List<LcdImageLine>) the list of
     * LcdImageLine used to construct the LcdImage
     */
    public LcdImage(List<LcdImageLine> lines) {
        this.lines = Collections.unmodifiableList(new LinkedList<>(lines));
    }
    
    /**
     * Returns the width of the LcdImage
     * @return (int) image width
     */
    public int width() {
        if (lines.isEmpty())
            return 0;
        return lines.get(0).size();
    }
    
    /**
     * Returns the height of the LcdImage
     * @return (int) image height
     */
    public int height() {
        return lines.size();
    }
    
    /**
     * Returns the color of the pixel
     * at the given position
     * as an int in [0;3]
     * @param x (int) x-axis position
     * @param y (int) y-axis position
     * @return (int) color of the pixel
     */
    public int getColor(int x, int y) {
        LcdImageLine l = lines.get(y);
        return (l.lsb().testBit(x) ? 0b01 : 0b00) 
                | (l.msb().testBit(x) ? 0b10 : 0b00);
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LcdImage && lines.equals(((LcdImage)obj).lines);
    }
    @Override
    public int hashCode() {
        return lines.hashCode();
    }
}
