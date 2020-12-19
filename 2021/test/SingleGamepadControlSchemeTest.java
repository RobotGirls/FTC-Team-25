package test;

import com.qualcomm.robotcore.hardware.Gamepad;

import team25core.JoystickDriveControlScheme;
import team25core.MotorValues;

/*
Modified version of TankMechanumControlSchemeReverse, which was created by Breanna Chan.

Specifications:
must use left joystick to steer, right joystick to turn
 */

public class SingleGamepadControlSchemeTest implements JoystickDriveControlScheme {

    protected double fr; //front right motor
    protected double fl; //front left motor
    protected double br; //back right motor
    protected double bl; //back left motor
    protected double leftX;
    protected double rightX;
    protected double leftY;
    protected double rightY;
    protected Gamepad gamepad;

    //for use in cases where EITHER the left wheels or right wheels are mounted backward.
    //currently: left wheels are mounted backward, so the left wheel forward/backward values are reversed from normal
    protected final double LEFT_WHEEL_FORWARD = -1;
    protected double LEFT_WHEEL_BACKWARD = 1;
    protected double RIGHT_WHEEL_FORWARD = 1;
    protected double RIGHT_WHEEL_BACKWARD = -1;


    public SingleGamepadControlSchemeTest(Gamepad gamepad)
    {
        this.gamepad = gamepad;
    }

    public MotorValues getMotorPowers()
    {
        leftX = gamepad.left_stick_x;
        rightX = gamepad.right_stick_x;
        leftY = gamepad.left_stick_y;
        rightY = gamepad.right_stick_y;

        // If left joystick are pointed left (negative joystick values), counter rotate wheels.
        // Threshold for joystick values in the x may vary.

        if (leftX > 0.5) {          // left joystick to the right
            fl = 1;
            bl = -1;
            fr = 1;
            br = -1;
        } else if (leftX < -0.5) {          //left joystick to the left
            fl = -1;
            bl = 1;
            fr = -1;
            br = 1;
        } else if (rightX > 0.5) {          //right joystick right (turn right)
            fl = 1;
            bl = 1;
            fr = 1;
            br = 1;
        } else if (rightX < -0.5) {          // right joystick left (turn left)
            fl = -1;
            bl = -1;
            fr = -1;
            br = -1;
        } else if (leftY < -0.5) { //left joystick forward
            fl = 1;
            bl = 1;
            fr = -1;
            br = -1;
        } else if (leftY > 0.5) { //left joystick backward
            fl = -1;
            bl = -1;
            fr = 1;
            br = 1;
        } else {          //forward and backward (left joystick forward and backward)
            //backwards

            fl = 0;
            bl = 0;
            fr = 0;
            br = 0;
        }

        return new MotorValues(fl, fr, bl, br);
    }
}
