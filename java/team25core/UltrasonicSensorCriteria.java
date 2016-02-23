package team25core;

import com.qualcomm.robotcore.hardware.UltrasonicSensor;
import com.qualcomm.robotcore.util.RobotLog;

/**
 * Created by Izzie on 2/22/2016.
 */
public class UltrasonicSensorCriteria implements SensorCriteria {

    private int distance;

    UltrasonicAveragingTask average;

    public UltrasonicSensorCriteria(UltrasonicAveragingTask average, int distance)
    {
        this.average = average;
        this.distance = distance;
    }

    @Override
    public boolean satisfied()
    {
        if (average.getAverage() <= distance) {
            return true;
        } else {
            return false;
        }
    }
}

