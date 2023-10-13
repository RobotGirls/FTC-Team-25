package opmodes;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.rev.Rev2mDistanceSensor;
import com.qualcomm.hardware.rev.RevColorSensorV3;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.TouchSensor;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import team25core.DeadReckonPath;
import team25core.DistanceSensorCriteria;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import team25core.OneWheelDriveTaskwLimitSwitch;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.StandardFourMotorRobot;
import team25core.TeleopDriveTask;
import team25core.TwoStickMechanumControlScheme;

@TeleOp(name = "CenterstageTeleop")
//@Disabled
public class CenterstageTeleop extends StandardFourMotorRobot {
//new teleop

    private TeleopDriveTask drivetask;

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private BNO055IMU imu;

    private Telemetry.Item locationTlm;
    private Telemetry.Item targetPositionTlm;

    TwoStickMechanumControlScheme scheme;

    private MechanumGearedDrivetrain drivetrain;

    //private DcMotor linearLift;
    private Servo rotateShooter;
    private Servo shooter;
    private DcMotor intake;

    //  @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {

        super.init();
        initIMU();

        intake=hardwareMap.get(DcMotor.class, "outtake");
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        /*
        intake.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        intake.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        */

        rotateShooter = hardwareMap.servo.get("rotateShooter");
        shooter = hardwareMap.servo.get("shootDrone");

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        scheme = new TwoStickMechanumControlScheme(gamepad1);

        //code for forward mecanum drivetrain:
        drivetrain = new MechanumGearedDrivetrain(motorMap);
        drivetask = new TeleopDriveTask(this, scheme, frontLeft, frontRight, backLeft, backRight);
        drivetask.slowDown(false);

        locationTlm = telemetry.addData("location","init");
        targetPositionTlm = telemetry.addData("target Pos","init");
    }

    public void initIMU()
    {
        // Retrieve the IMU from the hardware map
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        // Technically this is the default, however specifying it is clearer
        parameters.angleUnit = BNO055IMU.AngleUnit.RADIANS;
        // Without this, data retrieving from the IMU throws an exception
        imu.initialize(parameters);

    }

    @Override
    public void start() {

        this.addTask(drivetask);
        locationTlm.setValue("in start");

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent) e;
                locationTlm.setValue("in gamepad1 handler");
                switch (gamepadEvent.kind) {

                }
            }
        });
/*
        this.addTask(new OneWheelDriveTaskwLimitSwitch(this, linearLift, true, umbrellaLimitSwitch, true)
        {
            public void handleEvent(RobotEvent e) {
                OneWheelDriveTaskwLimitSwitchEvent switchEvent = (OneWheelDriveTaskwLimitSwitchEvent) e;
                locationTlm.setValue("in gamepad1 handler");
                switch (switchEvent.kind) {

                }
            }
        });
*/

        //gamepad2 w /nowheels only mechs
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent) e;
                locationTlm.setValue("in gamepad2 handler");
                switch (gamepadEvent.kind) {
                    case LEFT_TRIGGER_DOWN:
                        intake.setPower(0.5); // test this
                        break;
                    case RIGHT_TRIGGER_DOWN:
                        intake.setPower(-0.5); // test this
                        break;
                    case LEFT_TRIGGER_UP:
                        intake.setPower(0);
                        break;
                    case RIGHT_TRIGGER_UP:
                        intake.setPower(0);
                        break;
                    case BUTTON_B_DOWN:
                        shooter.setPosition(0.6); // need to test this
                        break;
                        /*
                    case BUTTON_X_DOWN:
                        turret.setTargetPosition(800);
                        turret.setPower(0.8);
                        locationTlm.setValue(turret.getCurrentPosition());
                        targetPositionTlm.setValue(turret.getTargetPosition());
                        break;
                        */
                    case BUTTON_Y_DOWN:
                        shooter.setPosition(0.6); // test this
                        break;
                }
            }
        });
    }
}