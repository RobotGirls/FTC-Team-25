package test;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.HashSet;

import opmodes.BeaconArms;
import opmodes.NeverlandServoConstants;
import team25core.ColorSensorTask;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.MonitorGyroTask;
import team25core.MonitorMotorTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.Team25DcMotor;
import team25core.TwoWheelGearedDriveDeadReckon;

/*
 * FTC Team 5218: izzielau, January 30, 2016
 */

@Autonomous(name="TEST Beacon", group = "AutoTest")
@Disabled
public class BeaconTest extends Robot {

    public static final int TICKS_PER_DEGREE = 23;
    private final static int TICKS_PER_INCH = 159;
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
    private Servo climber;

    private TwoWheelGearedDriveDeadReckon deadReckon;
    private TwoWheelGearedDriveDeadReckon deadReckonPush;
    private DeadReckonTask deadReckonPushTask;
    private DeadReckonTask deadReckonTask;
    private MonitorGyroTask monitorGyroTask;
    public  BeaconArms pushers;
    private PersistentTelemetryTask telemetryTask = new PersistentTelemetryTask(this);

    private MonitorMotorTask monitorMotorTask;

    @Override
    public void start() {
        addTask(telemetryTask);

        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_DEPLOYED) ;
        addTask(new SingleShotTimerTask(this, 1000) {
            @Override
            public void handleEvent(RobotEvent e) {
                super.handleEvent(e);
                handleBeacon();
            }
        });
    }

    @Override
    public void init() {
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
        climber = hardwareMap.servo.get("climber");

        HashSet<Servo> servos = new HashSet<Servo>();
        servos.add(rightPusher);
        servos.add(leftPusher);
        servos.add(rightBumper);
        servos.add(leftBumper);
        servos.add(climber);

        pushers = new BeaconArms(rightPusher, leftPusher, true);

        // Motor controller.
        mc = hardwareMap.dcMotorController.get("motors");

        // Treads.
        rightTread = new Team25DcMotor(this, mc, 1);
        leftTread = new Team25DcMotor(this, mc, 2);
        rightTread.startPeriodic();
        leftTread.startPeriodic();

        // Motors.
        rightTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
        leftTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
        rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);

        // Servos.
        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
        leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
        climber.setPosition(NeverlandServoConstants.CLIMBER_SCORE);

        // Class: Dead reckon (push beacon button).
        deadReckonPush = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 8, 1.0);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 8, -0.4);
    }

    @Override
    public void handleEvent(RobotEvent event) {
    }

    public void handleBeacon() {
        addTask(new ColorSensorTask(this, color, core, true, true, LED_CHANNEL) {
            public void handleEvent(RobotEvent e) {
                final BeaconArms pushers = new BeaconArms(rightPusher, leftPusher, true);
                ColorSensorEvent event = (ColorSensorEvent) e;

                if (event.kind == EventKind.RED) {
                    // Hit with left pusher.
                    pushers.rightStow();
                    pushers.colorDeploy();
                    telemetryTask.addData("Color: ", "RED");
                } else if (event.kind == EventKind.BLUE) {
                    // Hit with right pusher.
                    pushers.leftStow();
                    pushers.rightDeploy();
                    telemetryTask.addData("Color: ", "BLUE");
                } else {
                    pushers.allStow();
                    telemetryTask.addData("Color: ", "PURPLE");
                }

                climber.setPosition(NeverlandServoConstants.CLIMBER_SCORE);
                deadReckonPushTask = new DeadReckonTask(robot, deadReckonPush);
                addTask(deadReckonPushTask);
            }
        });
    }
}
