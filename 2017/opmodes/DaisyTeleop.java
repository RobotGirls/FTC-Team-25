package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
import team25core.OneWheelDriveTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 10/22/2016.
 */

@TeleOp(name = "Daisy Teleop", group = "Team25")
public class DaisyTeleop extends Robot
{
    /*

    GAMEPAD 1: DRIVETRAIN CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    |  (LT) bward left diagonal    (RT) bward right diagonal
      (L bumper)         (R bumper)     |  (LB) fward left diagonal    (RB) fward right diagonal
                            (y)         |
      arrow pad          (x)   (b)      |
                            (a)         |  (a) toggle slowness

    GAMEPAD 2: MECHANISM CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    | (LT) toggle L pusher out     (RT) toggle R pusher out
      (L bumper)         (R bumper)     | (LB) rotate flea forward     (RB) rotate flea backward
                            (y)         |  (y) launch particle
      arrow pad          (x)   (b)      |  (b) flower power (accept)
                            (a)         |  (a) flower power (reject)

    */

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor flowerPower;
    private DcMotor conveyor;
    private DcMotor launcher;
    private DcMotor capBall;
    private Servo capServo;
    private Servo leftPusher;
    private Servo rightPusher;
    private ContinuousBeaconArms pushers;
    private Servo odsSwinger;

    private MecanumWheelDriveTask drive;
    private OneWheelDriveTask controlCapBall;
    private PersistentTelemetryTask ptt;

    private final int LAUNCH_POSITION = Daisy.LAUNCH_POSITION;

    private boolean slow;
    private boolean leftPusherOut;
    private boolean rightPusherOut;

    @Override
    public void handleEvent(RobotEvent e)
    {
        // Nothing.
    }

    private void toggleLeftPusher()
    {
        if (!leftPusherOut) {
            pushers.deployLeft();
            leftPusherOut = true;
        } else {
            pushers.stowLeft();
            leftPusherOut = false;
        }
    }

    private void toggleRightPusher()
    {
        if (!rightPusherOut) {
            pushers.deployRight();
            rightPusherOut = true;
        } else {
            pushers.stowRight();
            rightPusherOut = false;
        }
    }

    @Override
    public void init()
    {
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        flowerPower = hardwareMap.dcMotor.get("flowerPower");
        conveyor    = hardwareMap.dcMotor.get("conveyor");
        launcher    = hardwareMap.dcMotor.get("launcher");
        capBall     = hardwareMap.dcMotor.get("capBall");
        leftPusher  = hardwareMap.servo.get("leftPusher");
        rightPusher = hardwareMap.servo.get("rightPusher");
        odsSwinger  = hardwareMap.servo.get("odsSwinger");
        capServo    = hardwareMap.servo.get("capServo");

        leftPusher.setPosition(0.5);
        rightPusher.setPosition(0.5);
        pushers = new ContinuousBeaconArms(this, leftPusher, rightPusher, true);

        odsSwinger.setPosition(0.7);
        capServo.setPosition(1.0);

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        rearRight.setDirection(DcMotorSimple.Direction.REVERSE);

        slow = false;
        rightPusherOut = false;
        leftPusherOut = false;

        ptt = new PersistentTelemetryTask(this);
    }

    @Override
    public void start()
    {
        drive = new MecanumWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);
        controlCapBall = new OneWheelDriveTask(this, capBall, true);
        this.addTask(drive);
        this.addTask(controlCapBall);
        this.addTask(ptt);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_A_DOWN) {
                    flowerPower.setPower(1.0);
                    conveyor.setPower(-1.0);
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    flowerPower.setPower(-1.0);
                    conveyor.setPower(1.0);
                } else if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    launcher.setPower(1.0);
                } else if (event.kind == EventKind.RIGHT_BUMPER_DOWN) {
                    launcher.setPower(-1.0);
                } else if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    capBall.setPower(1.0);
                } else if (event.kind == EventKind.BUTTON_X_DOWN) {
                    capBall.setPower(-1.0);
                } else if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    toggleLeftPusher();
                } else if (event.kind == EventKind.RIGHT_TRIGGER_DOWN) {
                    toggleRightPusher();
                } else {
                    flowerPower.setPower(0.0);
                    conveyor.setPower(0.0);
                    launcher.setPower(0.0);
                    capBall.setPower(0.0);
                }
            }
        });

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_A_DOWN) {
                   // Toggles slowness of motors.
                    if (!slow) {
                        drive.slowDown(0.45);
                        slow = true;
                        ptt.addData("Slow","true");
                   } else {
                        drive.slowDown(false);
                        slow = false;
                        ptt.addData("Slow","false");
                   }
                } else if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    capServo.setPosition(0.8);  // Values may change.
                } else if (event.kind == EventKind.BUTTON_X_DOWN) {
                    // change direction
                    drive.changeDirection();
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    capServo.setPosition(0.0);  // Values may change.
                }
            }
        });
    }

    @Override
    public void stop()
    {

    }
}
