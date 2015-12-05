
package team25core;

/*
 * FTC Team 5218: izzielau, September 27, 2015
 */

import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.Gamepad;

public class TwoMotorDriveTask extends RobotTask {
    protected Robot robot;
    protected DcMotor motorRight;
    protected DcMotor motorLeft;

    public float right;
    public float left;

    public TwoMotorDriveTask(Robot robot, DcMotor rightMotor, DcMotor leftMotor)
    {
        super(robot);

        this.motorRight = rightMotor;
        this.motorLeft = leftMotor;
        this.robot = robot;
    }

    private void getJoystick()
    {
        Gamepad gamepad;
        gamepad = robot.gamepad1;

        left  = gamepad.left_stick_y;
        right = gamepad.right_stick_y;
    }

    @Override
    public void start()
    {
        // Nothing.
    }

    @Override
    public void stop()
    {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        getJoystick();

        double leftPowerValue = Math.exp((4.6 * left) / 100);
        double rightPowerValue = Math.exp((4.6 * right) / 100);

        motorLeft.setPower(leftPowerValue);
        motorRight.setPower(rightPowerValue);

        return false;
    }

}