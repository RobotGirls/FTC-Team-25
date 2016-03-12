
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
public class CaffeineRedTargetAutonomous extends Robot {

    protected final static int TURN_MULTIPLIER = NeverlandAutonomousConstants.RED_TURN_MULTIPLIER;
    protected final static int START_DELAY = NeverlandAutonomousConstants.DELAY_BEFORE_START;
    protected final static int TICKS_PER_DEGREE = NeverlandMotorConstants.ENCODER_TICKS_PER_DEGREE;
    protected final static int TICKS_PER_INCH = NeverlandMotorConstants.ENCODER_TICKS_PER_INCH;

    protected final static double SPEED_TURN = NeverlandAutonomousConstants.SPEED_TURN;
    protected final static double SPEED_STRAIGHT = NeverlandAutonomousConstants.SPEED_STRAIGHT;

    protected final static int LIGHT_MIN = NeverlandLightConstants.ROOM_LIGHT_MIN;
    protected final static int LIGHT_MAX = NeverlandLightConstants.ROOM_LIGHT_MAX;
    protected final static int LIGHT_MIN_BACK = NeverlandLightConstants.ROOM_BACK_LIGHT_MIN;
    protected final static int LIGHT_MAX_BACK = NeverlandLightConstants.ROOM_BACK_LIGHT_MAX;

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
    private UltrasonicSensorCriteria distanceCriteria;
    private UltrasonicAveragingTask ultrasonicLeftAverage;

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
        frontDarkCriteria = new LightSensorCriteria(frontLight, LightSensorCriteria.LightPolarity.BLACK,
                LIGHT_MIN, LIGHT_MAX);
        backLightCriteria = new LightSensorCriteria(backLight, LIGHT_MIN_BACK, LIGHT_MAX_BACK);
        frontLightCriteria.setThreshold(NeverlandLightConstants.RED_THRESHOLD);
        backLightCriteria.setThreshold(NeverlandLightConstants.RED_THRESHOLD);
        frontDarkCriteria.setThreshold(NeverlandLightConstants.RED_THRESHOLD);

        // Ultrasonic.
        leftSound = hardwareMap.ultrasonicSensor.get("leftSound");
        rightSound = hardwareMap.ultrasonicSensor.get("rightSound");

        // Ultrasonic criteria.
        ultrasonicLeftAverage = new UltrasonicAveragingTask(this, leftSound, 4);
        distanceCriteria = new UltrasonicSensorCriteria(ultrasonicLeftAverage,
                NeverlandAutonomousConstants.DISTANCE_FROM_BEACON);

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

        // Beacon.
        pushers = new BeaconArms(rightPusher, leftPusher, true);
        helper = new BeaconHelper(BeaconHelper.Alliance.RED, this, color, core, pushers, climber,
                rightTread, leftTread, TICKS_PER_DEGREE, TICKS_PER_INCH);

        ptt = new PersistentTelemetryTask(this);
    }

    @Override
    public void start() {
        // Adds delay before start task, if the delay is not zero.
        if (START_DELAY != 0) {
            addTask(new SingleShotTimerTask(this, START_DELAY) {
                @Override
                public void handleEvent(RobotEvent e) {
                }
            });
        }

        RobotLog.e("Starting initial straight segment.");

        TwoWheelGearedDriveDeadReckon targetingLine = new TwoWheelGearedDriveDeadReckon
                (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        targetingLine.addSegment(DeadReckon.SegmentType.STRAIGHT, 88, 0.75 * SPEED_STRAIGHT);

        addTask(new DeadReckonTask(this, targetingLine, backLightCriteria) {
            public void handleEvent(RobotEvent e)
            {
                DeadReckonEvent ev = (DeadReckonEvent)e;
                if (ev.kind == EventKind.SENSOR_SATISFIED) {
                    handleDeadReckonEvent(ev);
                } else {
                    RobotLog.e("251 Initial move missed the white line.");
                }
            }
        });
    }

    protected void handleDeadReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case PATH_DONE:
                /*
                 * We missed the white line.  Turn counter clockwise and see if we can grab it.
                 */
                RobotLog.e("251 Missed the white line.");

                // (1) Turn 30 degrees.
                // (2) Move backwards to move back onto the line OR stop if sensor sees white.
                TwoWheelGearedDriveDeadReckon missedLine = new TwoWheelGearedDriveDeadReckon
                        (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                missedLine.addSegment(DeadReckon.SegmentType.TURN, 30, -SPEED_TURN * TURN_MULTIPLIER);
                missedLine.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, -0.75 * SPEED_STRAIGHT);

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
                RobotLog.e("251 Adding dead reckon turn task for front light sensor");

                // (1) Move forward to account for off-centered beacon placement.
                // (2) Over-turn right OR until front light sensors sees the white line.
                TwoWheelGearedDriveDeadReckon searchWhiteLine = new TwoWheelGearedDriveDeadReckon
                        (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                searchWhiteLine.addSegment(DeadReckon.SegmentType.STRAIGHT, 1, -SPEED_STRAIGHT);
                searchWhiteLine.addSegment(DeadReckon.SegmentType.TURN, 120, TURN_MULTIPLIER * SPEED_TURN);

                addTask(new DeadReckonTask(this, searchWhiteLine, frontLightCriteria) {
                    public void handleEvent(RobotEvent e) {
                        // elapsedTime.reset();
                        DeadReckonEvent event = (DeadReckonEvent) e;
                        RobotLog.e("251 Finished the turn task");

                        if (event.kind == EventKind.SENSOR_SATISFIED) {
                            RobotLog.i("251 Robot is parallel to the white line");
                            handleAlignedReckonEvent((DeadReckonTask.DeadReckonEvent)e);
                        } else {
                            RobotLog.e("251 Overturn did not catch the white line");
                        }
                    }
                });
                break;
        }
    }

    protected void handleAlignedReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case SEGMENT_DONE:
                RobotLog.e("251 Aborting because the light sensors are not working");
                ptt.addData("Abort: ", "Beacon approach failed");
                break;
            case SENSOR_SATISFIED:
                RobotLog.i("251 Moving forward to beacon, using ultrasonic sensor");
                ptt.addData("Path Success: ", "Both light sensors aligned with the white line");

                // Turns on left ultrasonic sensor.
                addTask(ultrasonicLeftAverage);

                // (1) Moves forward OR until ultrasonic sensor is certain distance away from wall.
                TwoWheelGearedDriveDeadReckon moveToBeacon = new TwoWheelGearedDriveDeadReckon
                        (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                moveToBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 24, SPEED_STRAIGHT);

                addTask(new DeadReckonTask(this, moveToBeacon, distanceCriteria) {
                    public void handleEvent(RobotEvent e) {
                        elapsedTime.reset();
                        RobotLog.e("251 Finished moving in front of beacon");
                        handleStraightReckonEvent((DeadReckonTask.DeadReckonEvent) e);
                    }
                });
                break;
        }
    }

    protected void handleStraightReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case SEGMENT_DONE:
                RobotLog.e("251 Aborting because could not find beacon distance");
                ptt.addData("Abort: ", "Beacon approach failed");
                break;
            case SENSOR_SATISFIED:
                RobotLog.i("251 Doing beacon work");
                ptt.addData("Path Success: ", "Doing beacon work");

                // (1) Swings out left pusher (servo with color sensor).
                // (2) Senses color of beacon.
                // (3) Swings correct pusher out to press beacon button.
                // (4) Dispenses climbers.
                helper.doBeaconWork();
                break;
        }
    }

    @Override
    public void stop()
    {
    }
}

