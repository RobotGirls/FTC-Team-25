
package team25core;

/*
 * FTC Team 5218: izzielau, September 27, 2015
 */

import com.qualcomm.robotcore.hardware.*;
import com.qualcomm.robotcore.hardware.Gamepad;

public class TwoWheelDriveTask extends RobotTask {
    protected Robot robot;
    protected DcMotor motorRight;
    protected DcMotor motorLeft;

    public float right;
    public float left;

    public boolean slow = false;

    public TwoWheelDriveTask(Robot robot, DcMotor rightMotor, DcMotor leftMotor) {
        super(robot);

        this.motorRight = rightMotor;
        this.motorLeft = leftMotor;
        this.robot = robot;
    }

    private void getJoystick() {
        Gamepad gamepad;
        gamepad = robot.gamepad1;

        left = -gamepad.left_stick_y;
        right = -gamepad.right_stick_y;
    }

    @Override
    public void start() {
        // Nothing.
    }

    @Override
    public void stop() {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice() {
        getJoystick();

        double leftPowerValue = left;
        double rightPowerValue = right;

        if (slow) {
            robot.telemetry.addData("Slow: ", "true");

            double alteredLeftPower = leftPowerValue / 10;
            double alteredRightPower = rightPowerValue / 10;
            motorLeft.setPower(alteredLeftPower);
            motorRight.setPower(alteredRightPower);

            robot.telemetry.addData("L: ", alteredLeftPower);
            robot.telemetry.addData("R: ", alteredRightPower);
        } else {
            robot.telemetry.addData("Slow: ", "false");
            motorLeft.setPower(leftPowerValue);
            motorRight.setPower(rightPowerValue);

            robot.telemetry.addData("L: ", leftPowerValue);
            robot.telemetry.addData("R: ", rightPowerValue);
        }

        return false;
    }

    public void slow(boolean on) {
        slow = on;
    }
}