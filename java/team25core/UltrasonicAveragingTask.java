package team25core;

/*
 * FTC Team 25: cmacfarl, February 22, 2016
 */

import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

public class UltrasonicAveragingTask extends RobotTask {

    protected int setSize;
    DescriptiveStatistics movingAvg;
    UltrasonicSensor sensor;

    public UltrasonicAveragingTask(Robot robot, UltrasonicSensor sensor, int setSize)
    {
        super(robot);
        this.setSize = setSize;
        this.movingAvg = new DescriptiveStatistics(setSize);
        this.sensor = sensor;
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

    @Override
    public boolean timeslice()
    {
        double val = sensor.getUltrasonicLevel();

        if ((val == 0) || (val == 255)) {
            return false;
        }

        movingAvg.addValue(sensor.getUltrasonicLevel());

        /*
         * Never stops
         */
        return false;
    }
}

