
package team25core;

/*
 * FTC Team 5218: izzielau, September 27, 2015
 */

import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.Gamepad;

public class TwoWheelDriveTask extends RobotTask
{
    protected Robot robot;
    protected DcMotor motorRight;
    protected DcMotor motorLeft;

    public double right;
    public double left;

    public boolean slow = false;

    public double slowMultiplier = 0.5;

    public TwoWheelDriveTask(Robot robot, DcMotor rightMotor, DcMotor leftMotor)
    {
        super(robot);

        this.motorRight = rightMotor;
        this.motorLeft = leftMotor;
        this.robot = robot;
    }

    private void getJoystick()
    {
        Gamepad gamepad = robot.gamepad1;

        left = -gamepad.left_stick_y * slowMultiplier;
        right = -gamepad.right_stick_y * slowMultiplier;
    }

    public void slowDown(boolean slow)
    {
        if (slow) {
            slowMultiplier = 0.5;
        } else {
            slowMultiplier = 1;
        }
    }

    public void slowDown(double mult)
    {
        slowMultiplier = mult;
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
        motorLeft.setPower(left);
        motorRight.setPower(right);

        if (slow) {
            robot.telemetry.addData("Slow: ", "true");
        } else {
            robot.telemetry.addData("Slow: ", "false");
        }

        robot.telemetry.addData("L: ", left);
        robot.telemetry.addData("R: ", right);

        return false;
    }

}
