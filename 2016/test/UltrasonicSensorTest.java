package test;

import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import org.swerverobotics.library.interfaces.Autonomous;

import team25core.MonitorUltrasonicSensorTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by Izzie on 2/20/2016.
 */
@Autonomous(name = "TEST Ultrasonic")
public class UltrasonicSensorTest extends Robot {
    private UltrasonicSensor leftSound;
    private UltrasonicSensor rightSound;

    private MonitorUltrasonicSensorTask monitorLeft;
    private MonitorUltrasonicSensorTask monitorRight;

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {
        leftSound = hardwareMap.ultrasonicSensor.get("leftSound");
        rightSound = hardwareMap.ultrasonicSensor.get("rightSound");

        monitorLeft = new MonitorUltrasonicSensorTask(this, leftSound);
        addTask(monitorLeft);

        monitorRight = new MonitorUltrasonicSensorTask(this, rightSound);
        addTask(monitorRight);
    }

}
