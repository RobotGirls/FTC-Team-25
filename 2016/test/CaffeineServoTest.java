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
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {
        rightPusher = hardwareMap.servo.get("rightPusher");

        rightBumper = hardwareMap.servo.get("rightBumper");
        leftBumper = hardwareMap.servo.get("leftBumper");

        rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
        leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
    }

    @Override
    public void start() {
        ServoCalibrateTask calibrate = new ServoCalibrateTask(this, rightPusher);
        addTask(calibrate);
    }

}
