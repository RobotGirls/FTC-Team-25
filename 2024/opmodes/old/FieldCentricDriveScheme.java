package opmodes;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.JoystickDriveControlScheme;
import team25core.MotorValues;

/**
 * Created by Ruchi Bondre on 9/3/22.
 */


public class FieldCentricDriveScheme implements JoystickDriveControlScheme {

    /*
     * An Andymark 40 native spin direction is counterclockwise.
     */
    public enum DriveType {
        DRIVE_GEARED,
        DRIVE_DIRECT,
    };

    public enum MotorPosition {
        OUTER_OPPOSED,
        INNER_OPPOSED,
    };

    public enum MotorDirection {
        CANONICAL,
        NONCANONICAL,
    };


    protected double rx;
    protected double x;
    protected double y;

    protected BNO055IMU imu;

    protected Gamepad gamepad;
    protected MotorDirection motorDirection;
    private Telemetry telemetry;
    private Telemetry.Item fLpowerTlm;
    private Telemetry.Item bLpowerTlm;
    private Telemetry.Item fRpowerTlm;
    private Telemetry.Item bRpowerTlm;




    public FieldCentricDriveScheme(Gamepad gamepad)
    {
        this.gamepad = gamepad;
        this.motorDirection = MotorDirection.CANONICAL;

    }

    public FieldCentricDriveScheme(Gamepad gamepad, MotorDirection motorDirection, BNO055IMU imu)
    {
        this.gamepad = gamepad;
        this.motorDirection = motorDirection;
        this.imu = imu;
        this.telemetry = telemetry;
    }

    public MotorValues getMotorPowers()
    {
        y = -gamepad.left_stick_y; // Remember, this is reversed!
        x = gamepad.left_stick_x * 1.1; // Counteract imperfect strafing
        rx = gamepad.right_stick_x;

        // If joysticks are pointed left (negative joystick values), counter rotate wheels.
        // Threshold for joystick values in the x may vary.

        // Read inverse IMU heading, as the IMU heading is CW positive
        double botHeading = -imu.getAngularOrientation().firstAngle;

        double rotX = x * Math.cos(botHeading) - y * Math.sin(botHeading);
        double rotY = x * Math.sin(botHeading) + y * Math.cos(botHeading);

        // Denominator is the largest motor power (absolute value) or 1
        // This ensures all the powers maintain the same ratio, but only when
        // at least one is out of the range [-1, 1]
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);
        double frontLeftPower = (rotY + rotX + rx) / denominator;
        double backLeftPower = (rotY - rotX + rx) / denominator;
        double frontRightPower = (rotY - rotX - rx) / denominator;
        double backRightPower = (rotY + rotX - rx) / denominator;

        fLpowerTlm.setValue(frontLeftPower);
        fRpowerTlm.setValue(frontRightPower);
        bLpowerTlm.setValue(backLeftPower);
        bRpowerTlm.setValue(backRightPower);

        return new MotorValues(frontLeftPower, frontRightPower, backLeftPower, backRightPower);

    }
}
