package team25core;

/*
 * FTC Team 25: cmacfarl, February 18, 2016
 */

import com.qualcomm.robotcore.hardware.LightSensor;

/*
 * Looks for a white line.
 */
public class LightSensorCriteria implements SensorCriteria {

    protected int min;
    protected int max;
    protected int threshold;
    protected LightSensor sensor;

    public LightSensorCriteria(LightSensor sensor, int min, int max)
    {
        this.sensor = sensor;
        this.min = min;
        this.max = max;
        this.threshold = (min + ((max - min)/2));
    }

    @Override
    public boolean satisfied()
    {
        if (sensor.getLightDetectedRaw() < threshold) {
            return true;
        } else {
            return false;
        }
    }
}
