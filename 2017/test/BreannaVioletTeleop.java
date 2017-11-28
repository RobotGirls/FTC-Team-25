package test;


import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
import team25core.OneWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import test.Violet;

/**
 * FTC Team 25: Created by Elizabeth Wu on 11/1/17.
 */
@TeleOp(name = "BC Teleop", group = "Team25")
public class BreannaVioletTeleop extends Robot {

     /*

    GAMEPAD 1: DRIVETRAIN CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    |  // FOR FUTURE..needs to be programmed
                                        |  (LT) bward left diagonal    (RT) bward right diagonal
      (L bumper)         (R bumper)     |  (LB) fward left diagonal    (RB) fward right diagonal
                            (y)         |
      arrow pad          (x)   (b)      |
                            (a)         |

    GAMEPAD 2: MECHANISM CONTROLLER
    --------------------------------------------------------------------------------------------
      (L trigger)        (R trigger)    | (LT) rotate block left      (RT) lower relic holder
      (L bumper)         (R bumper)     | (LB) rotate block right     (RB) raise relic holder
                            (y)         |  (y)
      arrow pad          (x)   (b)      |  (b)
                            (a)         |  (a)




    */

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    };

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor rotate;
    private DcMotor linear;
    //private DcMotor slide;

    private Servo s2;
    private Servo s4;
    private Servo s1;
    private Servo s3;
    //private Servo jewel;
    //private Servo relic;

    private FourWheelDirectDrivetrain drivetrain;
    private MecanumWheelDriveTask drive;
    private OneWheelDriveTask controlLinear;

    private boolean clawDown = true;
    private boolean s1Open = true;
    private boolean s3Open = true;

    private boolean lockout = false;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof RunToEncoderValueTask.RunToEncoderValueEvent) {
            if (((RunToEncoderValueTask.RunToEncoderValueEvent)e).kind == RunToEncoderValueTask.EventKind.DONE) {
                lockout = false;
                RobotLog.i("Done moving motor");
            }
        }
    }


    @Override
    public void init()
    {
        // Hardware mapping.
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft   = hardwareMap.dcMotor.get("rearLeft");
        rearRight  = hardwareMap.dcMotor.get("rearRight");
        rotate  = hardwareMap.dcMotor.get("rotate");
        linear  = hardwareMap.dcMotor.get("linear");

        s2    = hardwareMap.servo.get("s2");
        s4    = hardwareMap.servo.get("s4");
        s1    = hardwareMap.servo.get("s1");
        s3    = hardwareMap.servo.get("s3");
        //jewel       = hardwareMap.servo.get("jewel");
        //relic       = hardwareMap.servo.get("relic");

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

        openClaw();
    }

    /**
     * Move claw up and down, making sure we don't over rotate.
     */
    private void toggleClawVertical()
    {
        if (clawDown == true) {
            linear.setDirection(DcMotorSimple.Direction.FORWARD);
            clawDown = false;
        } else {
            linear.setDirection(DcMotorSimple.Direction.REVERSE);
            clawDown = true;
        }
        this.addTask(new RunToEncoderValueTask(this, linear, Violet.CLAW_VERTICAL, Violet.CLAW_VERTICAL_POWER));
    }

    /**
     * Blindly open both claws and set the state appropriately.
     */
    private void openClaw()
    {
        s1.setPosition(Violet.S1_OPEN);
        s2.setPosition(Violet.S2_OPEN);
        s3.setPosition(Violet.S3_OPEN);
        s4.setPosition(Violet.S4_OPEN);

        s1Open = true;
        s3Open = true;
    }

    /**
     * The servos always work in pairs.  S1/S2 and S3/S4.  toggleS1 therefore refers the to the S1/S2 pair.
     */
    private void toggleS1()
    {
        if (s1Open == true) {
            s1.setPosition(Violet.S1_CLOSED);
            s2.setPosition(Violet.S2_CLOSED);
            s1Open = false;
        } else {
            s1.setPosition(Violet.S1_OPEN);
            s2.setPosition(Violet.S2_OPEN);
            s1Open = true;
        }
    }

    /**
     * The servos always work in pairs.  S1/S2 and S3/S4.  toggleS3 therefore refers the to the S3/S4 pair.
     */
    private void toggleS3()
    {
        if (s3Open == true) {
            s3.setPosition(Violet.S3_CLOSED);
            s4.setPosition(Violet.S4_CLOSED);
            s3Open = false;
        } else {
            s3.setPosition(Violet.S3_OPEN);
            s4.setPosition(Violet.S4_OPEN);
            s3Open = true;
        }
    }

    /**
     * We will spin the claw back and forth, be careful that you alternate directions so that
     * you don't wrap the servo cables around the motor shaft.
     *
     * This motor's movement is not symmetrical, so we'll compensate in one direction.
     */
    private void rotate(Direction direction)
    {
        int distance;

        if (direction == Direction.CLOCKWISE) {
            rotate.setDirection(DcMotorSimple.Direction.REVERSE);
            distance = Violet.DEGREES_180;
        } else {
            rotate.setDirection(DcMotorSimple.Direction.FORWARD);
            distance = (int)(Violet.DEGREES_180);
        }
        this.addTask(new RunToEncoderValueTask(this, rotate, distance, Violet.ROTATE_POWER));
    }

    /**
     * Fine alignment for the claw.  Note that there is no deadman motor function specifically to
     * avoid operator error wherein the motor is held on too long and we over rotate thereby
     * damaging the cabling or wire harnesses.
     */
    private void nudge(Direction direction)
    {
        if (direction == Direction.CLOCKWISE) {
            rotate.setDirection(DcMotorSimple.Direction.REVERSE);
        } else {
            rotate.setDirection(DcMotorSimple.Direction.FORWARD);
        }
        this.addTask(new RunToEncoderValueTask(this, rotate, Violet.NUDGE, Violet.NUDGE_POWER));
    }

    @Override
    public void start()
    {
        drive = new MecanumWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);
        controlLinear = new OneWheelDriveTask(this, linear, true);

        this.addTask(drive);
        this.addTask(controlLinear);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;


                // Finish a move before we allow another one.

                if (lockout == true) {
                    return;
                }

                switch (event.kind) {
                    case BUTTON_Y_DOWN:
                        //Toggle the claw up and down.

                        //toggleClawVertical();
                        linear.setPower(0.5);
                        break;
                    case BUTTON_A_DOWN:
                        //Open the claw

                        openClaw();
                        break;
                    case LEFT_BUMPER_DOWN:
                        //Toggle s1/s2

                        toggleS1();
                        break;
                    case RIGHT_BUMPER_DOWN:
                        //Toggle s3/s4

                        toggleS3();
                        break;
                    case BUTTON_B_DOWN:
                        //Rotate 180 degrees clockwise

                        lockout = true;
                        rotate(Direction.CLOCKWISE);
                        break;
                    case BUTTON_X_DOWN:
                        // Rotate 180 degrees counterclockwise

                        lockout = true;
                        rotate(Direction.COUNTERCLOCKWISE);
                        break;
                    case LEFT_TRIGGER_DOWN:
                        //Nudge counterclockwise looking from behind robot

                        lockout = true;
                        nudge(Direction.COUNTERCLOCKWISE);
                        break;
                    case RIGHT_TRIGGER_DOWN:
                        //Nudge clockwise looking from behind robot

                        lockout = true;
                        nudge(Direction.CLOCKWISE);
                        break;
                    default:
                        linear.setPower(0.0);
                        break;
                }
            }
        });
    }
}