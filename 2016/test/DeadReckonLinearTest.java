package test;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.Servo;

import opmodes.BeaconArms;
import opmodes.NeverlandServoConstants;
import team25core.DeadReckon;
import team25core.DeadReckonGyroTask;
import team25core.DeadReckonTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.Team25DcMotor;
import team25core.TwoWheelGearedDriveDeadReckon;

/*
 * FTC Team 5218: izzielau, February 06, 2016
 */

@Autonomous(name="TEST Turn", group = "AutoTeam25")
@Disabled
public class DeadReckonLinearTest extends Robot {
    public static final int TICKS_PER_DEGREE = 29;
    private final static int TICKS_PER_INCH = 212;
    private final static int LED_CHANNEL = 0;

    private DcMotorController mc;
    private Team25DcMotor leftTread;
    private Team25DcMotor rightTread;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;

    private TwoWheelGearedDriveDeadReckon deadReckon;
    private DeadReckonTask deadReckonTask;

    @Override
    public void handleEvent(RobotEvent e) {
        // No-op.
    }

    @Override
    public void init()
    {
        // Gyro.
        gyro = (ModernRoboticsI2cGyro)hardwareMap.gyroSensor.get("gyro");
        gyro.setHeadingMode(ModernRoboticsI2cGyro.HeadingMode.HEADING_CARDINAL);
        gyro.calibrate();

        // Color.
        color = hardwareMap.colorSensor.get("color");
        core = hardwareMap.deviceInterfaceModule.get("interface");

        core.setDigitalChannelMode(LED_CHANNEL, DigitalChannelController.Mode.OUTPUT);
        core.setDigitalChannelState(LED_CHANNEL, false);

        // Servos.
        rightPusher = hardwareMap.servo.get("rightPusher");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightBumper = hardwareMap.servo.get("rightBumper");
        leftBumper = hardwareMap.servo.get("leftBumper");

        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
        leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);

        // Motor controller.
        mc = hardwareMap.dcMotorController.get("motors");

        // Treads.
        rightTread = new Team25DcMotor(this, mc, 1);
        leftTread = new Team25DcMotor(this, mc, 2);
        //rightTread.startPeriodic();
        //leftTread.startPeriodic();

        // Class factory (motor controller).
        ClassFactory.createEasyMotorController(this, leftTread, rightTread);

        rightTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        rightTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        // Class: Dead reckon.
        deadReckon = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 48, 0.5);

    }

    @Override
    public void start() {

        deadReckonTask = new DeadReckonGyroTask(this, gyro, deadReckon);
        addTask(deadReckonTask);
    }

}
