package test;

/*
 * FTC Team 25: cmacfarl, October 05, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadmanMotorTask;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;

public class DeadmanTest extends Robot {

    DcMotor motor;

    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    @Override
    public void init()
    {
        super.init();

        motor = hardwareMap.dcMotor.get("motor_1");
    }

    @Override
    public void start()
    {
        addTask(new DeadmanMotorTask(this, motor, 0.5, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.RIGHT_BUMPER));
        addTask(new DeadmanMotorTask(this, motor, -0.5, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.RIGHT_TRIGGER));
    }
}
