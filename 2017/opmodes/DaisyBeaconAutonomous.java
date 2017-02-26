package opmodes;

import com.qualcomm.hardware.ams.AMSColorSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.I2cController;
import com.qualcomm.robotcore.hardware.I2cDeviceImpl;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import team25core.AMSColorSensorImproved;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.GyroTask;
import team25core.MRLightSensor;
import team25core.MecanumGearedDriveDeadReckon;
import team25core.NavigateToTargetTask;
import team25core.OpticalDistanceSensorCriteria;
import team25core.PersistentTelemetryTask;
import team25core.RangeSensorCriteria;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RobotNavigation;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/5/2016.
 */
@Autonomous(name = "Daisy: Beacon Autonomous", group = "Team25")
public class DaisyBeaconAutonomous extends Robot
{
    enum TargetState {
        FIND_TARGET,
        LOST_TARGET,
        INITIAL_APPROACH,
        AT_TARGET,
        ALIGNED,
    }

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor launcher;
    private DcMotor conveyor;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo swinger;
    private Servo capServo;
    //private ColorSensor colorSensor;
    private AMSColorSensorImproved colorSensor;
    private OpticalDistanceSensor frontOds;
    private MRLightSensor frontLight;
    private DistanceSensor rangeSensor;
    private GyroSensor gyroSensor;
    private DeviceInterfaceModule cdim;
    private DeadReckonTask deadReckonParkTask;
    private BeaconHelper helper;
    private BeaconArms buttonPushers;
    private PersistentTelemetryTask ptt;
    RobotNavigation nav;
    NavigateToTargetTask nttt;
    NavigateToTargetTask red_near;
    NavigateToTargetTask red_far;
    NavigateToTargetTask blue_near;
    NavigateToTargetTask blue_far;
    private MecanumGearedDriveDeadReckon parkPath;
    private MecanumGearedDriveDeadReckon approachBeacon;
    private MecanumGearedDriveDeadReckon approachNext;
    private MecanumGearedDriveDeadReckon adjustTurn;
    private FourWheelDirectDrivetrain drivetrain;
    private final int TICKS_PER_INCH = Daisy.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = Daisy.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = Daisy.STRAIGHT_SPEED;
    private final double TURN_SPEED = Daisy.TURN_SPEED;
    private final int LAUNCH_POSITION = Daisy.LAUNCH_POSITION;
    private int turnMultiplier = 1;
    private int launchSelection = 0;
    private int beaconSelection = 0;
    private int target = 90;
    private boolean launched;
    private boolean goToNext;
    private RunToEncoderValueTask launchParticleTask;
    private SingleShotTimerTask stt;
    OpticalDistanceSensorCriteria frontLightCriteria;
    RangeSensorCriteria rangeSensorCriteria;

    private NavigateToTargetTask.Targets vuforiaTarget;
    private NavigateToTargetTask.Targets secondTarget;

    I2cController.I2cPortReadyCallback colorSensorCallback;
    I2cController.I2cPortReadyCallback rangeSensorCallback;
    private final int COLOR_PORT = Daisy.COLOR_PORT;
    private final int RANGE_PORT = Daisy.RANGE_PORT;

    private Alliance alliance = Alliance.RED;
    private AutonomousPath pathChoice = AutonomousPath.STAY;
    private AutonomousAction actionChoice = AutonomousAction.LAUNCH_0;
    private AutonomousBeacon beaconChoice = AutonomousBeacon.BEACON_0;

    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = Daisy.CAMERA_CHOICE;

    public enum Alliance {
        RED,
        BLUE,
    }

    public enum AutonomousPath {
        CORNER_PARK,
        CENTER_PARK,
        STAY,
    }

    public enum AutonomousAction {
        LAUNCH_0,
        LAUNCH_1,
        LAUNCH_2,
    }

    public enum AutonomousBeacon {
        BEACON_0,
        BEACON_1,
        BEACON_2,
    }

