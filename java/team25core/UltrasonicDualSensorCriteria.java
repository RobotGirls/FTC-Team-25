package team25core;

/*
 * FTC Team 25: cmacfarl, February 22, 2016
 */

import com.qualcomm.robotcore.hardware.UltrasonicSensor;
import com.qualcomm.robotcore.util.RobotLog;

public class UltrasonicDualSensorCriteria implements SensorCriteria {

    UltrasonicAveragingTask left;
    UltrasonicAveragingTask right;
    int margin;

    public UltrasonicDualSensorCriteria(UltrasonicAveragingTask left, UltrasonicAveragingTask right, int margin)
    {
        this.left = left;
        this.right = right;
        this.margin = margin;
    }

    public UltrasonicDualSensorCriteria(UltrasonicAveragingTask left, UltrasonicAveragingTask right)
    {
        this.left = left;
        this.right = right;
        this.margin = 0;
    }

    @Override
    public boolean satisfied()
    {
        RobotLog.i("251 Right avg: %d", (int)right.getAverage());
        RobotLog.i("251 Left avg: %d", (int)left.getAverage());

        if (Math.abs(left.getAverage() - right.getAverage()) <= margin) {
            return true;
        } else {
            return false;
        }
    }
}
