package opmodes;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.AutonomousEvent;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.LightSensorCriteria;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.ServoOscillateTask;
import team25core.SingleShotTimerTask;
import team25core.Team25DcMotor;
import team25core.TwoWheelGearedDriveDeadReckon;
import team25core.UltrasonicAveragingTask;
import team25core.UltrasonicSensorCriteria;

/*
 * FTC Team 5218: izzielau, February 17, 2016
 */

@Autonomous(name = "Caffeine Autonomous", group = "5218")
public class CaffeineAutonomous extends Robot {

    protected final static int TICKS_PER_DEGREE = NeverlandMotorConstants.ENCODER_TICKS_PER_DEGREE;
    protected final static int TICKS_PER_INCH = NeverlandMotorConstants.ENCODER_TICKS_PER_INCH;

    protected final static double SPEED_TURN = NeverlandAutonomousConstants.SPEED_TURN;
    protected final static double SPEED_STRAIGHT = NeverlandAutonomousConstants.SPEED_STRAIGHT;
    protected final static double SPEED_TARGET = NeverlandAutonomousConstants.SPEED_TARGET_LINE;

    protected final static int LIGHT_MIN = NeverlandLightConstants.ROOM_LIGHT_MIN;
    protected final static int LIGHT_MAX = NeverlandLightConstants.ROOM_LIGHT_MAX;
    protected final static int LIGHT_MIN_BACK = NeverlandLightConstants.ROOM_BACK_LIGHT_MIN;
    protected final static int LIGHT_MAX_BACK = NeverlandLightConstants.ROOM_BACK_LIGHT_MAX;

    private final static int LED_CHANNEL = 0;
    private static int TURN_MULTIPLY = 0;

    private int START_DELAY;
    private int COMPENSATION_DELAY = NeverlandAutonomousConstants.COMPENSATION_DELAY;

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
    private UltrasonicSensorCriteria distanceMountainCriteria;
    private UltrasonicAveragingTask ultrasonicLeftAverage;
    private UltrasonicAveragingTask ultrasonicRightAverage;
    private GamepadTask gamepad;

    private ElapsedTime elapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    private PersistentTelemetryTask ptt;

    private enum Alliance {
        BLUE,
        RED,
        PURPLE
    }

    private enum AfterBeacon {
        MOVE_TO_MOUNTAIN,
        MOVE_TO_PARK,
        STAY_AT_WASABI_WONTON
    }

