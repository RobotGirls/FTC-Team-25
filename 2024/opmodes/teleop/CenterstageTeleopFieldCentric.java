package opmodes.teleop;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import java.util.HashMap;

import opmodes.old.FieldCentricDriveScheme;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.MotorPackage;
import team25core.OneWheelDriveTask;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.StandardFourMotorRobot;
import team25core.TeleopDriveTask;

@TeleOp(name = "CenterstageTeleopFC")
//@Disabled
public class CenterstageTeleopFieldCentric extends StandardFourMotorRobot {
    //new teleop
    private TeleopDriveTask drivetask;

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private BNO055IMU imu;

    private Telemetry.Item locationTlm;
    private Telemetry.Item targetPositionTlm;

    FieldCentricDriveScheme scheme;

    private MechanumGearedDrivetrain drivetrain;

    private DcMotor linearLift;
    private OneWheelDriveTask liftMotorTask;
    private DcMotor intake;

    private Servo box;
    private Servo pixelRelease;

    private Servo linkage;

    private Servo purplePixel;

    private DcMotor rightHang;
    private DcMotor leftHang;

    private Servo shooter;

    private final double BLOCK_NOTHING = 0.25;
    private final double BLOCK_BOTH = 0.05;

    private final double LINKAGE_UP = 0.65;
    private final double LINKAGE_DOWN = 0.2;
    private final double LINKAGE_ONE_PIXEL = 0.48; // getting top pixel from stack of 5
    private final double LINKAGE_TWO_PIXELS = 0.45; // getting top pixel from stack of 4

    private boolean intakeOn;
    private boolean outtakeOn;


    //  @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {

        //super.init();
        motorMap = new HashMap<>();

        frontLeft = hardwareMap.get(DcMotorEx.class, "leftFront");
        motorMap.put(MotorPackage.MotorLocation.FRONT_LEFT, new MotorPackage(frontLeft));

        frontRight = hardwareMap.get(DcMotorEx.class, "rightFront");
        motorMap.put(MotorPackage.MotorLocation.FRONT_RIGHT, new MotorPackage(frontRight));

        backLeft = hardwareMap.get(DcMotorEx.class, "leftRear");
        motorMap.put(MotorPackage.MotorLocation.BACK_LEFT, new MotorPackage(backLeft));

        backRight = hardwareMap.get(DcMotorEx.class, "rightRear");
        motorMap.put(MotorPackage.MotorLocation.BACK_RIGHT, new MotorPackage(backRight));

        initIMU();

        intake=hardwareMap.get(DcMotor.class, "outtake");
        intake.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        //purple pixel servo
        purplePixel = hardwareMap.servo.get("purplePixel");

        // flip mechanism
        box = hardwareMap.servo.get("pixelBox");
//        box.setPosition(0.0975);

        // pixel release mechanism (mounted on box)
        pixelRelease = hardwareMap.servo.get("pixelRelease");
        pixelRelease.setPosition(BLOCK_BOTH);

        shooter = hardwareMap.servo.get("droneShooter");
        shooter.setPosition(0.45);

        linkage = hardwareMap.servo.get("linkage");
        linkage.setPosition(LINKAGE_UP);

        rightHang = hardwareMap.get(DcMotor.class, "rightHang");
        rightHang.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightHang.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightHang.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        leftHang = hardwareMap.get(DcMotor.class, "leftHang");
        leftHang.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftHang.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftHang.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        linearLift = hardwareMap.get(DcMotor.class, "linearLift");
        linearLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        linearLift.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        linearLift.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        liftMotorTask = new OneWheelDriveTask(this, linearLift, false);
        liftMotorTask.slowDown(false);

        frontLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeft.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRight.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        scheme = new FieldCentricDriveScheme(gamepad1);

        //code for forward mecanum drivetrain:
        drivetrain = new MechanumGearedDrivetrain(motorMap);
        drivetask = new TeleopDriveTask(this, scheme, frontLeft, frontRight, backLeft, backRight);
        drivetask.slowDown(false);

        drivetrain.setNoncanonicalMotorDirection();

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