    @Override
    public void init()
    {
        // Hardware mapping.
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        launcher    = hardwareMap.dcMotor.get("launcher");
        conveyor    = hardwareMap.dcMotor.get("conveyor");
        leftPusher  = hardwareMap.servo.get("leftPusher");
        rightPusher = hardwareMap.servo.get("rightPusher");
        swinger     = hardwareMap.servo.get("odsSwinger");
        capServo    = hardwareMap.servo.get("capServo");
        //colorSensor = hardwareMap.colorSensor.get("color");
        cdim        = hardwareMap.deviceInterfaceModule.get("cdim");
        colorSensor = AMSColorSensorImproved.create(AMSColorSensor.Parameters.createForAdaFruit(), new I2cDeviceImpl(cdim, 0));
        rangeSensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "range");
        gyroSensor  = hardwareMap.gyroSensor.get("gyroSensor");

        drivetrain  = new FourWheelDirectDrivetrain(TICKS_PER_INCH, frontRight, rearRight, frontLeft, rearLeft);

        // Callback.
        this.colorSensorCallback = cdim.getI2cPortReadyCallback(COLOR_PORT);
        this.rangeSensorCallback = cdim.getI2cPortReadyCallback(RANGE_PORT);

        // Initialize pushers.
        leftPusher.setPosition(Daisy.LEFT_STOW_POS);
        rightPusher.setPosition(Daisy.RIGHT_STOW_POS);
        swinger.setPosition(0.7);
        capServo.setPosition(0.8);

        // Optical Distance Sensor (front) setup.
        frontOds = hardwareMap.opticalDistanceSensor.get("frontLight");
        frontLight = new MRLightSensor(frontOds);
        frontLightCriteria = new OpticalDistanceSensorCriteria(frontLight, Daisy.ODS_MIN, Daisy.ODS_MAX);

