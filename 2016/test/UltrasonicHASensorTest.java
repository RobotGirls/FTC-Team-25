package test;/*
 * FTC Team 25: cmacfarl, March 04, 2016
 */

import com.qualcomm.hardware.modernrobotics.ModernRoboticsUsbLegacyModule;

import org.swerverobotics.library.interfaces.Autonomous;

import team25core.Robot;
import team25core.RobotEvent;
import team25core.Team25UltrasonicSensor;
import team25core.UltrasonicSensorHighAvailabilityTask;

@Autonomous(name="HA US Test")
public class UltrasonicHASensorTest extends Robot {

    Team25UltrasonicSensor left;
    Team25UltrasonicSensor right;
    ModernRoboticsUsbLegacyModule legacyModule;
    UltrasonicSensorHighAvailabilityTask usTask;
    double lastGoodDistance;

    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    @Override
    public void init()
    {
        legacyModule = (ModernRoboticsUsbLegacyModule)hardwareMap.legacyModule.get("LegacyModule");

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
        left.doPing();;
        lastGoodDistance = 0.0;
    }

    @Override
    public void start()
    {
        usTask = UltrasonicSensorHighAvailabilityTask.factory(this, left, right);
        addTask(usTask);
    }

    @Override
    public void loop()
    {
        double distance;

        super.loop();

        distance = usTask.getUltrasonicLevel();
        if ((distance == 0) || (distance == 255)) {
            if (lastGoodDistance != 0) {
                telemetry.addData("Distance: ", lastGoodDistance + "cm");
            }
            return;
        } else {
            telemetry.addData("Distance: ", distance + "cm");
        }
        lastGoodDistance = distance;
    }
}