    private void delay(int delayInMsec) {
        this.addTask(new SingleShotTimerTask(this, delayInMsec) {
            @Override
            public void handleEvent(RobotEvent e) {
                SingleShotTimerEvent event = (SingleShotTimerEvent) e;
            }
        });
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
                    case RIGHT_BUMPER_DOWN:
                        //intake pixels
                        if(intakeOn == false) {
                            intake.setPower(-0.9);
                            intakeOn = true;
                        }
                        else {
                            intake.setPower(0);
                            intakeOn = false;
                        }
                        break;
                    case LEFT_BUMPER_DOWN:
                        //outtake pixels
                        if(outtakeOn == false) {
                            intake.setPower(0.8);
                            outtakeOn = true;
                        }
                        else {
                            intake.setPower(0);
                            outtakeOn = false;
                        }
                        break;
                    case DPAD_UP_DOWN:
                        // box up to score and block pixels
                        box.setPosition(0.45);
                        pixelRelease.setPosition(BLOCK_BOTH);
                        break;
                    case DPAD_DOWN_DOWN:
                        // flip box to original position block pixels from falling
                        pixelRelease.setPosition(BLOCK_BOTH);
                        box.setPosition(0.85);
                        break;
                    case BUTTON_Y_DOWN:
                        // shoot drone
                        shooter.setPosition(0.15);
                        locationTlm.setValue("drone button y pressed");
                        break;
                    case BUTTON_A_DOWN:
                        // hold drone
                        shooter.setPosition(0.45);
                        locationTlm.setValue("drone button a pressed");
                        break;
                    case BUTTON_X_DOWN:
                        // intake linkage
                        linkage.setPosition(LINKAGE_UP);
                        locationTlm.setValue("intake linkage up");
                        break;
                    case BUTTON_B_DOWN:
                        // intake linkage
                        linkage.setPosition(LINKAGE_DOWN);
                        locationTlm.setValue("intake linkage down");
                        break;
                    case DPAD_LEFT_DOWN:
                        // intake one pixel from stack of 5
                        linkage.setPosition(LINKAGE_ONE_PIXEL);
                        locationTlm.setValue("intake linkage at height of top pixel");
                        break;
                    case DPAD_RIGHT_DOWN:
                        // intake one pixel from stack of 4
                        linkage.setPosition(LINKAGE_TWO_PIXELS);
                        locationTlm.setValue("intake linkage at height of 2nd pixel");
                        break;
                }
            }
        });

        this.addTask(liftMotorTask);

        //gamepad2 w /nowheels only mechs
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent gamepadEvent = (GamepadEvent) e;
                locationTlm.setValue("in gamepad2 handler");
                switch (gamepadEvent.kind) {
//                    case LEFT_TRIGGER_DOWN:
//                        // flip box to original position block pixels from falling
//                        pixelRelease.setPosition(BLOCK_BOTH);
//                        box.setPosition(0.85);
//                        break;
//                    case LEFT_BUMPER_DOWN:
//                        // box up to score and block pixels
//                        box.setPosition(0.45);
//                        pixelRelease.setPosition(BLOCK_BOTH);
//                        break;
                    case DPAD_UP_DOWN:
                        // block pixels in box
                        pixelRelease.setPosition(BLOCK_BOTH);
                        break;
                    case DPAD_DOWN_DOWN:
                        // pixel box is open
                        pixelRelease.setPosition(BLOCK_NOTHING);
                        break;
                    // hanger up
                    case BUTTON_Y_DOWN:
                        leftHang.setPower(1);
                        rightHang.setPower(-1);
                        break;
                    case BUTTON_Y_UP:
                        leftHang.setPower(0);
                        rightHang.setPower(0);
                        break;
                    // hanger down
                    case BUTTON_A_DOWN:
                        leftHang.setPower(-1);
                        rightHang.setPower(1);
                        break;
                    case BUTTON_A_UP:
                        leftHang.setPower(0);
                        rightHang.setPower(0);
                        break;
                    case BUTTON_X_DOWN:
                        leftHang.setPower(1);
                        break;
                    case BUTTON_X_UP:
                        leftHang.setPower(0);
                        break;
                    case BUTTON_B_DOWN:
                        leftHang.setPower(-1);
                        break;
                    case BUTTON_B_UP:
                        leftHang.setPower(0);
                        break;
                    case RIGHT_STICK_UP:
                        //turns off intake
                        intake.setPower(0);
                        intakeOn = false;
                        locationTlm.setValue("lift is up, intake automatically off");
                        break;
                    case DPAD_RIGHT_DOWN:
                        //turns off intake
                        intake.setPower(0);
                        intakeOn = false;
                        break;
                }
            }
        });
    }
}