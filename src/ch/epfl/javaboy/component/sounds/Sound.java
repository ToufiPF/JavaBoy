package ch.epfl.javaboy.component.sounds;

public final class Sound {
    public interface NRX1Bits {
        int LENGTH_LOAD_START = 0, LENGTH_LOAD_SIZE = 6;
        int DUTY_START = 6, DUTY_SIZE = 2;
    }
    public interface NRX2Bits {
        int ENVELOPE_PERIOD_START = 0, ENVELOPE_PERIOD_SIZE = 3;
        int ENVELOPE_ADD_MODE_BIT = 3;
        int STARTING_VOLUME_START = 4, STARTING_VOLUME_SIZE = 4;
    }
    public interface NRX3Bits {
        int FREQUENCY_LSB_START = 0, FREQUENCY_LSB_SIZE = 8;
    }
    public interface NRX4Bits {
        int FREQUENCY_MSB_START = 0, FREQUENCY_MSB_SIZE = 3;
        int LENGTH_ENABLE_BIT = 6;
        int TRIGGER_BIT = 7;
    }

    public interface NR10Bits {
        int SWEEP_SHIFT_START = 0, SWEEP_SHIFT_SIZE = 3;
        int SWEEP_NEGATE_BIT = 3;
        int SWEEP_PERIOD_START = 4, SWEEP_PERIOD_SIZE = 3;
    }
    
    public interface NR30Bits {
        int DAC_POWER_BIT = 7;
    }
    public interface NR31Bits {
        int LENGTH_LOAD_START = 0, LENGTH_LOAD_SIZE = 8;
    }
    public interface NR32Bits {
        int VOLUME_CODE_START = 4, VOLUME_CODE_SIZE = 2;
    }
    
    public interface NR43Bits {
        int DIVISOR_CODE_START = 0, DIVISOR_CODE_SIZE = 3;
        int LFSR_WIDTH_MODE_BIT = 3;
        int CLOCK_SHIFT_START = 4, CLOCK_SHIFT_SIZE = 4;
    }
    
    public interface NR50Bits {
        int RIGHT_VOLUME_START = 0, RIGHT_VOLUME_SIZE = 3;
        int VIN_RIGHT_ENABLE_BIT = 3;
        int LEFT_VOLUME_START = 4, LEFT_VOLUME_SIZE = 3;
        int VIN_LEFT_ENABLE_BIT = 7;
    }
    public interface NR51Bits {
        int RIGHT_ENABLES_START = 0, RIGHT_ENABLES_SIZE = 4;
        int LEFT_ENABLES_START = 4, LEFT_ENABLES_SIZE = 4;
    }
    public interface NR52Bits {
        int CHANEL_LENGHT_STATUSES_START = 0, CHANEL_LENGHT_STATUSES_SIZE = 4;
        int POWER_CONTROL_BIT = 7;
    }
    
    private Sound() {
    }
}
