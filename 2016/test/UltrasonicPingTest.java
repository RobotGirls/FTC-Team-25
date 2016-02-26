package test;

/*
 * FTC Team 25: cmacfarl, February 25, 2016
 */

import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbLegacyModule;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.HashSet;

import team25core.Robot;
import team25core.RobotEvent;
import team25core.Team25UltrasonicSensor;
import team25core.UltrasonicAveragingTask;
import team25core.UltrasonicSensorArbitratorTask;

public class UltrasonicPingTest extends Robot {

    Team25UltrasonicSensor left;
    Team25UltrasonicSensor right;
    ModernRoboticsUsbLegacyModule legacyModule;
    UltrasonicSensorArbitratorTask arbitrator;
    UltrasonicAveragingTask averaging;

    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    @Override
    public void init()
    {
        legacyModule = (ModernRoboticsUsbLegacyModule)hardwareMap.legacyModule.get("legacy");

        left = new Team25UltrasonicSensor(legacyModule, 4);
        right = new Team25UltrasonicSensor(legacyModule, 5);
        /*
         * Make sure one of them is turned off.  This won't happen unless
         * we do a single ping as the default state for the sensor is on.
         * The ping will turn it off (see the datasheet).
         *
         * As long as a second sensor is connected and we only want to test one of
         * them this is necessary.
         */
        right.doPing();;

    }

    @Override
    public void start()
    {
        HashSet<Team25UltrasonicSensor> sensors = new HashSet<Team25UltrasonicSensor>();
        sensors.add(left);
        /*
         * You can add a second sensor here via sensors.add(right), and also create a new averaging
         * task for it below.
         */

        arbitrator = new UltrasonicSensorArbitratorTask(this, sensors);
        addTask(arbitrator);
        averaging = new UltrasonicAveragingTask(this, arbitrator, left, 5);
        addTask(averaging);
    }

    @Override
    public void loop()
    {
        super.loop();

        telemetry.addData("Distance ", averaging.getAverage());
    }
}
