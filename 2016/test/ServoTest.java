package test;

/*
 * FTC Team 5218: izzielau, December 05, 2015
 */

import com.qualcomm.robotcore.hardware.Servo;

import opmodes.NeverlandServoConstants;
import team25core.RobotEvent;
import team25core.ServoCalibrateTask;

public class ServoTest extends team25core.Robot {
    Servo servo;
    Servo rightPusher;
    Servo rightFlag;
    Servo leftPusher;
    Servo leftFlag;

    @Override
    public void handleEvent(RobotEvent e)
    {
        // None.
    }

    @Override
    public void init() {
        rightPusher = hardwareMap.servo.get("rightServo");
        leftPusher = hardwareMap.servo.get("leftServo");
        rightFlag = hardwareMap.servo.get("rightFlag");
        leftFlag = hardwareMap.servo.get("leftFlag");

        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        rightFlag.setPosition(NeverlandServoConstants.RIGHT_FLAG_NINETY);
        leftFlag.setPosition(NeverlandServoConstants.LEFT_FLAG_NINETY);

        servo.getController().pwmEnable();
    }

    @Override
    public void start() {
        ServoCalibrateTask servoTask = new ServoCalibrateTask(this, servo);
        addTask(servoTask);
    }

    @Override
    public void stop() {

    }
}
