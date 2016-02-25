package test;

/*
 * FTC Team 5218: izzielau, January 15, 2015
 */

import com.qualcomm.robotcore.hardware.Servo;

//import org.swerverobotics.library.interfaces.TeleOp;

import org.swerverobotics.library.interfaces.TeleOp;

import opmodes.NeverlandServoConstants;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.ServoCalibrateTask;

@TeleOp(name = "TEST Servo", group = "AutoTest")
public class CaffeineServoTest extends Robot {

    private Servo servo;
    private ServoCalibrateTask servoTask;

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {
        servo = hardwareMap.servo.get("servo");
    }

    @Override
    public void start() {
        ServoCalibrateTask calibrate = new ServoCalibrateTask(this, servo);
        addTask(calibrate);
    }

}
