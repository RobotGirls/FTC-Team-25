package team25core;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

/**
 * FTC Team 25: Created by Katelyn on 1/5/2016.
 */
public class RangeSensorCriteria implements SensorCriteria {

    private double max;

    DistanceSensor range;

    public RangeSensorCriteria(DistanceSensor range, int max)
    {
        this.range = range;
        this.max = max;
    }

    @Override
    public boolean satisfied()
    {
        double distance = range.getDistance(DistanceUnit.CM);
        RobotLog.i("251 Distance %f", distance);

        if (distance <= max) {
            return true;
        } else {
            return false;
        }
    }
}

