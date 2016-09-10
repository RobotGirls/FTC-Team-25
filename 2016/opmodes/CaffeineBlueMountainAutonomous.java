package opmodes;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;

import opmodes.NeverlandServoConstants;
import team25core.Robot;
import team25core.RobotEvent;

/*
 * FTC Team 5218: izzielau, November 02, 2015
 */

@Autonomous(name="BLUE Mountain", group = "AutoTeam25")
@Disabled
public class CaffeineBlueMountainAutonomous extends OpMode {

    public static final int TICKS_PER_INCH = NeverlandMotorConstants.ENCODER_TICKS_PER_INCH;
    public static final int TICKS_PER_DEGREE = NeverlandMotorConstants.ENCODER_TICKS_PER_DEGREE;

    private DcMotorController mc;
    private DcMotor leftTread;
    private DcMotor rightTread;
    private DcMotor hook;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;
    private int state = 1;

    @Override
    public void init() {
        // Treads.
        rightTread = hardwareMap.dcMotor.get("rightTread");
        leftTread = hardwareMap.dcMotor.get("leftTread");

        leftTread.setDirection(DcMotor.Direction.REVERSE);

        rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);

        // Servos.
        rightPusher = hardwareMap.servo.get("rightPusher");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightBumper = hardwareMap.servo.get("rightBumper");
        leftBumper = hardwareMap.servo.get("leftBumper");

        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
        leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
    }

    @Override
    public void loop () {

        telemetry.addData("State: ", state);
        switch(state) {
            // Case 1 - 4: Move 62.5 inches forward.
            case 1:
                leftTread.setPower(0.5);
                rightTread.setPower(0.5);
                if (Math.abs(leftTread.getCurrentPosition()) >= (62.5 * TICKS_PER_INCH)) {
                    state = 2;
                }
                break;
            case 2:
                leftTread.setPower(0.0);
                rightTread.setPower(0.0);
                state = 3;
                break;
            case 3:
                leftTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                rightTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                state = 4;
                break;
            case 4:
                if ((leftTread.getCurrentPosition() == 0) && (rightTread.getCurrentPosition() == 0)) {
                    state = 13;
                    leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                    rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                }
                break;
            // Case 5 - 9: Turn 90 degrees.
            case 5:
                leftTread.setPower(0.251);
                rightTread.setPower(0.251);
                if (Math.abs(leftTread.getCurrentPosition()) >= (90 * TICKS_PER_DEGREE)) {
                    state = 6;
                }
                break;
            case 6:
                leftTread.setPower(0.0);
                rightTread.setPower(0.0);
                state = 7;
                break;
            case 7:
                leftTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                rightTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                state = 8;
                break;
            case 8:
                if ((leftTread.getCurrentPosition() == 0) && (rightTread.getCurrentPosition() == 0)) {
                    state = 9;
                    leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                    rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                }
                break;
            // Case 9 - 12: Move 20 inches forward.
            case 9:
                leftTread.setPower(0.5);
                rightTread.setPower(0.5);
                if (Math.abs(leftTread.getCurrentPosition()) >= (20 * TICKS_PER_INCH)) {
                    state = 10;
                }
                break;
            case 10:
                leftTread.setPower(0.0);
                rightTread.setPower(0.0);
                state = 11;
                break;
            case 11:
                leftTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                rightTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                state = 12;
                break;
            case 12:
                if ((leftTread.getCurrentPosition() == 0) && (rightTread.getCurrentPosition() == 0)) {
                    //state = 1;
                    leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                    rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                    state = 17;
                }
                break;
            // Case 13 - 16: Backward 12 inches.
            case 13:
                leftTread.setPower(-0.5);
                rightTread.setPower(-0.5);
                leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_UP);
                rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_UP);
                if (Math.abs(leftTread.getCurrentPosition()) >= (12 * TICKS_PER_INCH)) {
                    state = 14;
                }
                break;
            case 14:
                leftTread.setPower(0.0);
                rightTread.setPower(0.0);
                state = 15;
                break;
            case 15:
                leftTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                rightTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                state = 16;
                break;
            case 16:
                if ((leftTread.getCurrentPosition() == 0) && (rightTread.getCurrentPosition() == 0)) {
                    leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                    rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                    state = 5;
                }
                break;
            // Case 17 - 20: Forward 7 inches (slow speed).
            case 17:
                leftTread.setPower(0.5);
                rightTread.setPower(0.5);
                if (Math.abs(leftTread.getCurrentPosition()) >= (7 * TICKS_PER_INCH)) {
                    state = 18;
                }
                break;
            case 18:
                leftTread.setPower(0.0);
                rightTread.setPower(0.0);
                state = 19;
                break;
            case 19:
                leftTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                rightTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
                state = 20;
                break;
            case 20:
                if ((leftTread.getCurrentPosition() == 0) && (rightTread.getCurrentPosition() == 0)) {
                    //state = 1;
                    leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                    rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
                }
                break;
        }
    }
}