    Alliance alliance;
    AfterBeacon afterBeacon;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            switch (event.kind) {
                case BUTTON_X_DOWN:
                    // Blue.
                    alliance = Alliance.BLUE;
                    ptt.addData("ALLIANCE: ", "" + alliance);
                    break;
                case BUTTON_B_DOWN:
                    // Red.
                    alliance = Alliance.RED;
                    ptt.addData("ALLIANCE: ", "" + alliance);
                    break;
                case BUTTON_Y_DOWN:
                    // Add one second to delay.
                    START_DELAY++;
                    ptt.addData("DELAY: ", START_DELAY);
                    break;
                case BUTTON_A_DOWN:
                    // Subtract one second from delay.
                    START_DELAY--;
                    ptt.addData("DELAY: ", START_DELAY);
                    break;
                case LEFT_BUMPER_DOWN:
                    // Park in floor zone.
                    afterBeacon = AfterBeacon.MOVE_TO_PARK;
                    break;
                case RIGHT_BUMPER_DOWN:
                    // Park on low zone of mountain.
                    afterBeacon = AfterBeacon.MOVE_TO_MOUNTAIN;
                    break;
                case LEFT_TRIGGER_DOWN:
                    // Stay at wasabi wonton.
                    afterBeacon = AfterBeacon.STAY_AT_WASABI_WONTON;
                    break;
            }
            beaconTelemetry(afterBeacon);
        } else if (e instanceof AutonomousEvent) {
            AutonomousEvent event = (AutonomousEvent) e;
            if (event.kind == AutonomousEvent.EventKind.BEACON_DONE) {
                addTask(new SingleShotTimerTask(this, 1000) {
                    @Override
                    public void handleEvent(RobotEvent e)
                    {
                        // Wait for timer to finish.
                        SingleShotTimerEvent eva = (SingleShotTimerEvent) e;
                        if (eva.kind == EventKind.EXPIRED) {
                            RobotLog.i("251 Handling autonomous event: ", "" + afterBeacon);
                            handleBeaconReckonEvent(afterBeacon);
                        }
                    }
                });
            }
        }
    }

    @Override
    public void init()
    {

        // Set globals to default so no errors occur.
        alliance = Alliance.PURPLE;
        afterBeacon = AfterBeacon.STAY_AT_WASABI_WONTON;
        START_DELAY = 0;

        // Persistent telemetry task.
        ptt = new PersistentTelemetryTask(this);
        addTask(ptt);

        // Telemetry for autonomous specificity.
        ptt.addData("DELAY: ", START_DELAY);
        ptt.addData("ALLIANCE: ", "NOT SELECTED");
        beaconTelemetry(afterBeacon);

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

        // Ultrasonic.
        leftSound = hardwareMap.ultrasonicSensor.get("leftSound");
        rightSound = hardwareMap.ultrasonicSensor.get("rightSound");

        // Ultrasonic criteria.
        ultrasonicLeftAverage = new UltrasonicAveragingTask(this, leftSound,
                          NeverlandAutonomousConstants.MOVING_AVG_SET_SIZE);
        ultrasonicRightAverage = new UltrasonicAveragingTask(this, rightSound,
                            NeverlandAutonomousConstants.MOVING_AVG_SET_SIZE);
        distanceCriteria = new UltrasonicSensorCriteria(ultrasonicLeftAverage,
                           NeverlandAutonomousConstants.DISTANCE_FROM_BEACON);
        distanceMountainCriteria = new UltrasonicSensorCriteria(ultrasonicRightAverage,
                                 NeverlandAutonomousConstants.DISTANCE_FROM_MOUNTAIN);

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
        // ClassFactory.createEasyMotorController(this, leftTread, rightTread);

        rightTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
        leftTread.setMode(DcMotor.RunMode.RESET_ENCODERS);
        rightTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftTread.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);

        // Beacon.
        pushers = new BeaconArms(rightPusher, leftPusher, true);

        // Creates gamepad task for init_loop().
        gamepad = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1);
        addTask(gamepad);

        // Creates task for servo oscillation.
        /*
        ServoOscillateTask oscillate = new ServoOscillateTask(this, rightPusher, 128, 20);
        addTask(oscillate);

        if (alliance != Alliance.PURPLE) {
            oscillate.stop(NeverlandServoConstants.CLIMBER_STORE);
        }
        */
    }

    public void blueInit()
    {
        TURN_MULTIPLY = NeverlandAutonomousConstants.BLUE_TURN_MULTIPLIER;

        frontLightCriteria.setThreshold(NeverlandLightConstants.BLUE_THRESHOLD);
        backLightCriteria.setThreshold(NeverlandLightConstants.BLUE_THRESHOLD);
        frontDarkCriteria.setThreshold(NeverlandLightConstants.BLUE_THRESHOLD);

        helper = new BeaconHelper(BeaconHelper.Alliance.BLUE, this, color, core, pushers, climber,
                rightTread, leftTread, TICKS_PER_DEGREE, TICKS_PER_INCH);
    }

    public void redInit()
    {
        TURN_MULTIPLY = NeverlandAutonomousConstants.RED_TURN_MULTIPLIER;

        frontLightCriteria.setThreshold(NeverlandLightConstants.RED_THRESHOLD);
        backLightCriteria.setThreshold(NeverlandLightConstants.RED_THRESHOLD);
        frontDarkCriteria.setThreshold(NeverlandLightConstants.RED_THRESHOLD);

        helper = new BeaconHelper(BeaconHelper.Alliance.RED, this, color, core, pushers, climber,
                rightTread, leftTread, TICKS_PER_DEGREE, TICKS_PER_INCH);
    }

    public void beaconTelemetry(AfterBeacon after)
    {
        switch (after) {
            case MOVE_TO_MOUNTAIN:
                ptt.addData("AFTER BEACON: ", "MOUNTAIN");
                break;
            case MOVE_TO_PARK:
                ptt.addData("AFTER BEACON: ", "PARK");
                break;
            case STAY_AT_WASABI_WONTON:
                ptt.addData("AFTER BEACON: ", "WASABI WONTON!");
                break;
        }
    }

    /*
     * Move from the wall to the white line, stopping when we see the white line.
     */
    public void initialMove(final DeadReckon path)
    {
        addTask(new DeadReckonTask(this, path, backLightCriteria) {
            public void handleEvent(RobotEvent e)
            {
                DeadReckonEvent event = (DeadReckonEvent) e;
                switch (event.kind) {
                    case SENSOR_SATISFIED:
                        handleDeadReckonEvent(event);
                        break;
                    case PATH_DONE:
                        /*
                         * We missed the white line.  Turn counter clockwise and see if we can grab it.
                         */
                        RobotLog.e("251 Initial move missed the white line.");

                        // (1) Turn 30 degrees.
                        // (2) Move backwards to move back onto the line OR stop if sensor sees white.
                        TwoWheelGearedDriveDeadReckon missedLine = new TwoWheelGearedDriveDeadReckon
                                (this.robot, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                        missedLine.addSegment(DeadReckon.SegmentType.TURN, 40, -SPEED_TURN * TURN_MULTIPLY);
                        missedLine.addSegment(DeadReckon.SegmentType.STRAIGHT, 12, -0.75 * SPEED_STRAIGHT);

                        addTask(new DeadReckonTask(this.robot, missedLine, backLightCriteria) {
                            public void handleEvent(RobotEvent e)
                            {
                                DeadReckonEvent ev = (DeadReckonEvent) e;
                                if (ev.kind == EventKind.SENSOR_SATISFIED) {
                                    handleDebrisReckonEvent(ev);
                                } else {
                                    RobotLog.e("Aborting after failing to find white line");
                                    ptt.addData("Abort: ", "Failed to find line");
                                }
                            }
                        });
                        break;
                    default:
                        RobotLog.e("251 Unknown event kind");
                }
            }
        });
    }

    @Override
    public void start()
    {
        if (alliance == Alliance.BLUE) {
            blueInit();
        } else if (alliance == Alliance.RED) {
            redInit();
        }

        RobotLog.e("251 Starting initial straight segment");

        final TwoWheelGearedDriveDeadReckon targetingLine = new TwoWheelGearedDriveDeadReckon
                             (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        targetingLine.addSegment(DeadReckon.SegmentType.STRAIGHT, 90, SPEED_TARGET);

        if (START_DELAY > 0) {
            // Timer is a little fast, so only take 90% of the initial delay.
            addTask(new SingleShotTimerTask(this, COMPENSATION_DELAY * START_DELAY) {
                @Override
                public void handleEvent(RobotEvent e)
                {
                    // (1) Move straight 90 inches OR until front light sensor sees the white line.
                    initialMove(targetingLine);
                }
            });
        } else {
            initialMove(targetingLine);
        }
    }

    protected void handleDeadReckonEvent(DeadReckonTask.DeadReckonEvent e)
    {
        switch (e.kind) {
            case SENSOR_SATISFIED:
                RobotLog.e("251 Adding dead reckon tasks for pushing debris out of the way");
                TwoWheelGearedDriveDeadReckon pushDebris = new TwoWheelGearedDriveDeadReckon
                            (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                pushDebris.addSegment(DeadReckon.SegmentType.STRAIGHT, 3, SPEED_STRAIGHT);

                // (1) Move forward to push away debris.
                // (2) Move backwards 8 inches OR until back light sensor sees the white line.
                addTask(new DeadReckonTask(this, pushDebris) {
                    public void handleEvent(RobotEvent e)
                    {
                        handleDebrisReckonEvent((DeadReckonTask.DeadReckonEvent) e);
                    }
                });
                break;
        }
    }

    protected void handleDebrisReckonEvent(DeadReckonTask.DeadReckonEvent e)
    {
        switch(e.kind) {
            case PATH_DONE:
                TwoWheelGearedDriveDeadReckon moveBack = new TwoWheelGearedDriveDeadReckon
                        (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                moveBack.addSegment(DeadReckon.SegmentType.STRAIGHT, 8, -SPEED_STRAIGHT);

                addTask(new DeadReckonTask(this, moveBack, backLightCriteria) {
                    public void handleEvent(RobotEvent e)
                    {
                        DeadReckonEvent event = (DeadReckonEvent) e;
                        RobotLog.e("251 Finished the turn task");

                        if (event.kind == EventKind.SENSOR_SATISFIED) {
                            RobotLog.i("251 Robot has pushed debris out of the way and re-found line");
                            handleDebrisReckonEvent((DeadReckonTask.DeadReckonEvent) e);
                        } else {
                            RobotLog.e("251 Robot over-rotated backwards");
                        }
                    }
                });
                break;
            case SENSOR_SATISFIED:
                RobotLog.i("251 Pushing debris out of the way");

                // (1) Over-turn right OR until front light sensor sees the white line.
                TwoWheelGearedDriveDeadReckon searchWhiteLine = new TwoWheelGearedDriveDeadReckon
                                 (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                searchWhiteLine.addSegment(DeadReckon.SegmentType.STRAIGHT, 1, -SPEED_STRAIGHT);
                searchWhiteLine.addSegment(DeadReckon.SegmentType.TURN, 120, TURN_MULTIPLY * SPEED_TURN);

                addTask(new DeadReckonTask(this, searchWhiteLine, frontLightCriteria) {
                    public void handleEvent(RobotEvent e)
                    {
                        // elapsedTime.reset();
                        DeadReckonEvent event = (DeadReckonEvent) e;
                        RobotLog.e("251 Finished the turn task");

                        if (event.kind == EventKind.SENSOR_SATISFIED) {
                            RobotLog.i("251 Robot is parallel to the white line");
                            handleAlignedReckonEvent((DeadReckonTask.DeadReckonEvent) e);
                        } else {
                            RobotLog.e("251 Overturn did not catch the white line");
                        }
                    }
                });
            break;
        }
    }

    protected void handleAlignedReckonEvent(DeadReckonTask.DeadReckonEvent e)
    {
        switch (e.kind) {
            case PATH_DONE:
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
                moveToBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 24, 0.5 * SPEED_STRAIGHT);

                addTask(new DeadReckonTask(this, moveToBeacon, distanceCriteria) {
                    public void handleEvent(RobotEvent e)
                    {
                        elapsedTime.reset();
                        RobotLog.e("251 Finished moving in front of beacon");
                        handleStraightReckonEvent((DeadReckonTask.DeadReckonEvent) e);
                    }
                });
                break;
        }
    }

    protected void handleStraightReckonEvent(DeadReckonTask.DeadReckonEvent e)
    {
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

    protected void handleBeaconReckonEvent(AfterBeacon afterBeacon)
    {
        climber.setPosition(NeverlandServoConstants.CLIMBER_STORE);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);

        if (afterBeacon == AfterBeacon.MOVE_TO_PARK) {
            RobotLog.i("251 After beacon is MOVE_TO_PARK");
            // Turns on the right ultrasonic sensor.
            addTask(ultrasonicRightAverage);

            TwoWheelGearedDriveDeadReckon parkDeadReckon = new TwoWheelGearedDriveDeadReckon
                            (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);

            // (1) Move backwards 8 inches.
            // (2) Turn 90 degrees.
            // (3) Move forwards 28 inches at a faster power.
            parkDeadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 8.0, -SPEED_STRAIGHT);
            parkDeadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, 1.15 * SPEED_TURN * TURN_MULTIPLY);
            parkDeadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 24.0, 1.5 * SPEED_STRAIGHT);

            addTask(new DeadReckonTask(this, parkDeadReckon));
        } else if (afterBeacon == AfterBeacon.MOVE_TO_MOUNTAIN) {
            RobotLog.i("After beacon is MOVE_TO_MOUNTAIN");
            TwoWheelGearedDriveDeadReckon mountainDeadReckon = new TwoWheelGearedDriveDeadReckon
                                (this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
            mountainDeadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 24.0, -SPEED_STRAIGHT);
            mountainDeadReckon.addSegment(DeadReckon.SegmentType.TURN, 90, SPEED_TURN * TURN_MULTIPLY);
            mountainDeadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 28, SPEED_STRAIGHT);
            mountainDeadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, SPEED_TURN * -TURN_MULTIPLY);

            addTask(new DeadReckonTask(this, mountainDeadReckon));
        }
    }

    @Override
    public void stop()
    {
    }
}

