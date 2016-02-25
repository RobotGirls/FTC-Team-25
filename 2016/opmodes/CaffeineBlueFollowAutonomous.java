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
        light = hardwareMap.lightSensor.get("light");
        light.enableLed(true);

        // Light criteria.
        lightCriteria = new LightSensorCriteria(light, LIGHT_MIN, LIGHT_MAX);

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
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 100, 0.65);

        // Dead-reckon: turn.
        deadReckonTurn = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonTurn.addSegment(DeadReckon.SegmentType.TURN, NeverlandAutonomousConstants.TURN_TOWARD_BEACON, 0.10);

        // Dead-reckon: straight:
        deadReckonStraight = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonStraight.addSegment(DeadReckon.SegmentType.STRAIGHT, 24, 0.10);

        // Beacon.
        pushers = new BeaconArms(rightPusher, leftPusher, true);
        helper = new BeaconHelper(BeaconHelper.Alliance.BLUE, this, color, core, pushers, climber, rightTread, leftTread, TICKS_PER_DEGREE, TICKS_PER_INCH);

        ptt = new PersistentTelemetryTask(this);
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
            case PATH_DONE:
                /*
                 * We missed the white line.  Turn counter clockwise and see if we can grab it.
                 */
                RobotLog.e("Missed the white line.");
                TwoWheelGearedDriveDeadReckon missedLine = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                missedLine.addSegment(DeadReckon.SegmentType.TURN, 30, -0.10);
                missedLine.addSegment(DeadReckon.SegmentType.STRAIGHT, -12, 0.10);
                addTask(new DeadReckonTask(this, missedLine, lightCriteria) {
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
                // If deadReckonTask is stopped because it saw a white line, turn until the ultrasonic
                // sensors return the same value (not zero).
                ultrasonicLeftAverage = new UltrasonicAveragingTask(this, leftSound, NeverlandAutonomousConstants.MOVING_AVG_SET_SIZE);
                ultrasonicRightAverage = new UltrasonicAveragingTask(this, rightSound, NeverlandAutonomousConstants.MOVING_AVG_SET_SIZE);
                addTask(ultrasonicLeftAverage);
                addTask(ultrasonicRightAverage);
                ultrasonicCriteria = new UltrasonicDualSensorCriteria(ultrasonicLeftAverage,
                        ultrasonicRightAverage, NeverlandAutonomousConstants.ULTRASONIC_DIFFERENCE);

                addTask(new SingleShotTimerTask(this, NeverlandAutonomousConstants.DELAY_BEFORE_TURN) {
                            @Override
                            public void handleEvent(RobotEvent e)
                            {
                                if (ultrasonicCriteria.satisfied()) {
                                    /*
                                     * Something is wrong.  We can't be satisfied without having turned.  So abort.
                                     */
                                    RobotLog.e("Aborting after hitting line because ultrasonic sensors broken");
                                    ptt.addData("Abort: ", "Ultrasonic sensors both failed");
                                } else {
                                    elapsedTime.reset();
                                    deadReckonTurnTask = new DeadReckonTask(this.robot, deadReckonTurn, ultrasonicCriteria) {
                                        public void handleEvent(RobotEvent e)
                                        {
                                            if (elapsedTime.time() <= NeverlandAutonomousConstants.TURN_SAFETY_TIME) {
                                                RobotLog.e("Aborting because we could not possibly have turned enough");
                                                ptt.addData("Abort: ", "Impossibly short turn");
                                            } else {
                                                handleTurnReckonEvent((DeadReckonTask.DeadReckonEvent) e);
                                            }
                                        }
                                    };
                                    addTask(deadReckonTurnTask);
                                }
                            }
                        });
                break;
        }
    }

    protected void handleTurnReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case PATH_DONE:
                /*
                 * We moved through square to the wall.
                 */
                RobotLog.e("Rotated past square to the wall");
                double min = ultrasonicRightAverage.getMin();
                if (min < ultrasonicRightAverage.getAverage()) {
                    RobotLog.i("Last ditch effort to square to the wall");
                    TwoWheelGearedDriveDeadReckon lastDitchTurn = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
                    lastDitchTurn.addSegment(DeadReckon.SegmentType.TURN, 60, -0.10);
                    distanceCriteria = new UltrasonicSensorCriteria(ultrasonicRightAverage, (int)min);
                    addTask(new DeadReckonTask(this, lastDitchTurn, distanceCriteria) {
                        public void handleEvent(RobotEvent e) {
                            handleStraightReckonEvent((DeadReckonTask.DeadReckonEvent)e);
                        }
                    });
                } else {
                    RobotLog.e("Aborting everything is completely broken");
                    ptt.addData("Abort: ", "Rotated past wall no recovery");
                }
                break;
            case SENSOR_SATISFIED:
                distanceCriteria = new UltrasonicSensorCriteria(ultrasonicRightAverage, NeverlandAutonomousConstants.DISTANCE_FROM_BEACON);
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
                RobotLog.e("Aborting because could not find beacon distance");
                ptt.addData("Abort: ", "Beacon approach failed");
                break;
            case SENSOR_SATISFIED:
                RobotLog.i("Doing beacon work");
                ptt.addData("Path Success: ", "Doing beacon work");
                helper = new BeaconHelper(BeaconHelper.Alliance.BLUE, this, color, core, pushers, climber, rightTread, leftTread, TICKS_PER_DEGREE, TICKS_PER_INCH);
                helper.doBeaconWork();
                break;
        }

    }

    @Override
    public void stop()
    {
    }
}


