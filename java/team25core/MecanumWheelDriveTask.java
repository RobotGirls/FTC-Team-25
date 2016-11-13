
package team25core;

/*
 * created by katie on 10/29/2016.
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

public class MecanumWheelDriveTask extends RobotTask {
    protected Robot robot;
    protected DcMotor frontLeft;
    protected DcMotor frontRight;
    protected DcMotor rearLeft;
    protected DcMotor rearRight;

    // ITF: better variables names can be used.
    public double fr;
    public double fl;
    public double rr;
    public double rl;

    public MecanumWheelDriveTask(Robot robot, DcMotor frontLeft, DcMotor frontRight, DcMotor rearLeft, DcMotor rearRight)
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
        Gamepad gamepad;
        gamepad = robot.gamepad1;

        // If joysticks are pointed left, counter rotate wheels.
        // +0.5 assumes left temporarily.
        // threshold for joystick values in the x may vary.
        // or? and?
        // which is better? I like this because you see that it's counter rotating, but it's kind of ugly...
        if (gamepad.left_stick_x > 0.5 && gamepad.right_stick_x > 0.5) {
            fl = gamepad.left_stick_x;
            rl = fl * -1;
            fr = gamepad.right_stick_x;
            rr = fr * -1;
        } else if (gamepad.left_stick_x < -0.5 && gamepad.right_stick_x < -0.5) {
            fl = -gamepad.left_stick_x;
            rl = gamepad.left_stick_x;
            fr = -gamepad.right_stick_x;
            rr = gamepad.right_stick_x;
        } else {
            fl = -gamepad.left_stick_y;
            rl = -gamepad.left_stick_y;
            fr = gamepad.left_stick_y;
            rr = gamepad.left_stick_y;
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

        frontLeft.setPower(fl);
        rearLeft.setPower(rl);
        frontRight.setPower(fr);
        rearRight.setPower(rr);
        return false;
    }

}