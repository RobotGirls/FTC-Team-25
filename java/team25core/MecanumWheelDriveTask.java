
package team25core;

/*
 * created by katie on 10/29/2016.
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;

public class MecanumWheelDriveTask extends RobotTask {
    protected Robot robot;
    protected DcMotor frontLeft;
    protected DcMotor frontRight;
    protected DcMotor rearLeft;
    protected DcMotor rearRight;

    public double fr;
    public double fl;
    public double rr;
    public double rl;
    public double slowMultiplier = 1;

    public MecanumWheelDriveTask(Robot robot, DcMotor frontLeft, DcMotor frontRight, DcMotor rearLeft, DcMotor rearRight)
    {
        super(robot);

        this.frontLeft = frontLeft;
        this.frontRight = frontRight;
        this.rearLeft = rearLeft;
        this.rearRight = rearRight;
        this.robot = robot;
    }

    private void getJoystick() {
        Gamepad gamepad;
        gamepad = robot.gamepad1;

        // If joysticks are pointed left, counter rotate wheels.
        // -1.0 assumes left temporarily.
        // threshold for joystick values in the x may vary.

        if (gamepad.left_stick_x > 0.5 && gamepad.right_stick_x > 0.5) {
            fl = -gamepad.left_stick_x;
            rl = gamepad.left_stick_x;
            fr = -gamepad.right_stick_x;
            rr = gamepad.right_stick_x;
        } else if (gamepad.left_stick_x < -0.5 && gamepad.right_stick_x < -0.5) {
            fl = -gamepad.left_stick_x;
            rl = gamepad.left_stick_x;
            fr = -gamepad.right_stick_x;
            rr = gamepad.right_stick_x;
        } else if (gamepad.right_trigger > 0.5) {
            fr = -1.0;
            rl = 1.0;
        } else if (gamepad.left_trigger > 0.5) {
            fl = 1.0;
            rr = -1.0;
        } else if (gamepad.left_bumper) {
            fr = 1.0;
            rl = -1.0;
        } else if (gamepad.right_bumper) {
            rr = 1.0;
            fl = -1.0;
        } else {
            fl = gamepad.left_stick_y;
            rl = gamepad.left_stick_y;
            fr = -gamepad.right_stick_y;
            rr = -gamepad.right_stick_y;
        }
    }

    @Override
    public void start()
    {
        // Nothing.
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
    public void stop()
    {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        getJoystick();

        frontLeft.setPower(fl * slowMultiplier);
        rearLeft.setPower(rl * slowMultiplier);
        frontRight.setPower(fr * slowMultiplier);
        rearRight.setPower(rr * slowMultiplier);
        return false;
    }

}
