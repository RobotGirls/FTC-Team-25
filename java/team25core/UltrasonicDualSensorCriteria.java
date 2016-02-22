package team25core;

/*
 * FTC Team 25: cmacfarl, February 22, 2016
 */

import com.qualcomm.robotcore.hardware.UltrasonicSensor;

public class UltrasonicDualSensorCriteria implements SensorCriteria {

    UltrasonicAveragingTask left;
    UltrasonicAveragingTask right;
    int margin;

    UltrasonicDualSensorCriteria(UltrasonicAveragingTask left, UltrasonicAveragingTask right, int margin)
    {
        this.left = left;
        this.right = right;
        this.margin = margin;
    }

    UltrasonicDualSensorCriteria(UltrasonicAveragingTask left, UltrasonicAveragingTask right)
    {
        this.left = left;
        this.right = right;
        this.margin = 0;
    }

    @Override
    public boolean satisfied()
    {
        if (Math.abs(left.getAverage() - right.getAverage()) <= margin) {
            return true;
        } else {
            return false;
        }
    }
}
