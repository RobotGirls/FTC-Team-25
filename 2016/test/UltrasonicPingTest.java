package test;

/*
 * FTC Team 25: cmacfarl, February 25, 2016
 */

import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbLegacyModule;
import com.qualcomm.robotcore.util.RobotLog;

import org.swerverobotics.library.interfaces.Autonomous;

import java.util.HashSet;

import team25core.Robot;
import team25core.RobotEvent;
import team25core.Team25UltrasonicSensor;
import team25core.UltrasonicAveragingTask;
import team25core.UltrasonicSensorArbitratorTask;

@Autonomous(name = "TEST Ping")
public class UltrasonicPingTest extends Robot {

    Team25UltrasonicSensor left;
    Team25UltrasonicSensor right;
    ModernRoboticsUsbLegacyModule legacyModule;
    UltrasonicSensorArbitratorTask arbitrator;
    UltrasonicAveragingTask leftAveraging;
    UltrasonicAveragingTask rightAveraging;

    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    @Override
    public void init()
    {
        legacyModule = (ModernRoboticsUsbLegacyModule)hardwareMap.legacyModule.get("legacy");

        left = new Team25UltrasonicSensor(legacyModule, 5);
        right = new Team25UltrasonicSensor(legacyModule, 4);
        /*
         * Make sure one of them is turned off.  This won't happen unless
         * we do a single ping as the default state for the sensor is on.
         * The ping will turn it off (see the datasheet).
         *
         * As long as a second sensor is connected and we only want to test one of
         * them this is necessary.
         */
        // left.doPing();;

    }

    @Override
    public void start()
    {
        HashSet<Team25UltrasonicSensor> sensors = new HashSet<Team25UltrasonicSensor>();
        sensors.add(left);
        sensors.add(right);
        /*
         * You can add a second sensor here via sensors.add(right), and also create a new averaging
         * task for it below.
         */

        arbitrator = new UltrasonicSensorArbitratorTask(this, sensors);
        addTask(arbitrator);
        leftAveraging = new UltrasonicAveragingTask(this, arbitrator, left, 3);
        addTask(leftAveraging);
        rightAveraging = new UltrasonicAveragingTask(this, arbitrator, right, 3);
        addTask(rightAveraging);
    }

    @Override
    public void loop()
    {
        super.loop();

        telemetry.addData("Left Distance ", leftAveraging.getAverage());
        telemetry.addData("Right Distance ", rightAveraging.getAverage());
    }
}
