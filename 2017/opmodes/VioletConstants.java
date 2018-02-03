package opmodes;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ReadWriteFile;

import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

import java.io.File;
import java.util.Locale;

/**
 * FTC Team 25: Created by Elizabeth Wu (updated by Breanna Chan) on 10/28/2017.
 */

public class VioletConstants extends LinearOpMode
{
    // Autonomous constants.
    public static final int TICKS_PER_INCH = 45;
    public static final int TICKS_PER_DEGREE = 22;
    public final static double STRAIGHT_SPEED = 0.7;
    public final static double TURN_SPEED = 0.7;

    private static double SERVO_DOMAIN = 256.0;

    public static double JEWEL_UP     = 164 / SERVO_DOMAIN;
    public static double JEWEL_DOWN   = 29  / SERVO_DOMAIN;
    public static double S1_INIT      = 10  / SERVO_DOMAIN;
    public static double S2_INIT      = 255 / SERVO_DOMAIN;
    public static double S3_INIT      = 224 / SERVO_DOMAIN;
    public static double S4_INIT      = 10  / SERVO_DOMAIN;
    public static double RELIC_INIT   = 116 / SERVO_DOMAIN;
    //public static double S1_OPEN      = 70  / SERVO_DOMAIN;
    public static double S1_OPEN      = 102 / SERVO_DOMAIN;
    public static double S1_CLOSED    = 151 / SERVO_DOMAIN;
    //public static double S2_OPEN      = 172 / SERVO_DOMAIN;
    public static double S2_OPEN      = 166 / SERVO_DOMAIN;
    public static double S2_CLOSED    = 120 / SERVO_DOMAIN;
    //public static double S3_OPEN      = 155 / SERVO_DOMAIN;
    public static double S3_OPEN      = 121 / SERVO_DOMAIN;
    public static double S3_CLOSED    = 85  / SERVO_DOMAIN;
    //public static double S4_OPEN      = 65  / SERVO_DOMAIN;
    public static double S4_OPEN      = 92  / SERVO_DOMAIN;
    public static double S4_CLOSED    = 140 / SERVO_DOMAIN;
    public static double RELIC_OPEN   = 0  / SERVO_DOMAIN;
    public static double RELIC_CLOSED = 100 / SERVO_DOMAIN;
    public static double RELIC_ROTATE_DOWN = 0 / SERVO_DOMAIN;
    public static double RELIC_ROTATE_UP   = 240 / SERVO_DOMAIN;

    //public static int DEGREES_180_CLOCKWISE = 375;
    //public static int DEGREES_180_COUNTERCLOCKWISE = 400;
    public static int DEGREES_180 = 475;
    public static double ROTATE_POWER = 0.3;
    public static int NUDGE = 15;
    public static double NUDGE_POWER = 0.1;
    //public static int VERTICAL_MIN_HEIGHT = 700;
    public static int VERTICAL_MIN_HEIGHT = 1500;
    public static int CLAW_VERTICAL = 700;
    public static double CLAW_VERTICAL_POWER = 0.75;
    // put on actual teleop in order to be able to change power in certain instances (Bella)
    // Hesitant initial values for relic slides.
    public static double RELIC_CEILING = 0.3;
    public static int RELIC_HORIZONTAL = 100; //FIXME
    public static double RELIC_HORIZONTAL_POWER = 0.5;

    public final static double MAX_TILT = 150;

    BNO055IMU imu;
    Orientation angles;
    Acceleration gravity;

    @Override public void runOpMode() {

        telemetry.log().setCapacity(12);
        telemetry.log().add("");
        telemetry.log().add("Please refer to the calibration instructions");
        telemetry.log().add("contained in the Adafruit IMU calibration");
        telemetry.log().add("sample opmode.");
        telemetry.log().add("");
        telemetry.log().add("When sufficient calibration has been reached,");
        telemetry.log().add("press the 'A' button to write the current");
        telemetry.log().add("calibration data to a file.");
        telemetry.log().add("");

        // We are expecting the IMU to be attached to an I2C port on a Core Device Interface Module and named "imu".
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit           = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit           = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.loggingEnabled = true;
        parameters.loggingTag     = "IMU";
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);

