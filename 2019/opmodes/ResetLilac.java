package opmodes;
/*
 * FTC Team 25: elizabeth, December 21, 2018
 */

import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadReckonPath;
import team25core.DeadmanMotorTask;
import team25core.GamepadTask;
import team25core.MotorStallTask;
import team25core.Robot;
import team25core.RobotEvent;

public class ResetLilac extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;

    @Override
    public void init()
    {
        this.addTask(new DeadmanMotorTask(this, latchArm, 0.5, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.LEFT_BUMPER));
        this.addTask(new DeadmanMotorTask(this, latchArm, -0.5, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.LEFT_TRIGGER));
    }

    @Override
    public void start()
    {
        latchArm.setPower(0.2);
        this.addTask(new MotorStallTask(this, latchArm, telemetry) {
            @Override
            public void handleEvent(RobotEvent event)
            {
                latchArm.setPower(0.0);
            }

        });

    }

    @Override
    public void handleEvent(RobotEvent e) {

    }
}
