package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;
import team25core.TankMechanumControlSchemeFrenzy;
import team25core.TeleopDriveTask;

@TeleOp(name = "FreightFrenzyTeleopQT1")
//@Disabled
public class FrenzyQT1Teleop extends StandardFourMotorRobot {


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


    private Telemetry.Item locationTlm;
    private Telemetry.Item buttonTlm;

    //changing direction for gravelLift mechanism
    private DcMotor gravelLift;
    //private OneWheelDirectDrivetrain flipOverDrivetrain;
    private static double INTAKEDROP_OPEN = 180 / 256.0;
    private static double INTAKEDROP_OUT = 1 / 256.0;
    private boolean rotateDown = true;
    TankMechanumControlSchemeFrenzy scheme;


    private MechanumGearedDrivetrain drivetrain;

    @Override
    public void handleEvent(RobotEvent e) {
    }


    @Override
    public void init() {

        super.init();

        //mapping carousel mech
        carouselMech = hardwareMap.get(DcMotor.class, "carouselMech");


        //mapping freight intake mech
        freightIntake = hardwareMap.get(DcMotor.class, "freightIntake");
        gravelLift = hardwareMap.get(DcMotor.class, "gravelLift");
        intakeDrop = hardwareMap.servo.get("intakeDrop");

        // reset encoders

        carouselMech.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        freightIntake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        gravelLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        gravelLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

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

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent) e;
                locationTlm.setValue("in gamepad2 handler");
                switch (gamepadEvent.kind) {
                    //launching system
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
                }
            }

        });

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
                    case BUTTON_Y_DOWN:
                        //gravellift moves forward
                        gravelLift.setPower(0.07);
                        buttonTlm.setValue("button B down");
                        break;
                    case BUTTON_Y_UP:
                        buttonTlm.setValue("button B up");
                        gravelLift.setPower(0);
                        break;
                    case BUTTON_A_DOWN:
                        //gravellife moves backward
                        buttonTlm.setValue("button X down");
                        gravelLift.setPower(-0.04);
                        break;
                    case BUTTON_A_UP:
                        buttonTlm.setValue("button X up");
                        gravelLift.setPower(0);
                        break;
                    case RIGHT_BUMPER_DOWN:
                        //lets freight fall from gravellift
                        intakeDrop.setPosition(INTAKEDROP_OUT);
                        break;
                    case RIGHT_BUMPER_UP:
                        intakeDrop.setPosition(INTAKEDROP_OPEN);
                        break;

                }
            }

        });

    }
}

