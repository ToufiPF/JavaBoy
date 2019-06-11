package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.component.Clocked;
import ch.epfl.javaboy.component.Component;

public interface Channel extends Component, Clocked {
    
    interface Square1Bits {
    }
    
    interface Square2Bits {
        
    }
    
    interface WaveBits {
        
    }
    
    interface NoiseBits {
        
    }
    
    interface ControlBits {
        
    }
    
    
    
    public int getOutput();
}