        // Path setup.
        approachBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        parkPath = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        approachNext = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        adjustTurn = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);

        // Launch setup.
        launchParticleTask = new RunToEncoderValueTask(this, launcher, LAUNCH_POSITION, 1.0);
        launched = false;

        // Single shot timer task for reloading launcher.
        stt = new SingleShotTimerTask(this, 2000);

        // Reset encoders.
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launcher.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Button pushers setup.
        buttonPushers = new BeaconArms(this, leftPusher, rightPusher, Daisy.LEFT_DEPLOY_POS, Daisy.RIGHT_DEPLOY_POS, Daisy.LEFT_STOW_POS, Daisy.RIGHT_STOW_POS, true);

        // Telemetry setup.
        ptt = new PersistentTelemetryTask(this);
        this.addTask(ptt);
        ptt.addData("BEACON", "Claim 0 Beacons");
        ptt.addData("LAUNCH", "Launch 0 Balls");
        ptt.addData("ALLIANCE", "Red");
        ptt.addData("For additional help, press", "(Y)");

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        // Gyro calibration.
        gyroSensor.calibrate();

        // Vuforia setup.
        nav = new RobotNavigation(this, drivetrain);
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = Daisy.KEY;
        parameters.cameraDirection = CAMERA_CHOICE;
        parameters.useExtendedTracking = false;

        VuforiaLocalizer vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        VuforiaTrackables targets = vuforia.loadTrackablesFromAsset("FTC_2016-17");
        targets.get(0).setName("Blue Near");
        targets.get(1).setName("Red Far");
        targets.get(2).setName("Blue Far");
        targets.get(3).setName("Red Near");

        OpenGLMatrix phoneLocationOnRobot = Daisy.PHONE_LOCATION_ON_ROBOT;

        nttt = new NavigateToTargetTask(this, drivetrain, NavigateToTargetTask.Targets.RED_NEAR, 1000000, gamepad1);
        nttt.init(targets, parameters, phoneLocationOnRobot);

        /*
        red_far = new NavigateToTargetTask(this, drivetrain, NavigateToTargetTask.Targets.RED_FAR, 1000000, gamepad1);
        red_far.init(targets, parameters, phoneLocationOnRobot);

        blue_near = new NavigateToTargetTask(this, drivetrain, NavigateToTargetTask.Targets.BLUE_NEAR, 1000000, gamepad1);
        blue_near.init(targets, parameters, phoneLocationOnRobot);

        blue_far = new NavigateToTargetTask(this, drivetrain, NavigateToTargetTask.Targets.BLUE_FAR, 1000000, gamepad1);
        blue_far.init(targets, parameters, phoneLocationOnRobot);
        */
    }

    @Override
    public void start()
    {
        pathSetup(pathChoice);
        pathSetup(beaconChoice);
        deadReckonParkTask = new DeadReckonTask(this, parkPath);

        // Launch first particle.
       if (actionChoice != AutonomousAction.LAUNCH_0) {
           addTask(launchParticleTask);
       } else {
           approachBeacon(approachBeacon);
           RobotLog.i("141 Approaching first beacon.");
       }
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            switch(event.kind) {
                case BUTTON_X_DOWN:
                    selectAlliance(Alliance.BLUE);
                    ptt.addData("ALLIANCE", "Blue");
                    break;
                case BUTTON_B_DOWN:
                    selectAlliance(Alliance.RED);
                    ptt.addData("ALLIANCE", "Red");
                    break;
                case LEFT_BUMPER_DOWN:
                    filterLaunchSelection();
                    break;
                case RIGHT_BUMPER_DOWN:
                    filterBeaconSelection();
                    break;
                case BUTTON_Y_DOWN:
                    ptt.addData("Press (X) to select", "Blue alliance!");
                    ptt.addData("Press (B) to select", "Red alliance!");
                    ptt.addData("Press (LEFT BUMPER) to select", "Launch!");
                    ptt.addData("Press (RIGHT BUMPER) to select", "Beacons!");
                    break;
            }
        }

        if (e instanceof RunToEncoderValueTask.RunToEncoderValueEvent) {
            RunToEncoderValueTask.RunToEncoderValueEvent event = (RunToEncoderValueTask.RunToEncoderValueEvent) e;
            if (event.kind == RunToEncoderValueTask.EventKind.DONE) {
                // Load another particle or continue.
                handleEncoderEvent();
            }
        }

        if (e instanceof SingleShotTimerTask.SingleShotTimerEvent) {
            // Launch second particle.
            conveyor.setPower(0);
            addTask(launchParticleTask);
        }

        // Once robot is at the target, push beacon.
        if (e instanceof NavigateToTargetTask.NavigateToTargetEvent) {
            NavigateToTargetTask.NavigateToTargetEvent event = (NavigateToTargetTask.NavigateToTargetEvent) e;
            switch (event.kind) {
                case INITIAL_APPROACH_AXIAL:
                    leftPusher.setPosition(Daisy.LEFT_DEPLOY_POS);
                    rightPusher.setPosition(Daisy.RIGHT_STOW_POS);
                    break;
                case AT_TARGET:
                    drivetrain.setCanonicalMotorDirection();
                    helper.doBeaconWork();
                    vuforiaTarget = secondTarget;
                    break;
            }
        }
    }

    private void handleEncoderEvent()
    {
        if (!launched && actionChoice == AutonomousAction.LAUNCH_2) {
            // Reload the launcher using the conveyor belt.
            conveyor.setPower(0.5);
            addTask(stt);
            launched = true;
            RobotLog.i("141 Reloading launcher.");
        } else if (beaconChoice != AutonomousBeacon.BEACON_0){
            // Begin to approach the beacon.
            approachBeacon(approachBeacon);
            RobotLog.i("141 Approaching first beacon.");
        }
    }

    private void approachBeacon(MecanumGearedDriveDeadReckon path)
    {
        drivetrain.setCanonicalMotorDirection();
        this.addTask(new DeadReckonTask(this, path) {
            @Override
            public void handleEvent(RobotEvent e) {
                if (e instanceof DeadReckonEvent) {
                    DeadReckonEvent drEvent = (DeadReckonEvent) e;

                    if (drEvent.kind == EventKind.PATH_DONE) {
                        RobotLog.i("141 Approached beacon.");
                        navigateToTarget(vuforiaTarget);
                        //detectLine();
                    }
                }
            }
        });
    }

    /*
     * Navigates to the target, one axis at a time, using Vuforia magic.
     */
    private void navigateToTarget(NavigateToTargetTask.Targets target)
    {
        RobotLog.i("141 Navigating to target.");
        drivetrain.setNoncanonicalMotorDirection();
        switch (target) {
            case RED_NEAR:
                RobotLog.i("141 Navigating to RED NEAR target.");
                nttt.setTarget(NavigateToTargetTask.Targets.RED_NEAR);
                break;
            case RED_FAR:
                RobotLog.i("141 Navigating to RED FAR target.");
                nttt.setTarget(NavigateToTargetTask.Targets.RED_FAR);
                break;
            case BLUE_NEAR:
                RobotLog.i("141 Navigating to BLUE NEAR target.");
                nttt.setTarget(NavigateToTargetTask.Targets.BLUE_NEAR);
                break;
            case BLUE_FAR:
                RobotLog.i("141 Navigating to BLUE FAR target.");
                nttt.setTarget(NavigateToTargetTask.Targets.BLUE_FAR);
                break;
        }
        drivetrain.resetEncoders();
        drivetrain.encodersOn();
        this.addTask(nttt);
        nttt.findTarget();
    }

    /*
     * NOTE: Currently, detectLine() should not be in use. However, I'm leaving it just in case
     *       we decide to get even closer to the beacon before using Vuforia target finding.
     */
    private void detectLine()
    {
        RobotLog.i("141 Attempting to detect white line.");

        // De-registering color sensor and range sensor.
        cdim.deregisterForPortReadyCallback(COLOR_PORT);
        cdim.deregisterForPortReadyCallback(RANGE_PORT);

        MecanumGearedDriveDeadReckon lineDetect = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        lineDetect.addSegment(DeadReckon.SegmentType.SIDEWAYS, 40, 0.2 * turnMultiplier);

        // Move sideways until white line is found.
        this.addTask(new DeadReckonTask(this, lineDetect, frontLightCriteria) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                DeadReckonEvent drEvent = (DeadReckonEvent) e;

                if (drEvent.kind == EventKind.SENSOR_SATISFIED) {
                    RobotLog.i("141 Detected white line.");
                    this.stop();
                } else if (drEvent.kind == EventKind.PATH_DONE) {
                    RobotLog.i("141 White line not found.");
                    // Missed white line, try again.
                }
            }
        });
    }

    /*
     * NOTE: Might remove?
     */
    private void alignedAndPushing()
    {
        RobotLog.i("141 Aligned and pushing.");

        // Re-registering color sensor and range sensor.
        cdim.registerForI2cPortReadyCallback(colorSensorCallback, COLOR_PORT);
        cdim.registerForI2cPortReadyCallback(rangeSensorCallback, RANGE_PORT);
        goToNextBeacon();
    }

    public void goPushBeacon() {
        RobotLog.i("141 Moving forward to push beacon.");
        final MecanumGearedDriveDeadReckon pushBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        pushBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 4, -0.1);

        this.addTask(new DeadReckonTask(this, pushBeacon) {
            @Override
            public void handleEvent(RobotEvent e) {
                if (e instanceof DeadReckonEvent) {
                    DeadReckonEvent drEvent = (DeadReckonEvent) e;

                    if (drEvent.kind == EventKind.PATH_DONE) {
                        RobotLog.i("141 Smashed beacon path done; attempting beacon work.");
                        //goToNextBeacon();
                    } else {
                        RobotLog.i("141 Push beacon path done before range criteria satisfied.");
                        // back up and try again.
                    }
                }
            }
        });
    }
    public void goToNextBeacon()
    {
        if (beaconChoice == AutonomousBeacon.BEACON_2 && goToNext) {
            approachBeacon(approachNext);
            RobotLog.i("141 Approaching second beacon.");
            goToNext = false;
        }
    }

    private void filterLaunchSelection()
    {
        if (launchSelection == 0) {
            actionChoice = AutonomousAction.LAUNCH_0;
            ptt.addData("LAUNCH", "Launch 0 Balls");
            launchSelection = 1;
        } else if (launchSelection == 1) {
            actionChoice = AutonomousAction.LAUNCH_1;
            ptt.addData("LAUNCH", "Launch 1 Balls");
            launchSelection = 2;
        } else {
            actionChoice = AutonomousAction.LAUNCH_2;
            ptt.addData("LAUNCH", "Launch 2 Balls");
            launchSelection = 0;
        }
    }

    private void filterBeaconSelection()
    {
        if (beaconSelection == 0) {
            beaconChoice = AutonomousBeacon.BEACON_0;
            ptt.addData("BEACON", "Claim 0 Beacons");
            beaconSelection = 1;
        } else if (beaconSelection == 1) {
            beaconChoice = AutonomousBeacon.BEACON_1;
            ptt.addData("BEACON", "Claim 1 Beacon");
            beaconSelection = 2;
        } else {
            beaconChoice = AutonomousBeacon.BEACON_2;
            ptt.addData("BEACON", "Claim 2 Beacons");
            beaconSelection = 0;
        }
    }
    private void selectAlliance(Alliance color)
    {
        if (color == Alliance.BLUE) {
            // Do blue setup.
            RobotLog.i("141 Doing blue setup.");
            vuforiaTarget = NavigateToTargetTask.Targets.BLUE_NEAR;
            secondTarget = NavigateToTargetTask.Targets.BLUE_FAR;
            turnMultiplier = -1;
            alliance = Alliance.BLUE;
            helper = new BeaconHelper(this, this, BeaconHelper.Alliance.BLUE, buttonPushers, colorSensor, cdim);
            ((ModernRoboticsI2cGyro)gyroSensor).setHeadingMode(ModernRoboticsI2cGyro.HeadingMode.HEADING_CARTESIAN);
        } else {
            // Do red setup.
            RobotLog.i("141 Doing red setup.");
            vuforiaTarget = NavigateToTargetTask.Targets.RED_NEAR;
            secondTarget = NavigateToTargetTask.Targets.RED_FAR;
            turnMultiplier = 1;
            alliance = Alliance.RED;
            helper = new BeaconHelper(this, this, BeaconHelper.Alliance.RED, buttonPushers, colorSensor, cdim);
            ((ModernRoboticsI2cGyro)gyroSensor).setHeadingMode(ModernRoboticsI2cGyro.HeadingMode.HEADING_CARDINAL);
        }
    }

    private void pathSetup(AutonomousPath pathChoice)
    {
        switch(pathChoice) {
            case CORNER_PARK:
                parkPath.addSegment(DeadReckon.SegmentType.STRAIGHT,  58, STRAIGHT_SPEED);
                parkPath.addSegment(DeadReckon.SegmentType.TURN,     120, TURN_SPEED * turnMultiplier);
                parkPath.addSegment(DeadReckon.SegmentType.STRAIGHT,  85, STRAIGHT_SPEED);
                break;
            case CENTER_PARK:
                parkPath.addSegment(DeadReckon.SegmentType.STRAIGHT,  60, STRAIGHT_SPEED);
                break;
        }
    }

    private void pathSetup(AutonomousBeacon beaconChoice)
    {
        switch(beaconChoice) {
            case BEACON_1:
                approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT,  8, STRAIGHT_SPEED);
                approachBeacon.addSegment(DeadReckon.SegmentType.TURN,     90, -TURN_SPEED * turnMultiplier);
                approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 55, STRAIGHT_SPEED * turnMultiplier);
                approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 43, -STRAIGHT_SPEED);
                approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 50, STRAIGHT_SPEED * turnMultiplier);
                break;
            case BEACON_2:
                approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT,  8, STRAIGHT_SPEED);
                approachBeacon.addSegment(DeadReckon.SegmentType.TURN,     90, -TURN_SPEED * turnMultiplier);
                approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 55, STRAIGHT_SPEED * turnMultiplier);
                approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 43, -STRAIGHT_SPEED);
                approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 50, STRAIGHT_SPEED * turnMultiplier);
                approachNext.addSegment(DeadReckon.SegmentType.STRAIGHT,   13, 0.2);
                approachNext.addSegment(DeadReckon.SegmentType.SIDEWAYS,   67, 0.8 * turnMultiplier);
                goToNext = true;
                break;
        }
    }
}
