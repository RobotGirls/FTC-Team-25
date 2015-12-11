package team25core;

/*
 * FTC Team 5218: izzielau, December 10, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

public class ThrottleMap {
    Robot robot;
    DcMotor left;
    DcMotor right;

    public float rightPower;
    public float leftPower;

    public ThrottleMap(Robot robot, DcMotor leftMotor, DcMotor rightMotor) {
        this.left = leftMotor;
        this.right = rightMotor;
    }

    private static double logarithmicPower(double power) {
        boolean negative = false;

        // Convert power (decimal) to whole number.
        //power = power * 100;
        if (power < 0) {
            negative = true;
        } else if (power == 0) {
            return 0;
        }

        // Assign value of "e" to a variable.
        double e = Math.exp(1.0);

        double joystick = Math.abs(power);
        double returnPower = Math.pow(e, (4.6 * joystick)) / 100;
        if (negative) {
            return -1 * returnPower;
        } else {
            return returnPower;
        }
    }

    public void applyPower() {
        Gamepad gamepad;
        gamepad = robot.gamepad1;

        leftPower  = -gamepad.left_stick_y;
        rightPower = gamepad.right_stick_y;

        left.setPower(logarithmicPower(leftPower));
        right.setPower(logarithmicPower(rightPower));
    }
}