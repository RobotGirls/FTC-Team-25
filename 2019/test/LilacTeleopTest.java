package test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;


import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.LeftAirplaneMechanumControlScheme;
import team25core.OneWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;
import team25core.TankMechanumControlScheme;
import team25core.TeleopDriveTask;

/**
 * FTC Team 25: Created by Elizabeth Wu on 11/20/18.
 */

@TeleOp(name = "LilacTeleopTest", group = "Team25")

public class LilacTeleopTest extends Robot {

     /*

    GAMEPAD 1: DRIVETRAIN CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    |  (LT) bward left diagonal    (RT) bward right diagonal                                        |
      (L bumper)         (R bumper)     |  (LB) fward left diagonal    (RB) fward right diagonal
                            (y)         |   (y)
      arrow pad          (x)   (b)      |   (x)                        (b)
                            (a)         |   (a)
                                        |   (DPad - UP)                (DPad - DOWN)

    GAMEPAD 2: MECHANISM CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    | (LT)                        (RT)
      (L bumper)         (R bumper)     | (LB)                        (RB)
                            (y)         |  (y)
      arrow pad          (x)   (b)      |  (x)                         (b)
                            (a)         |  (a)
    */

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latch;

    private Servo latchServo;

    private FourWheelDirectDrivetrain drivetrain;
    private TeleopDriveTask drive;
    private OneWheelDriveTask controlArm;


    private boolean slow = false;
    private boolean s1Open= true;
    private boolean s3Open = true;
    private boolean relicOpen = false;
    private boolean relicDown = true;
    private boolean rotateLeft = false;
    private Telemetry.Item speed;
    private Telemetry.Item encoderRelic;
    private Telemetry.Item encoderLift;

    private boolean rotated180 = false;
    private boolean lockout = false;

    public Direction currentDirection;
    public int currentEncoder;
    public boolean goDown = false;

    @Override
    public void handleEvent(RobotEvent e)
    {
        // Nothing.
    }


    @Override
    public void init()
    {
        // Hardware mapping.
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft   = hardwareMap.dcMotor.get("rearLeft");
        rearRight  = hardwareMap.dcMotor.get("rearRight");
        latch      = hardwareMap.dcMotor.get("latchArm");

        latchServo = hardwareMap.servo.get("latchServo");

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);

        drivetrain.resetEncoders();
        drivetrain.encodersOn();
        drivetrain.setNoncanonicalMotorDirection();
        drivetrain.setSplitPersonalityMotorDirection(true);

    }



    @Override
    public void start()
    {
        TankMechanumControlScheme scheme = new TankMechanumControlScheme(gamepad1);

        drive = new TeleopDriveTask(this, scheme, frontLeft, frontRight, rearLeft, rearRight);


        this.addTask(drive);

    }

}