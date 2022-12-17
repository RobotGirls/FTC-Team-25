package opmodes;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.RunToEncoderValueTask;
import team25core.MechanumGearedDrivetrain;
import team25core.OneWheelDirectDrivetrain;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;
import team25core.TankMechanumControlSchemeFrenzy;
import team25core.TeleopDriveTask;
import opmodes.MecanumFieldCentricDriveScheme;

@TeleOp(name = "powerplayteleopnofieldcentric")
//@Disabled
public class PowerPlayTeleopNoFieldCentric extends StandardFourMotorRobot {


    private TeleopDriveTask drivetask;

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private BNO055IMU imu;

    //duck carousel



    private Telemetry.Item locationTlm;



    TankMechanumControlSchemeFrenzy scheme;



    private MechanumGearedDrivetrain drivetrain;

    private DcMotor linearLift;
    private CRServo umbrella;
    private DcMotor turret;
    private DeadReckonPath turretTurn;
    private OneWheelDirectDrivetrain turretDrivetrain;


    //what does CR stand for

    @Override
    public void handleEvent(RobotEvent e) {
    }

    //flipover positions for bottom and top positions for intake and placing on hubs


    //alternates between down and up positions.



    @Override
    public void init() {

        super.init();
        initIMU();

        linearLift=hardwareMap.get(DcMotor.class, "linearLift");
        turret =hardwareMap.get(DcMotor.class, "turret");
        turretDrivetrain = new OneWheelDirectDrivetrain(linearLift);
        turretDrivetrain.resetEncoders();
        turretDrivetrain.encodersOn();


        linearLift.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        turret.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);



        umbrella=hardwareMap.crservo.get("umbrella");

        scheme = new TankMechanumControlSchemeFrenzy(gamepad1);



        //code for forward mechanum drivetrain:
        drivetrain = new MechanumGearedDrivetrain(motorMap);
        drivetask = new TeleopDriveTask(this, scheme, frontLeft, frontRight, backLeft, backRight);
        drivetask.slowDown(true);

        locationTlm = telemetry.addData("location","init");

        initPaths();


    }

    private void initPaths() {
        turretTurn = new DeadReckonPath();
        turretTurn.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 0.2, 0.01);
    }

    private void setTurretTurn() {
        this.addTask(new DeadReckonTask(this, turretTurn, turretDrivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {




                }
            }
        });
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
                    case BUTTON_X_DOWN:
                        turret.setPower(0.5);
                        break;
                    case BUTTON_B_DOWN:
                        turret.setPower(-0.5);
                        break;
                    case BUTTON_X_UP:
                        turret.setPower(0);
                        break;
                    case BUTTON_B_UP:
                        turret.setPower(0);
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
                    case LEFT_BUMPER_DOWN:
                        linearLift.setPower(1);
                        break;
                    case RIGHT_BUMPER_DOWN:
                        linearLift.setPower(-1);
                        break;
                    case LEFT_BUMPER_UP:
                        linearLift.setPower(0);
                        break;
                    case RIGHT_BUMPER_UP:
                        linearLift.setPower(0);
                        break;
                    case RIGHT_TRIGGER_DOWN:
                        umbrella.setPower(0.5);
                        break;
                    case LEFT_TRIGGER_DOWN:
                        umbrella.setPower(-0.5);
                        break;
                    case RIGHT_TRIGGER_UP:
                        umbrella.setPower(0);
                        break;
                    case LEFT_TRIGGER_UP:
                        umbrella.setPower(0);
                        break;
                    case BUTTON_A_DOWN:
                        linearLift.setPower(1);
                        break;
                    case BUTTON_Y_DOWN:
                        linearLift.setPower(-1);
                        break;
                    case BUTTON_A_UP:
                        linearLift.setPower(0);
                        break;
                    case BUTTON_Y_UP:
                        linearLift.setPower(0);
                        break;
                    case BUTTON_X_DOWN:
                        setTurretTurn();
                        break;
                    case BUTTON_X_UP:
                        linearLift.setPower(0);
                        break;




                }
            }

        });


    }
}
