package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;
import team25core.TankMechanumControlScheme;
import team25core.TankMechanumControlSchemeFrenzy;
import team25core.TankMechanumControlSchemeReverse;
import team25core.TeleopDriveTask;

@TeleOp(name = "FreightFrenzyLm3Teleop")
//@Disabled
public class FrenzyLm3Teleop extends StandardFourMotorRobot {


    private TeleopDriveTask drivetask;

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    //duck carousel
    private DcMotor carouselMech;

    //freight intake
    private DcMotor freightIntake;
    private Servo intakeDrop;
    private boolean intakeDropOpen = false;

    private Telemetry.Item locationTlm;
    private Telemetry.Item buttonTlm;

    //changing direction for flip mechanism
    private DcMotor flipOver;
    private DcMotor flapper;
    //private OneWheelDirectDrivetrain flipOverDrivetrain;
    public static int DEGREES_DOWN = 1600;
    public static int DEGREES_UP = 180;
    public static double FLIPOVER_POWER = 0.3;
    private boolean rotateDown = true;
    TankMechanumControlSchemeFrenzy scheme;


    private MechanumGearedDrivetrain drivetrain;

    @Override
    public void handleEvent(RobotEvent e) {
    }

//    //flipover positions for bottom and top positions for intake and placing on hubs
//    private void rotateFlipOver(Direction direction) {
//        if (direction == FrenzyTeleop.Direction.CLOCKWISE) {
//            flipOver.setDirection(DcMotorSimple.Direction.REVERSE);
//        } else {
//            flipOver.setDirection(DcMotorSimple.Direction.FORWARD);
//        }
//        this.addTask(new RunToEncoderValueTask(this, flipOver, DEGREES_DOWN, FLIPOVER_POWER));
//    }
//
//    //alternates between down and up positions.
//    private void alternateRotate() {
//        if (rotateDown) {       // happens first
//            rotateFlipOver(FrenzyTeleop.Direction.CLOCKWISE);
//            rotateDown = false;
//
//        } else {
//            rotateFlipOver(FrenzyTeleop.Direction.COUNTERCLOCKWISE);
//            rotateDown = true;
//        }
//    }


    @Override
    public void init() {

        super.init();

        //mapping carousel mech
        carouselMech = hardwareMap.get(DcMotor.class, "carouselMech");


        //mapping freight intake mech
        freightIntake = hardwareMap.get(DcMotor.class, "freightIntake");
        flipOver = hardwareMap.get(DcMotor.class, "flipOver");
        //flapper = hardwareMap.get(DcMotor.class, "flipOver");
//      intakeDrop = hardwareMap.servo.get("intakeDrop");

        // reset encoders

        carouselMech.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        freightIntake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flipOver.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flipOver.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        //flapper.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        //flipOver.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        //allows for flipOver moter to hold psoition when no button is being pressed
        //flipOver.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //flipOverDrivetrain = new OneWheelDirectDrivetrain(flipOver);

        // scheme = new TankMechanumControlSchemeReverse(gamepad1);
        scheme = new TankMechanumControlSchemeFrenzy(gamepad1);


        //code for forward mechanum drivetrain:
        drivetrain = new MechanumGearedDrivetrain(motorMap);
        drivetask = new TeleopDriveTask(this, scheme, frontLeft, frontRight, backLeft, backRight);
        drivetask.slowDown(true);

        locationTlm = telemetry.addData("location","init");
        buttonTlm = telemetry.addData("button", "n/a");
    }

    @Override
    public void start() {

        this.addTask(drivetask);
        locationTlm.setValue("in start");

        //gamepad2 w /nowheels only mechs
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent) e;
                locationTlm.setValue("in gamepad2 handler");
                switch (gamepadEvent.kind) {
                    //launching system
                    case RIGHT_TRIGGER_DOWN:
                        //moving carousel
                        carouselMech.setPower(-1);
                        break;
                    case RIGHT_TRIGGER_UP:
                        //STOPPING CAROUSEL
                        carouselMech.setPower(0);
                        break;
                    case LEFT_TRIGGER_DOWN:
                        //moving carousel
                        carouselMech.setPower(1);
                        break;
                    case LEFT_TRIGGER_UP:
                        //STOPPING CAROUSEL
                        carouselMech.setPower(0);
                        break;
                    case RIGHT_BUMPER_DOWN:
                        //moving flaps forward
                        freightIntake.setPower(1);
                        break;
                    case RIGHT_BUMPER_UP:
                        freightIntake.setPower(0);
                        break;
                    case LEFT_BUMPER_DOWN:
                        //moving flaps backward
                        freightIntake.setPower(-1);
                        break;
                    case LEFT_BUMPER_UP:
                        freightIntake.setPower(0);
                        break;
                    case BUTTON_A_DOWN:
                        flipOver.setPower(0.4);
                        buttonTlm.setValue("button B down");
                        break;
                    case BUTTON_A_UP:
                        buttonTlm.setValue("button B up");
                        flipOver.setPower(0);
                        break;
                    case BUTTON_Y_DOWN:
                        buttonTlm.setValue("button X down");
                        flipOver.setPower(-0.4);
                        break;
                    case BUTTON_Y_UP:
                        buttonTlm.setValue("button X up");
                        flipOver.setPower(0);
                        break;

                }
            }

        });

    }
}

