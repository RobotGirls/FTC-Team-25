
package opmodes;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import org.swerverobotics.library.ClassFactory;
import org.swerverobotics.library.interfaces.Autonomous;

import java.nio.channels.InterruptibleChannel;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.LightSensorCriteria;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.Team25DcMotor;
import team25core.TwoWheelGearedDriveDeadReckon;
import team25core.UltrasonicAveragingTask;
import team25core.UltrasonicDualSensorCriteria;
import team25core.UltrasonicSensorCriteria;

/*
 * FTC Team 5218: izzielau, February 17, 2016
 */

@Autonomous(name = "RED Target", group = "AutoTeam25")
public class CaffeineRedFollowAutonomous extends Robot {
    protected final static int TURN_MULTIPLIER = 1;

    protected final static int TICKS_PER_DEGREE = NeverlandMotorConstants.ENCODER_TICKS_PER_DEGREE;
    protected final static int TICKS_PER_INCH = NeverlandMotorConstants.ENCODER_TICKS_PER_INCH;

    protected final static int LIGHT_MIN = NeverlandLightConstants.LIGHT_MINIMUM;
    protected final static int LIGHT_MAX = NeverlandLightConstants.LIGHT_MAXIMUM;
    protected final static int LIGHT_MIN_BACK = NeverlandLightConstants.BACK_LIGHT_MINIMUM;
    protected final static int LIGHT_MAX_BACK = NeverlandLightConstants.BACK_LIGHT_MAXIMUM;

    private final static int LED_CHANNEL = 0;

    private DcMotorController mc;
    private Team25DcMotor leftTread;
    private Team25DcMotor rightTread;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private LightSensor frontLight;
    private LightSensor backLight;
    private UltrasonicSensor leftSound;
    private UltrasonicSensor rightSound;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;
    private Servo climber;

    private BeaconArms pushers;
    private BeaconHelper helper;

    private LightSensorCriteria frontLightCriteria;
    private LightSensorCriteria backLightCriteria;
    private LightSensorCriteria frontDarkCriteria;
    private UltrasonicDualSensorCriteria ultrasonicCriteria;
    private UltrasonicSensorCriteria distanceCriteria;
    private UltrasonicAveragingTask ultrasonicLeftAverage;
    private UltrasonicAveragingTask ultrasonicRightAverage;

    private DeadReckon deadReckon;
    private DeadReckonTask deadReckonTask;
    private DeadReckon deadReckonStraight;
    private DeadReckonTask deadReckonStraightTask;
    private DeadReckon deadReckonLightTurn;
    private DeadReckonTask deadReckonLightTurnTask;

    private ElapsedTime elapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    private PersistentTelemetryTask ptt;

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
        frontLight = hardwareMap.lightSensor.get("frontLight");
        frontLight.enableLed(true);
        backLight = hardwareMap.lightSensor.get("backLight");
        backLight.enableLed(true);

        // Right light criteria.
        frontLightCriteria = new LightSensorCriteria(frontLight, LIGHT_MIN, LIGHT_MAX);
        frontDarkCriteria = new LightSensorCriteria(frontLight, LightSensorCriteria.LightPolarity.BLACK, LIGHT_MIN, LIGHT_MAX);
        backLightCriteria = new LightSensorCriteria(backLight, LIGHT_MIN_BACK, LIGHT_MAX_BACK);
        frontLightCriteria.setThreshold(NeverlandLightConstants.RED_THRESHOLD);
        backLightCriteria.setThreshold(NeverlandLightConstants.RED_THRESHOLD);
        frontDarkCriteria.setThreshold(NeverlandLightConstants.RED_THRESHOLD);

        // Ultrasonic.
        leftSound = hardwareMap.ultrasonicSensor.get("leftSound");
        rightSound = hardwareMap.ultrasonicSensor.get("rightSound");

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
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 88, NeverlandAutonomousConstants.SPEED_STRAIGHT);

        // Dead-reckon: straight.
        deadReckonStraight = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonStraight.addSegment(DeadReckon.SegmentType.STRAIGHT, 24, 0.10);

        // Dead-reckon: in case of light.
        deadReckonLightTurn = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonLightTurn.addSegment(DeadReckon.SegmentType.STRAIGHT, 1, -0.251);
        deadReckonLightTurn.addSegment(DeadReckon.SegmentType.TURN, 90, TURN_MULTIPLIER * 0.152);

        // Beacon.
        pushers = new BeaconArms(rightPusher, leftPusher, true);
        helper = new BeaconHelper(BeaconHelper.Alliance.BLUE, this, color, core, pushers, climber, rightTread, leftTread, TICKS_PER_DEGREE, TICKS_PER_INCH);

        ptt = new PersistentTelemetryTask(this);
    }

    @Override
    public void start() {
        // Dead reckon task.
        // Start dead reckon. Run until frontLight sees white or until the path ends.
        deadReckonTask = new DeadReckonTask(this, deadReckon, backLightCriteria) {
            public void handleEvent(RobotEvent e) {
                handleDeadReckonEvent((DeadReckonTask.DeadReckonEvent) e);
            }
        };
        addTask(deadReckonTask);
    }

    protected void handleDeadReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case PATH_DONE:
                /*
                 * We missed the white line.  Turn counter clockwise and see if we can grab it.
                 */
                RobotLog.e("Missed the white line.");
                TwoWheelGearedDriveDeadReckon missedLine = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                missedLine.addSegment(DeadReckon.SegmentType.TURN, 30, TURN_MULTIPLIER * -0.10);
                missedLine.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, -0.10);
                addTask(new DeadReckonTask(this, missedLine, backLightCriteria) {
                    public void handleEvent(RobotEvent e)
                    {
                        DeadReckonEvent ev = (DeadReckonEvent)e;
                        if (ev.kind == EventKind.SENSOR_SATISFIED) {
                            handleDeadReckonEvent(ev);
                        } else {
                            RobotLog.e("Aborting after failing to find white line");
                            ptt.addData("Abort: ", "Failed to find line");
                        }
                    }
                });
                break;
            case SENSOR_SATISFIED:
                // If deadReckonTask is stopped because it saw a white line, turn until the front
                // light sensor sees white.

                RobotLog.e("251 Adding dead reckon turn task for front light sensor");
                deadReckonLightTurnTask = new DeadReckonTask(this, deadReckonLightTurn, frontLightCriteria) {
                    public void handleEvent(RobotEvent e) {
                        elapsedTime.reset();
                        RobotLog.e("251 Finished the turn task");
                        handleStraightReckonEvent((DeadReckonTask.DeadReckonEvent) e);
                    }
                };
                addTask(deadReckonLightTurnTask);
                break;
        }
    }

    protected void handleStraightReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case SEGMENT_DONE:
                RobotLog.e("Aborting because could not find beacon distance");
                ptt.addData("Abort: ", "Beacon approach failed");
                break;
            case SENSOR_SATISFIED:
                RobotLog.i("Doing beacon work");
                ptt.addData("Path Success: ", "Doing beacon work");
                helper = new BeaconHelper(BeaconHelper.Alliance.BLUE, this, color, core, pushers,
                        climber, rightTread, leftTread, TICKS_PER_DEGREE, TICKS_PER_INCH);
                helper.doBeaconWork();
                break;
        }
    }

    @Override
    public void stop()
    {
    }
}

