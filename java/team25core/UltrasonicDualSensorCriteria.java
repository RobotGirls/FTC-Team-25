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
    Team25UltrasonicSensor leftSensor;
    Team25UltrasonicSensor rightSensor;
    UltrasonicSensorArbitratorTask arbitrator;
    int margin;
    ElapsedTime et = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

    public UltrasonicDualSensorCriteria(UltrasonicAveragingTask left, UltrasonicAveragingTask right, int margin)
    {
        this.left = left;
        this.right = right;
        this.margin = margin;
        this.arbitrator = null;
    }

    public UltrasonicDualSensorCriteria(UltrasonicAveragingTask left, UltrasonicAveragingTask right)
    {
        this.left = left;
        this.right = right;
        this.margin = 0;
        this.arbitrator = null;
    }

    public UltrasonicDualSensorCriteria(UltrasonicSensorArbitratorTask arbitrator, Team25UltrasonicSensor left, Team25UltrasonicSensor right,
                                        int margin)
    {
        this.leftSensor = left;
        this.rightSensor = right;
        this.margin = margin;
        this.arbitrator = arbitrator;
    }

    @Override
    public boolean satisfied()
    {
        double leftVal;
        double rightVal;

        if (arbitrator != null) {
            leftVal = arbitrator.getUltrasonicLevel(leftSensor);
            rightVal = arbitrator.getUltrasonicLevel(rightSensor);
        } else {
            leftVal = left.getAverage();
            rightVal = right.getAverage();
        }

        if (et.time() >= 200) {
            RobotLog.i("251 Left %3.1f, right %3.1f", leftVal, rightVal);
            et.reset();
        }


        if (Math.abs(leftVal - rightVal) <= margin) {
            RobotLog.i("251 Ultrasonic satisfied: Left %3.1f, right %3.1f", left.getAverage(), right.getAverage());
            return true;
        } else {
            return false;
        }
    }
}
