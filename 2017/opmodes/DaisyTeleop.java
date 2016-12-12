package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.FourWheelDriveTask;
import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
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
      (L trigger)        (R trigger)    |
      (L bumper)         (R bumper)     |
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
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo odsSwinger;

    private MecanumWheelDriveTask drive;
    private PersistentTelemetryTask ptt;
    private RunToEncoderValueTask runToPositionTask;

    private final int LAUNCH_POSITION = DaisyConfiguration.LAUNCH_POSITION;
    private double leftPosition  = 0;
    private double rightPosition = 0;

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
            leftPosition = 1.0;
            leftPusherOut = true;
        } else {
            leftPosition = 0;
            leftPusherOut = false;
        }
        leftPusher.setPosition(leftPosition);
    }

    private void toggleRightPusher()
    {
        if (!rightPusherOut) {
            rightPosition = 1.0;
            rightPusherOut = true;
        } else {
            rightPosition = 0;
            rightPusherOut = false;
        }
        rightPusher.setPosition(rightPosition);
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
        leftPusher  = hardwareMap.servo.get("leftPusher");
        rightPusher = hardwareMap.servo.get("rightPusher");
        odsSwinger  = hardwareMap.servo.get("odsSwinger");

        leftPusher.setPosition(leftPosition);
        rightPusher.setPosition(rightPosition);
        odsSwinger.setPosition(0.7);

        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        rearRight.setDirection(DcMotorSimple.Direction.REVERSE);

        runToPositionTask = new RunToEncoderValueTask(this, launcher, LAUNCH_POSITION, 1.0);

        slow = false;
        rightPusherOut = false;
        leftPusherOut = false;

        ptt = new PersistentTelemetryTask(this);
    }

    @Override
    public void start()
    {
        drive = new MecanumWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);
        this.addTask(drive);
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
                    addTask(runToPositionTask);
                } else if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    toggleLeftPusher();
                } else if (event.kind == EventKind.RIGHT_TRIGGER_DOWN) {
                    toggleRightPusher();
                } else {
                    flowerPower.setPower(0.0);
                    conveyor.setPower(0.0);
                    launcher.setPower(0.0);
                }
            }
        });

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_A_DOWN) {
                   // Toggles slowness of motors.
                    if (!slow) {
                       drive.slowDown(0.35);
                       slow = true;
                        ptt.addData("Slow","true");
                   } else {
                       drive.slowDown(false);
                       slow = false;
                        ptt.addData("Slow","false");
                   }
                }
            }
        });
    }

    @Override
    public void stop()
    {

    }
}
