package team25core;

/*
 * FTC Team 25: cmacfarl, February 22, 2016
 */

import com.qualcomm.robotcore.hardware.UltrasonicSensor;
import com.qualcomm.robotcore.util.RobotLog;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class UltrasonicAveragingTask extends RobotTask {

    protected int setSize;
    DescriptiveStatistics movingAvg;
    UltrasonicSensor sensor;
    protected final double ULTRASONIC_MAX = 255.0;
    protected double min;

    public UltrasonicAveragingTask(Robot robot, UltrasonicSensor sensor, int setSize)
    {
        super(robot);
        this.setSize = setSize;
        this.movingAvg = new DescriptiveStatistics(setSize);
        this.sensor = sensor;
        this.min = ULTRASONIC_MAX;
    }

    public double getAverage()
    {
        return movingAvg.getMean();
    }

    @Override
    public void start()
    {

    }

    @Override
    public void stop()
    {

    }

    public void resetMin()
    {
        min = ULTRASONIC_MAX;
    }

    public double getMin()
    {
        return min;
    }

    @Override
    public boolean timeslice()
    {
        double val = sensor.getUltrasonicLevel();

        if ((val == 0) || (val == 255)) {
            return false;
        }

        if (val < min) {
            min = val;
            // RobotLog.i(sensor.getConnectionInfo() + " min %3.1f", min);
        }

        robot.telemetry.addData("Distance " + sensor.getConnectionInfo(), val);
        movingAvg.addValue(sensor.getUltrasonicLevel());

        /*
         * Never stops
         */
        return false;
    }
}

