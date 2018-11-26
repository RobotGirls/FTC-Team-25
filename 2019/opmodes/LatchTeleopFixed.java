package opmodes;

// For auto import of necessary classes, click on the
// name of the class in the code, then hit alt-enter for automatic import


import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
import team25core.OneWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TankMechanumControlSchemeReverse;
import team25core.TeleopDriveTaskReverse;

/*
 * FTC Team 25: Created by Elizabeth, November 03, 2018
 */
@TeleOp(name="Latch Teleop Fixed", group="Team 25")
//@Disabled
public class LatchTeleopFixed extends Robot {

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;
    private Servo latchServo;

    //private FourWheelDirectDrivetrain drivetrain;
    private TeleopDriveTaskReverse drive;
    //private MecanumWheelDriveTask drive;
    private OneWheelDriveTask driveArm;


    public static double LATCH_OPEN = 160/256.0;
    public static double LATCH_CLOSED = 210/256.0;

    @Override
    public void init() {
        // Hardware mapping.
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft   = hardwareMap.dcMotor.get("rearLeft");
        rearRight  = hardwareMap.dcMotor.get("rearRight");

        // Latch arm used to raise/lower arm
        latchArm        = hardwareMap.dcMotor.get("latchArm");

        // Latch servo used to close claw at end of latch arm
        latchServo      = hardwareMap.servo.get("latchServo");

        latchArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        latchArm.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Allows for latchArm to hold position when no button is pressed
        latchArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        latchServo.setPosition(LATCH_CLOSED);

        //drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        //drivetrain.setCanonicalMotorDirection();
        //drivetrain.resetEncoders();
        //drivetrain.encodersOn();

    }

    @Override
    public void handleEvent(RobotEvent e) {
        // Nothing
    }

    @Override
    public void start() {

        // sets up joysticks, so
        // both Y sticks up   - drives forward
        // both Y stick down - drives backward
        // both X sticks left - drives sideways left
        // both X sticks right - drives sideways right
        // right trigger - backward diagonal to the right
        // left trigger - backward diagonal to the left
        // right bumper - forward diagonal to the right
        // left bumper - forward diagonal to the left

        RobotLog.i(">>>> inside start");
        TankMechanumControlSchemeReverse scheme = new TankMechanumControlSchemeReverse(gamepad1);

        drive = new TeleopDriveTaskReverse(this, scheme, frontLeft, frontRight, rearLeft, rearRight);
        this.addTask(drive);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            @Override
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;

                RobotLog.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> eventkind" + event.kind);
                if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    // latchArm
                    latchArm.setPower(1);
                    RobotLog.i(">>>>>>>>>>>>>>>> eventkind" + event.kind);
                } else if (event.kind == EventKind.BUTTON_Y_UP) {
                    // latchArm
                    latchArm.setPower(0);
                    RobotLog.i(">>>>>>>>>>>>>>>> eventkind" + event.kind);
                } else if (event.kind == EventKind.BUTTON_A_DOWN) {
                    // latchArm
                    latchArm.setPower(-1);
                    RobotLog.i(">>>>>>>>>>>>>>>> eventkind" + event.kind);
                } else if (event.kind == EventKind.BUTTON_A_UP) {
                    // latchArm
                    latchArm.setPower(0);
                    RobotLog.i(">>>>>>>>>>>>>>>> eventkind" + event.kind);
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    // latchServo open
                    latchServo.setPosition(LATCH_OPEN);
                    RobotLog.i(">>>>>>>>>>>>>>>> eventkind " + event.kind);
                } else if (event.kind == EventKind.BUTTON_X_DOWN) {
                    latchServo.setPosition(LATCH_CLOSED);
                    RobotLog.i(">>>>>>>>>>>>>>>> eventkind " + event.kind);
                }
            }
        });
    }

}


