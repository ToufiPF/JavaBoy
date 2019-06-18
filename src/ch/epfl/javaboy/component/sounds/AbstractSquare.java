package ch.epfl.javaboy.component.sounds;

import ch.epfl.javaboy.bits.Bits;
import ch.epfl.javaboy.component.sounds.subcomponent.FrameSequencer;
import ch.epfl.javaboy.component.sounds.subcomponent.LengthCounter;
import ch.epfl.javaboy.component.sounds.subcomponent.SoundTimer;
import ch.epfl.javaboy.component.sounds.subcomponent.VolumeEnvelope;

public abstract class AbstractSquare {    

    private final FrameSequencer fs;
    private final LengthCounter lc;
    private final VolumeEnvelope ve;
    
    private SoundTimer timerWave;
    
    private int waveBitIndex = 0;
    private boolean currBit;
    
    private int output;
    
    public AbstractSquare(FrameSequencer frameSequencer) {
        fs = frameSequencer;
        lc = new LengthCounter();
        ve = new VolumeEnvelope();
        
        timerWave = SoundTimer.fromFrequency(getFreqWave());
    }
    
    @Override
    public void cycle(long cycle) {
        fs.cycle(cycle);
        lc.cycleIfEnabled(fs.enable256Hz());
        
        if (timerWave.enable()) {
            currBit = Bits.test(getWaveForm(), waveBitIndex) && lc.isEnabled();
            ++waveBitIndex;
            if (waveBitIndex >= Byte.SIZE)
                waveBitIndex = 0;
        }
        
        ve.setIncrement(Bits.test(getNRXN(2), ENVELOPE_ADD_MODE_BIT));
        ve.cycleIfEnabled(fs.enable64Hz());
        
        if (currBit)
            output = ve.getVolume();
        else
            output =0;
    }
    
    @Override
    public int getOutput() {
        return output;
    }
    
    protected abstract int getNRXN(int N);
    protected abstract int getFreqWave();
    
    private int getWaveForm() {
        int NRx1 = getNRXN(1);
        
        return WAVE_FORM[Bits.extract(NRx1, DUTY_START, DUTY_SIZE)];
    }
    
    private int getStartingVolume() {
        int NRx2 = getNRXN(2);
        
    }
}
