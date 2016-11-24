
package team25core;

/*
 * FTC Team 25: izzielau, September 27, 2015
 */

import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.Gamepad;

public class FourWheelDriveTask extends RobotTask {
    protected Robot robot;
    protected DcMotor frontLeft;
    protected DcMotor frontRight;
    protected DcMotor rearLeft;
    protected DcMotor rearRight;

    public double right;
    public double left;
    public double slowMultiplier = 1;

    public FourWheelDriveTask(Robot robot, DcMotor frontLeft, DcMotor frontRight, DcMotor rearLeft, DcMotor rearRight)
    {
        super(robot);

        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.rearLeft = rearLeft;
        this.rearRight = rearRight;
        this.robot = robot;
    }

    private void getJoystick()
    {
        Gamepad gamepad = robot.gamepad1;

        left  = -gamepad.left_stick_y * slowMultiplier;
        right = gamepad.right_stick_y * slowMultiplier;
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

        frontLeft.setPower(left);
        rearLeft.setPower(left);
        frontRight.setPower(right);
        rearRight.setPower(right);
        return false;
    }



}