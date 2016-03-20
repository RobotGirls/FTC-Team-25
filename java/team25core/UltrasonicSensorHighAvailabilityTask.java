package team25core;

/*
 * FTC Team 25: cmacfarl, February 26, 2016
 */

import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashSet;
import java.util.Set;

public class UltrasonicSensorHighAvailabilityTask extends UltrasonicSensorArbitratorTask
        implements IUltrasonicAveraging {

    Team25UltrasonicSensor active;
    Team25UltrasonicSensor primary;
    Team25UltrasonicSensor secondary;
    DescriptiveStatistics failureDetection;
    HashSet<Team25UltrasonicSensor> primarySet;
    HashSet<Team25UltrasonicSensor> secondarySet;

    public double getAverage() {
        // Not an average yet.
        double val;
        val = getUltrasonicLevel();

        return val;
    }

    public UltrasonicSensorHighAvailabilityTask(Robot robot, Team25UltrasonicSensor p, Team25UltrasonicSensor s)
    {
        super(robot, null);
        this.primarySet = new HashSet<Team25UltrasonicSensor>();
        this.primarySet.add(p);
        this.secondarySet = new HashSet<Team25UltrasonicSensor>();
        this.secondarySet.add(s);
        super.setSensors(primarySet);

        this.primary = p;
        this.secondary = s;
        this.failureDetection = new DescriptiveStatistics(50);
        this.active = primary;
    }

    public static UltrasonicSensorHighAvailabilityTask factory(Robot robot, Team25UltrasonicSensor primary,
                  Team25UltrasonicSensor secondary)
    {
        UltrasonicSensorHighAvailabilityTask task;

        task = new UltrasonicSensorHighAvailabilityTask(robot, primary, secondary);
        task.setFilterGarbage(false);
        return task;
    }

    public double getUltrasonicLevel()
    {
        double val;

        val = super.getUltrasonicLevel(active);
        failureDetection.addValue(val);

        if (failureDetection.getN() >= 50) {
            if ((failureDetection.getMean() == 0) || (failureDetection.getMean() == 255)) {
                if (active == primary) {
                    active = secondary;
                    super.setSensors(secondarySet);
                } else {
                    active = primary;
                    super.setSensors(primarySet);
                }
                failureDetection.clear();
                state = SensorState.PING;
            }
        }

        return val;
    }

    @Override
    public boolean timeslice()
    {
        super.timeslice();

        if (active == primary) {
            this.robot.telemetry.addData("Active: ", "Right");
        } else {
            this.robot.telemetry.addData("Active: ", "Left");
        }
        return false;
    }

}
