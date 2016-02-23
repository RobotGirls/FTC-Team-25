package opmodes;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import org.swerverobotics.library.ClassFactory;
import org.swerverobotics.library.interfaces.Autonomous;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.LightSensorCriteria;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.Team25DcMotor;
import team25core.TwoWheelGearedDriveDeadReckon;
import team25core.UltrasonicAveragingTask;
import team25core.UltrasonicDualSensorCriteria;
import team25core.UltrasonicSensorCriteria;

/*
 * FTC Team 5218: izzielau, February 17, 2016
 */

@Autonomous(name = "BLUE Target", group = "AutoTeam25")
public class CaffeineBlueFollowAutonomous extends Robot {

    protected final static int TICKS_PER_DEGREE = NeverlandMotorConstants.ENCODER_TICKS_PER_DEGREE;
    protected final static int TICKS_PER_INCH = NeverlandMotorConstants.ENCODER_TICKS_PER_INCH;

    protected final static int LIGHT_MIN = NeverlandLightConstants.LIGHT_MINIMUM;
    protected final static int LIGHT_MAX = NeverlandLightConstants.LIGHT_MAXIMUM;

    private final static int LED_CHANNEL = 0;

    private DcMotorController mc;
    private Team25DcMotor leftTread;
    private Team25DcMotor rightTread;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private LightSensor light;
    private UltrasonicSensor leftSound;
    private UltrasonicSensor rightSound;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;
    private Servo climber;

    private BeaconArms pushers;
    private BeaconHelper helper;

    private LightSensorCriteria lightCriteria;
    private UltrasonicDualSensorCriteria ultrasonicCriteria;
    private UltrasonicSensorCriteria distanceCriteria;
    private UltrasonicAveragingTask ultrasonicLeftAverage;
    private UltrasonicAveragingTask ultrasonicRightAverage;

    private DeadReckon deadReckon;
    private DeadReckonTask deadReckonTask;
    private DeadReckon deadReckonTurn;
    private DeadReckonTask deadReckonTurnTask;
    private DeadReckon deadReckonStraight;
    private DeadReckonTask deadReckonStraightTask;

    @Override
    public void handleEvent(RobotEvent e) {

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

        // Light.
        light = hardwareMap.lightSensor.get("light");
        light.enableLed(true);

        // Light criteria.
        lightCriteria = new LightSensorCriteria(light, LIGHT_MIN, LIGHT_MAX);

        // Ultrasonic.
        leftSound = hardwareMap.ultrasonicSensor.get("leftSound");
        rightSound = hardwareMap.ultrasonicSensor.get("rightSound");

        // Ultrasonic average.
        ultrasonicLeftAverage = new UltrasonicAveragingTask(this, leftSound, 5);
        addTask(ultrasonicLeftAverage);
        ultrasonicRightAverage = new UltrasonicAveragingTask(this, rightSound, 5);
        addTask(ultrasonicRightAverage);

        // Ultrasonic criteria.
        ultrasonicCriteria = new UltrasonicDualSensorCriteria(ultrasonicLeftAverage,
                                                              ultrasonicRightAverage, 1);

        // Distance criteria.
        distanceCriteria = new UltrasonicSensorCriteria(ultrasonicLeftAverage, 19);

        // Servos.
        climber = hardwareMap.servo.get("climber");
        rightPusher = hardwareMap.servo.get("rightPusher");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightBumper = hardwareMap.servo.get("rightBumper");
        leftBumper = hardwareMap.servo.get("leftBumper");

        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
        leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
        climber.setPosition(NeverlandServoConstants.CLIMBER_STORE);

        // Motor controller.
        mc = hardwareMap.dcMotorController.get("motors");

        // Treads.
        rightTread = new Team25DcMotor(this, mc, 1);
        leftTread = new Team25DcMotor(this, mc, 2);

        // Class factory (motor controller).
        ClassFactory.createEasyMotorController(this, leftTread, rightTread);

        rightTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        rightTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        // Dead-reckon.
        deadReckon = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 100, 0.65);

        // Dead-reckon: turn.
        deadReckonTurn = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonTurn.addSegment(DeadReckon.SegmentType.TURN, 75, 0.251);

        // Dead-reckon: straight:
        deadReckonStraight = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonStraight.addSegment(DeadReckon.SegmentType.STRAIGHT, 24, 0.251);

        // Beacon.
        pushers = new BeaconArms(rightPusher, leftPusher, true);
        helper = new BeaconHelper(BeaconHelper.Alliance.BLUE, this, color, pushers, climber, rightTread, leftTread, TICKS_PER_DEGREE, TICKS_PER_INCH);
    }

    @Override
    public void start() {
        // Dead reckon task.
        // Start dead reckon. Run until light sees white or until the path ends.
        deadReckonTask = new DeadReckonTask(this, deadReckon, lightCriteria) {
            public void handleEvent(RobotEvent e) {
                handleDeadReckonEvent((DeadReckonTask.DeadReckonEvent) e);
            }
        };
        addTask(deadReckonTask);
    }

    protected void handleDeadReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case SEGMENT_DONE:
                break;
            case SENSOR_SATISFIED:
                // If deadReckonTask is stopped because it saw a white line, turn until the ultrasonic
                // sensors return the same value (not zero).
                deadReckonTurnTask = new DeadReckonTask(this, deadReckonTurn, ultrasonicCriteria) {
                    public void handleEvent(RobotEvent e) {
                        handleTurnReckonEvent((DeadReckonTask.DeadReckonEvent) e);
                    }
                };
                addTask(deadReckonTurnTask);
                break;
        }
    }

    protected void handleTurnReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case SEGMENT_DONE:
                break;
            case SENSOR_SATISFIED:
                deadReckonStraightTask = new DeadReckonTask(this, deadReckonStraight, distanceCriteria) {
                    public void handleEvent(RobotEvent e) {
                        handleStraightReckonEvent((DeadReckonTask.DeadReckonEvent)e);
                    }
                };
                addTask(deadReckonStraightTask);
                break;
        }
    }

    protected void handleStraightReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case SEGMENT_DONE:
                break;
            case SENSOR_SATISFIED:
                helper = new BeaconHelper(BeaconHelper.Alliance.BLUE, this, color, pushers, climber, rightTread, leftTread, TICKS_PER_DEGREE, TICKS_PER_INCH);
                helper.doBeaconWork();
                break;
        }

    }

    @Override
    public void stop() {
        if (deadReckonTask != null && deadReckonTurnTask != null && deadReckonStraightTask != null) {
            deadReckonTask.stop();
        }
    }
}


