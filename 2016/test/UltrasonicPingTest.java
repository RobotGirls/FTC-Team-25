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

    Team25UltrasonicSensor sensor;
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

        sensor = new Team25UltrasonicSensor(legacyModule, 4);

    }

    @Override
    public void start()
    {
        HashSet<Team25UltrasonicSensor> sensors = new HashSet<Team25UltrasonicSensor>();
        sensors.add(sensor);

        arbitrator = new UltrasonicSensorArbitratorTask(this, sensors);
        addTask(arbitrator);
        averaging = new UltrasonicAveragingTask(this, arbitrator, sensor, 5);
        addTask(averaging);
    }

    @Override
    public void loop()
    {
        super.loop();

        telemetry.addData("Distance ", averaging.getAverage());
    }
}
