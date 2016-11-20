
package team25core;

/*
 * FTC Team 25: izzielau, September 27, 2015
 */

import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.Gamepad;

public class FourWheelDriveTask extends RobotTask {
    protected Robot robot;
    protected DcMotor motorOne;
    protected DcMotor motorTwo;
    protected DcMotor motorThree;
    protected DcMotor motorFour;

    public double right;
    public double left;
    public double slowMultiplier = 1;

    public FourWheelDriveTask(Robot robot, DcMotor motorOne, DcMotor motorTwo, DcMotor motorThree, DcMotor motorFour)
    {
        super(robot);

        this.motorOne = motorOne;
        this.motorTwo = motorTwo;
        this.motorThree = motorThree;
        this.motorFour = motorFour;
        this.robot = robot;
    }

    private void getJoystick()
    {
        Gamepad gamepad;
        gamepad = robot.gamepad1;

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

        motorOne.setPower(left);
        motorThree.setPower(left);
        motorTwo.setPower(right);
        motorFour.setPower(right);
        return false;
    }



}