        composeTelemetry();
        telemetry.log().add("Waiting for start...");

        // Wait until we're told to go
        while (!isStarted()) {
            telemetry.update();
            idle();
        }

        telemetry.log().add("...started...");

        while (opModeIsActive()) {

            if (gamepad1.a) {

                // Get the calibration data
                BNO055IMU.CalibrationData calibrationData = imu.readCalibrationData();

                // Save the calibration data to a file. You can choose whatever file
                // name you wish here, but you'll want to indicate the same file name
                // when you initialize the IMU in an opmode in which it is used. If you
                // have more than one IMU on your robot, you'll of course want to use
                // different configuration file names for each.
                String filename = "AdafruitIMUCalibration.json";
                File file = AppUtil.getInstance().getSettingsFile(filename);
                ReadWriteFile.writeFile(file, calibrationData.serialize());
                telemetry.log().add("saved to '%s'", filename);

                // Wait for the button to be released
                while (gamepad1.a) {
                    telemetry.update();
                    idle();
                }
            }

            telemetry.update();
        }
    }

    void composeTelemetry() {

        // At the beginning of each telemetry update, grab a bunch of data
        // from the IMU that we will then display in separate lines.
        telemetry.addAction(new Runnable() { @Override public void run()
        {
            // Acquiring the angles is relatively expensive; we don't want
            // to do that in each of the three items that need that info, as that's
            // three times the necessary expense.
            angles   = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            gravity  = imu.getGravity();
        }
        });

        telemetry.addLine()
                .addData("status", new Func<String>() {
                    @Override public String value() {
                        return imu.getSystemStatus().toShortString();
                    }
                })
                .addData("calib", new Func<String>() {
                    @Override public String value() {
                        return imu.getCalibrationStatus().toString();
                    }
                });

        telemetry.addLine()
                .addData("heading", new Func<String>() {
                    @Override public String value() {
                        return formatAngle(angles.angleUnit, angles.firstAngle);
                    }
                })
                .addData("roll", new Func<String>() {
                    @Override public String value() {
                        //return formatAngle(angles.angleUnit, angles.secondAngle);
                        return formatAngle(angles.angleUnit, angles.thirdAngle);
                    }
                })
                .addData("pitch", new Func<String>() {
                    @Override public String value() {
                        //return formatAngle(angles.angleUnit, angles.thirdAngle);
                        return formatAngle(angles.angleUnit, angles.secondAngle);
                    }
                });

        telemetry.addLine()
                .addData("grvty", new Func<String>() {
                    @Override public String value() {
                        return gravity.toString();
                    }
                })
                .addData("mag", new Func<String>() {
                    @Override public String value() {
                        return String.format(Locale.getDefault(), "%.3f",
                                Math.sqrt(gravity.xAccel*gravity.xAccel
                                        + gravity.yAccel*gravity.yAccel
                                        + gravity.zAccel*gravity.zAccel));
                    }
                });
        telemetry.addLine()
                .addData("tilt", new Func<String>() {
                    @Override public String value() {
                        return String.format(Locale.getDefault(), "%.3f",
                                Math.toDegrees(Math.acos(Math.cos(Math.toRadians(angles.secondAngle) *
                                        Math.cos(Math.toRadians(angles.thirdAngle))))));
                    }
                });
    }

    //----------------------------------------------------------------------------------------------
    // Formatting
    //----------------------------------------------------------------------------------------------

    String formatAngle(AngleUnit angleUnit, double angle) {
        return formatDegrees(AngleUnit.DEGREES.fromUnit(angleUnit, angle));
    }

    String formatDegrees(double degrees){
        return String.format(Locale.getDefault(), "%.1f", AngleUnit.DEGREES.normalize(degrees));
    }
}
