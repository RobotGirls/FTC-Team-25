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

    protected double leftWheelForward;
    protected double leftWheelBackward;
    protected double rightWheelForward;
    protected double rightWheelBackward;


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

        leftWheelForward   = -1;
        leftWheelBackward  = 1;
        rightWheelForward  = -1;
        rightWheelBackward = 1;

        // If left joystick are pointed left (negative joystick values), counter rotate wheels.
        // Threshold for joystick values in the x may vary.

        if (leftX > 0.5 && rightY > 0.5) {          // forward diagonal to the left
            fr = rightWheelForward;
            bl = leftWheelForward;
        } else if (leftX < -0.5 && rightY > 0.5) {          // forward diagonal to the right
            fl = leftWheelForward;
            br = rightWheelForward;
        } else if (leftX > 0.5 && rightY < -0.5) {          //backward diagonal left
            fl = leftWheelBackward;
            br = rightWheelBackward;
        } else if (leftX < -0.5 && rightY < -0.5) {          //backward diagonal right
            fr = rightWheelBackward;
            bl = leftWheelBackward;
        } else if (leftX > 0.5) {          // left joy-stick sideways; strafe left
            fl = leftWheelBackward;
            bl = leftWheelForward;
            fr = rightWheelForward;
            br = rightWheelBackward;
        } else if (leftX < -0.5) {          // left joy-stick sideways; strafe right
            fl = leftWheelForward;
            bl = leftWheelBackward;
            fr = rightWheelBackward;
            br = rightWheelForward;
        } else if (rightX > 0.5) {          // turn around toward left (right joystick left)
            fl = leftWheelBackward;
            bl = leftWheelBackward;
            fr = rightWheelForward;
            br = rightWheelForward;
        } else if (rightX < -0.5) {          // turn around toward right (right joystick right)
            fl = leftWheelForward;
            bl = leftWheelForward;
            fr = rightWheelBackward;
            br = rightWheelBackward;
        } else if (leftY > 0.5 || leftY < -0.5) {          //forward and backward (left joystick forward and backward)
            fl = leftY;
            bl = leftY;
            fr = leftY;
            br = leftY;
        }

        return new MotorValues(fl, fr, bl, br);
    }
}
