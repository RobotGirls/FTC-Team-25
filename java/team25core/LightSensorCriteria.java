package team25core;

/*
 * FTC Team 25: cmacfarl, February 18, 2016
 */

import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.util.RobotLog;

/*
 * Looks for a white line.
 */
public class LightSensorCriteria implements SensorCriteria {

    public enum LightPolarity {
        BLACK,
        WHITE,
    }

    protected int light;

    protected int min;
    protected int max;
    protected int threshold;
    protected LightSensor sensor;
    protected LightPolarity polarity;

    public LightSensorCriteria(LightSensor sensor, int min, int max)
    {
        this.sensor = sensor;
        this.polarity = LightPolarity.WHITE;
        this.min = min;
        this.max = max;
        this.threshold = (min + ((max - min)/2));
    }

    public LightSensorCriteria(LightSensor sensor, LightPolarity polarity, int min, int max)
    {
        this.sensor = sensor;
        this.polarity = polarity;
        this.min = min;
        this.max = max;
        this.threshold = (min + ((max - min)/2));
    }

    public void setThreshold(double percent) {
        this.threshold = (int)(max - ((max - min) * percent));
    }

    @Override
    public boolean satisfied()
    {
        RobotLog.i("251 Light: %d, Threshold: %d", sensor.getLightDetectedRaw(), threshold);
        if (polarity == LightPolarity.WHITE) {
            if (sensor.getLightDetectedRaw() < threshold) {
                return true;
            }
        } else if (polarity == LightPolarity.BLACK) {
            if (sensor.getLightDetectedRaw() > threshold) {
                return true;
            }
        }
        return false;
    }
}

