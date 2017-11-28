package opmodes;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import team25core.Alliance;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
import team25core.NavigateToTargetTask;
import team25core.OneWheelDriveTask;
import team25core.PersistentTelemetryTask;
import team25core.RangeSensorCriteria;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RobotNavigation;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 10/22/2016.
 */

@TeleOp(name = "DAISY Teleop", group = "Team25")
@Disabled
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
                            (y)         |  (y) claim beacon
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
    private DistanceSensor range;

    private FourWheelDirectDrivetrain drivetrain;
    private RangeSensorCriteria rangeCriteria;

    private MecanumWheelDriveTask drive;
    private OneWheelDriveTask controlCapBall;
    private PersistentTelemetryTask ptt;

    private final int LAUNCH_POSITION = Daisy.LAUNCH_POSITION;

    private boolean slow;
    private boolean leftPusherOut;
    private boolean rightPusherOut;

    private RobotNavigation nav;
    private NavigateToTargetTask nttt;
    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = Daisy.CAMERA_CHOICE;
    private NavigateToTargetTask.Targets vuforiaTarget;

    @Override
    public void handleEvent(RobotEvent e)
    {
        // Once robot is at the target, push beacon.
        if (e instanceof NavigateToTargetTask.NavigateToTargetEvent) {
            NavigateToTargetTask.NavigateToTargetEvent event = (NavigateToTargetTask.NavigateToTargetEvent) e;
            switch (event.kind) {
                case AT_TARGET:
                    DeadReckonPath pushBeacon = new DeadReckonPath();
                    pushBeacon.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, 0.6);
                    pushBeacon.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -0.6);

                    drivetrain.setNoncanonicalMotorDirection();
                    this.addTask(new DeadReckonTask(this, pushBeacon, drivetrain, rangeCriteria) {
                        @Override
                        public void handleEvent(RobotEvent e)
                        {
                            DeadReckonEvent event = (DeadReckonEvent) e;
                            if (event.kind == EventKind.PATH_DONE) {
                                drivetrain.resetEncoders();
                                drivetrain.encodersOn();
                                frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
                                rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
                                frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
                                rearRight.setDirection(DcMotorSimple.Direction.REVERSE);
                                drive.suspendTask(false);
                                robot.addTask(drive);
                            } else if (event.kind == EventKind.SENSOR_SATISFIED) {
                                drivetrain.resetEncoders();
                                drivetrain.encodersOn();
                                frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
                                rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
                                frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
                                rearRight.setDirection(DcMotorSimple.Direction.REVERSE);
                                drive.suspendTask(false);
                                robot.addTask(drive);
                            }
                        }
                    });
                    ptt.addData("TARGET", "Found");
                    break;
            }
        }
    }

    private void toggleLeftPusher()
    {
        if (!leftPusherOut) {
            leftPusher.setPosition(Daisy.LEFT_DEPLOY_POS);
            leftPusherOut = true;
        } else {
            leftPusher.setPosition(Daisy.LEFT_STOW_POS);
            leftPusherOut = false;
        }
    }

    private void toggleRightPusher()
    {
        if (!rightPusherOut) {
            rightPusher.setPosition(Daisy.RIGHT_DEPLOY_POS);
            rightPusherOut = true;
        } else {
            rightPusher.setPosition(Daisy.RIGHT_STOW_POS);
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
        range       = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "range");

        leftPusher.setPosition(0.5);
        rightPusher.setPosition(0.5);
        pushers = new ContinuousBeaconArms(this, leftPusher, rightPusher, true);

        odsSwinger.setPosition(0.7);
        capServo.setPosition(0.8);

        slow = false;
        rightPusherOut = false;
        leftPusherOut = false;

        ptt = new PersistentTelemetryTask(this);

        // Range sensor setup.
        rangeCriteria = new RangeSensorCriteria(range, 16);

        // Reset encoders.
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.setCanonicalMotorDirection();

        // Vuforia setup.
        nav = new RobotNavigation(this, drivetrain);
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = Daisy.KEY;
        parameters.cameraDirection = CAMERA_CHOICE;
        parameters.useExtendedTracking = false;

        VuforiaLocalizer vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        VuforiaTrackables targets = vuforia.loadTrackablesFromAsset("FTC_2016-17");
        targets.get(0).setName("Blue Near");
        targets.get(1).setName("Red Far");
        targets.get(2).setName("Blue Far");
        targets.get(3).setName("Red Near");

        OpenGLMatrix phoneLocationOnRobot = Daisy.PHONE_LOCATION_ON_ROBOT;

        nttt = new NavigateToTargetTask(this, drivetrain, 1000000, gamepad1, Alliance.RED);
        nttt.init(targets, parameters, phoneLocationOnRobot);
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
                    drive.suspendTask(true);
                    navigateToTarget();
                } else if (event.kind == EventKind.BUTTON_X_DOWN) {
                    robot.removeTask(nttt);   // Abort automatic target navigation.
                    drivetrain.resetEncoders();
                    drivetrain.encodersOn();
                    frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
                    rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
                    frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
                    rearRight.setDirection(DcMotorSimple.Direction.REVERSE);
                    drive.suspendTask(false);
                    robot.addTask(drive);
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
                        drive.slowDown(0.3);
                        slow = true;
                        ptt.addData("SLOW","true");
                   } else {
                        drive.slowDown(false);
                        slow = false;
                        ptt.addData("SLOW","false");
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

    private void navigateToTarget()
    {
        leftPusher.setPosition(Daisy.LEFT_DEPLOY_POS);
        rightPusher.setPosition(Daisy.RIGHT_STOW_POS);
        drivetrain.setNoncanonicalMotorDirection();
        drivetrain.resetEncoders();
        drivetrain.encodersOn();
        nttt.reset();
        this.addTask(nttt);
        nttt.findTarget();
        this.removeTask(drive);
    }

    @Override
    public void stop()
    {
        // Nothing.
    }
}
