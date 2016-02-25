package team25core;

/*
 * FTC Team 25: cmacfarl, February 22, 2016
 */

import com.qualcomm.robotcore.hardware.UltrasonicSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

public class UltrasonicDualSensorCriteria implements SensorCriteria {

    UltrasonicAveragingTask left;
    UltrasonicAveragingTask right;
    int margin;
    ElapsedTime et = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

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
        if (et.time() >= 200) {
            RobotLog.i("251 Left %3.1f, right %3.1f", left.getAverage(), right.getAverage());
            et.reset();
        }

        if (Math.abs(left.getAverage() - right.getAverage()) <= margin) {
            RobotLog.i("251 Ultrasonic satisfied: Left %3.1f, right %3.1f", left.getAverage(), right.getAverage());
            return true;
        } else {
            return false;
        }
    }
}
