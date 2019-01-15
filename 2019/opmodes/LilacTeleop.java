package opmodes;

// For auto import of necessary classes, click on the
// name of the class in the code, then hit alt-enter for automatic import


import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.LimitSwitchCriteria;
import team25core.MecanumWheelDriveTask;
import team25core.MotorStallTask;
import team25core.OneWheelDirectDrivetrain;
import team25core.OneWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TankMechanumControlScheme;
import team25core.TeleopDriveTask;

/*
 * FTC Team 25: Created by Elizabeth, November 03, 2018
 */

@TeleOp(name="Lilac Teleop (Meet2)", group="Team 25")
//@Disabled
public class LilacTeleop extends Robot {

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;
   // private Servo latchServo;
    private Servo marker;



    private DeadReckonPath moveArm;
    private DeadReckonTask moveArmTask;

    private MotorStallTask stallTask;

    private DigitalChannel limitSwitch;
    private LimitSwitchCriteria limitSwitchCriteria;

    //private FourWheelDirectDrivetrain drivetrain;
    private OneWheelDirectDrivetrain single;

    //private TeleopDriveTaskReverse drive;
    private TeleopDriveTask drive;
    //private MecanumWheelDriveTask drive;
    private OneWheelDriveTask driveArm;

    private boolean slow = false;


    public static double LATCH_OPEN     = 255  / 256.0;
    public static double LATCH_CLOSED   = 45   / 256.0;
    public static double MARKER_OPEN    = 250  / 256.0;
    public static double MARKER_CLOSED  = 129  / 256.0;

    @Override
    public void init() {
        // Hardware mapping.
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft   = hardwareMap.dcMotor.get("rearLeft");
        rearRight  = hardwareMap.dcMotor.get("rearRight");
        latchArm   = hardwareMap.dcMotor.get("latchArm");
        marker     = hardwareMap.servo.get("marker");
      //  latchServo = hardwareMap.servo.get("latchServo");

        // limitSwitch = hardwareMap.digitalChannel.get("limit");
        limitSwitchCriteria = new LimitSwitchCriteria(limitSwitch);

        single = new OneWheelDirectDrivetrain(latchArm);
        single.resetEncoders();
        single.encodersOn();

        // Allows for latchArm to hold position when no button is pressed
        latchArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

       // latchServo.setPosition(LATCH_CLOSED);

        moveArm = new DeadReckonPath();

        moveArmTask = new DeadReckonTask(this, moveArm, single) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    single.stop();
                }
            }
        };


        //drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        //drivetrain.setCanonicalMotorDirection();
        //drivetrain.resetEncoders();
        //drivetrain.encodersOn();

    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        // Nothing
    }

    private void runArm()
    {
        this.addTask(moveArmTask);
    }

    private void stopStallMotor()
    {
        this.addTask(new MotorStallTask(this, latchArm, telemetry) {
            @Override
            public void handleEvent(RobotEvent event)
            {
                latchArm.setPower(0.0);
            }

        });
    }

    @Override
    public void start()
    {

        // sets up joysticks, so
        // both Y sticks up   - drives forward
        // both Y stick down - drives backward
        // both X sticks left - drives sideways left
        // both X sticks right - drives sideways right
        // right trigger - backward diagonal to the right
        // left trigger - backward diagonal to the left
        // right bumper - forward diagonal to the right
        // left bumper - forward diagonal to the left

        TankMechanumControlScheme scheme = new TankMechanumControlScheme(gamepad1);

        drive = new TeleopDriveTask(this, scheme, frontLeft, frontRight, rearLeft, rearRight);
       // drive = new TeleopDriveTaskReverse(this, scheme, frontLeft, frontRight, rearLeft, rearRight);

        this.addTask(drive);


        /* TODO add slow mode
       this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;
                if (event.kind == EventKind.BUTTON_Y_DOWN) { // Going out
                    // latchArm.setPower(1);
                    // arm up
                    if (slow) {

                    }
                    moveArm.stop();
                    moveArm.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 55, 0.5);
                    runArm();
                }
            }

        }); */



        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;
                if (event.kind == EventKind.RIGHT_TRIGGER_DOWN) {
                    moveArmTask.stop();
                    latchArm.setPower(0);
                } else if (event.kind == EventKind.RIGHT_BUMPER_DOWN) {
                    moveArmTask.stop();
                    latchArm.setPower(0);
                }
            }
        });


        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;
                /*if (event.kind == EventKind.BUTTON_Y_DOWN) { // Going out
                   // latchArm.setPower(1);
                   // arm up
                   moveArm.stop();
                   moveArm.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 55, 0.5);
                   runArm();
                } else */
                if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    latchArm.setPower(-0.4);
                    stopStallMotor();
                } else if (event.kind == EventKind.RIGHT_BUMPER_DOWN) {
                    moveArmTask.stop();
                    latchArm.setPower(0);
                } else if (event.kind == EventKind.RIGHT_TRIGGER_DOWN) {
                    moveArmTask.stop();
                    latchArm.setPower(0);
                } else if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    latchArm.setPower(-0.7);
                } else if (event.kind == EventKind.LEFT_TRIGGER_UP) {
                    latchArm.setPower(0);
                } else if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    latchArm.setPower(1);
                } else if (event.kind == EventKind.LEFT_BUMPER_UP) {
                    latchArm.setPower(0);
                } else if (event.kind == EventKind.DPAD_UP_DOWN) {
                    marker.setPosition(MARKER_OPEN);
                } else if (event.kind == EventKind.DPAD_DOWN_DOWN) {
                    marker.setPosition(MARKER_CLOSED);
                }

            }

        });
    }

}


