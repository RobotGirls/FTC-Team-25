package team25core;

/*
 * FTC Team 25: cmacfarl, February 26, 2016
 */

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import java.util.HashSet;
import java.util.Set;

public class UltrasonicSensorHighAvailabilityTask extends UltrasonicSensorArbitratorTask {

    Team25UltrasonicSensor active;
    Team25UltrasonicSensor primary;
    Team25UltrasonicSensor secondary;
    DescriptiveStatistics failureDetection;
    HashSet<Team25UltrasonicSensor> primarySet;
    HashSet<Team25UltrasonicSensor> secondarySet;

    private UltrasonicSensorHighAvailabilityTask(Robot robot, Team25UltrasonicSensor p, Team25UltrasonicSensor s)
    {
        super(robot, null);
        this.primarySet = new HashSet<Team25UltrasonicSensor>();
        this.primarySet.add(primary);
        this.secondarySet = new HashSet<Team25UltrasonicSensor>();
        this.secondarySet.add(primary);
        super.setSensors(primarySet);

        this.primary = p;
        this.secondary = s;
        this.failureDetection = new DescriptiveStatistics(50);
        this.active = primary;
    }

    public static UltrasonicSensorHighAvailabilityTask factory(Robot robot, Team25UltrasonicSensor primary,
                  Team25UltrasonicSensor secondary)
    {
        return new UltrasonicSensorHighAvailabilityTask(robot, primary, secondary);
    }

    public double getUltrasonicLevel()
    {
        double val;

        val = super.getUltrasonicLevel(primary);
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
            }
        }

        return super.getUltrasonicLevel(active);
    }
}
