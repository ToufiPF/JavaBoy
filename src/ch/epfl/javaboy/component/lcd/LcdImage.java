package ch.epfl.javaboy.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class LcdImage {
    
    public static final class Builder {
        private final List<LcdImageLine> lines;
        public Builder(int width, int height) {
            lines = new ArrayList<>(height);
            for (int i = 0 ; i < height ; ++i)
                lines.add(new LcdImageLine(width));
        }
        
        public void setLine(int y, LcdImageLine line) {
            lines.set(y, line);
        }
        
        public LcdImage build() {
            return new LcdImage(lines);
        }
    }
    
    private final List<LcdImageLine> lines;
    
    public LcdImage(List<LcdImageLine> lines) {
        this.lines = Collections.unmodifiableList(new LinkedList<>(lines));
    }
    
    public int width() {
        if (lines.isEmpty())
            return 0;
        return lines.get(0).size();
    }
    
    public int height() {
        return lines.size();
    }
    
    public int getColor(int x, int y) {
        LcdImageLine l = lines.get(y);
        int color = l.lsb().testBit(x) ? 1 : 0;
        color |= (l.msb().testBit(x) ? 1 : 0) << 1;
        return color;
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
