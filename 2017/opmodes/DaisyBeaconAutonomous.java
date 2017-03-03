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
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor launcher;
    private DcMotor conveyor;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo capServo;
    private AMSColorSensorImproved colorSensor;
    private OpticalDistanceSensor ods;
    private MRLightSensor light;
    private DeviceInterfaceModule cdim;
    private BeaconHelper helper;
    private BeaconArms buttonPushers;
    private PersistentTelemetryTask ptt;

    private MecanumGearedDriveDeadReckon approachBeacon;
    private MecanumGearedDriveDeadReckon approachNext;
    private MecanumGearedDriveDeadReckon backOff;

    private FourWheelDirectDrivetrain drivetrain;
    private final int TICKS_PER_INCH = Daisy.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = Daisy.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = Daisy.STRAIGHT_SPEED;
    private final double TURN_SPEED = Daisy.TURN_SPEED;
    private final int LAUNCH_POSITION = Daisy.LAUNCH_POSITION;
    private int turnMultiplier = 1;
    private int launchSelection = 0;
    private int beaconSelection = 0;
    private boolean launched;
    private boolean goToNext;
    private RunToEncoderValueTask launchParticleTask;
    private SingleShotTimerTask stt;
    private OpticalDistanceSensorCriteria lightCriteria;

    private RobotNavigation nav;
    private NavigateToTargetTask nttt;
    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = Daisy.CAMERA_CHOICE;
    private NavigateToTargetTask.Targets vuforiaTarget;
    private NavigateToTargetTask.Targets secondTarget;

    private I2cController.I2cPortReadyCallback colorSensorCallback;
    private final int COLOR_PORT = Daisy.COLOR_PORT;

    private AutonomousPath pathChoice = AutonomousPath.STAY;
    private AutonomousAction actionChoice = AutonomousAction.LAUNCH_0;
    private AutonomousBeacon beaconChoice = AutonomousBeacon.BEACON_0;

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
        capServo    = hardwareMap.servo.get("capServo");
        cdim        = hardwareMap.deviceInterfaceModule.get("cdim");
        colorSensor = AMSColorSensorImproved.create(AMSColorSensor.Parameters.createForAdaFruit(), new I2cDeviceImpl(cdim, COLOR_PORT));
        drivetrain  = new FourWheelDirectDrivetrain(TICKS_PER_INCH, frontRight, rearRight, frontLeft, rearLeft);

        // Callback.
        this.colorSensorCallback = cdim.getI2cPortReadyCallback(COLOR_PORT);

        // Initialize pushers.
        leftPusher.setPosition(Daisy.LEFT_STOW_POS);
        rightPusher.setPosition(Daisy.RIGHT_STOW_POS);
        capServo.setPosition(0.8);

        // Optical Distance Sensor (front) setup.
        ods = hardwareMap.opticalDistanceSensor.get("frontLight");
        light = new MRLightSensor(ods);
        lightCriteria = new OpticalDistanceSensorCriteria(light, Daisy.ODS_MIN, Daisy.ODS_MAX);

        // Path setup.
        approachBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        approachNext = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        backOff = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);

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
        ptt.addData("LAUNCH", "Unselected (LB)");
        ptt.addData("BEACON", "Unselected (RB)");
        ptt.addData("ALLIANCE", "Unselected (X/B)");
        ptt.addData("For additional help, press", "(Y)");

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

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

        nttt = new NavigateToTargetTask(this, drivetrain, NavigateToTargetTask.Targets.RED_NEAR, 1000000, gamepad1, NavigateToTargetTask.Alliance.RED);
        nttt.init(targets, parameters, phoneLocationOnRobot);
    }

    @Override
    public void start()
    {
        pathSetup(beaconChoice);

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
     * UPDATE: Haha, now we're using it again!
     */
    private void detectLine()
    {
        RobotLog.i("141 Attempting to detect white line.");

        // De-registering color sensor and range sensor.
        cdim.deregisterForPortReadyCallback(COLOR_PORT);

        MecanumGearedDriveDeadReckon lineDetect = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        lineDetect.addSegment(DeadReckon.SegmentType.STRAIGHT, 20, 0.2);

        // Move sideways until white line is found.
        this.addTask(new DeadReckonTask(this, lineDetect, lightCriteria) {
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
                    // Maybe, rotate? back up?
                }
            }
        });
    }

    public void goPushBeacon() {
        RobotLog.i("141 Moving forward to push beacon.");
        MecanumGearedDriveDeadReckon pushBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        pushBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 7, -0.1);

        this.addTask(new DeadReckonTask(this, pushBeacon) {
            @Override
            public void handleEvent(RobotEvent e) {
                if (e instanceof DeadReckonEvent) {
                    DeadReckonEvent drEvent = (DeadReckonEvent) e;

                    if (drEvent.kind == EventKind.PATH_DONE) {
                        RobotLog.i("141 Smashed beacon path done; attempting beacon work.");
                        goToNextBeacon();
                    }
                }
            }
        });
    }

    public void backOffBuster()
    {
        this.addTask(new DeadReckonTask(this, backOff));
    }

    public void goToNextBeacon()
    {
        this.addTask(new SingleShotTimerTask(this, 1000) {
            @Override
            public void handleEvent(RobotEvent e) {
                if (beaconChoice == AutonomousBeacon.BEACON_2 && goToNext) {
                    approachBeacon(approachNext);
                    RobotLog.i("141 Approaching second beacon.");
                    goToNext = false;
                } else {
                    backOffBuster();
                }
            }
        });
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
            drivetrain.setAlliance(Alliance.BLUE);
            nttt.setAlliance(NavigateToTargetTask.Alliance.BLUE);
            nav.setMaxLateralOffset(2);
            helper = new BeaconHelper(this, this, BeaconHelper.Alliance.BLUE, buttonPushers, colorSensor, cdim);
        } else {
            // Do red setup.
            RobotLog.i("141 Doing red setup.");
            vuforiaTarget = NavigateToTargetTask.Targets.RED_NEAR;
            secondTarget = NavigateToTargetTask.Targets.RED_FAR;
            turnMultiplier = 1;
            drivetrain.setAlliance(Alliance.RED);
            nttt.setAlliance(NavigateToTargetTask.Alliance.RED);
            nav.setMaxLateralOffset(4);
            helper = new BeaconHelper(this, this, BeaconHelper.Alliance.RED, buttonPushers, colorSensor, cdim);
        }
    }

    private void pathSetup(AutonomousBeacon beaconChoice)
    {
        switch(beaconChoice) {
            case BEACON_1:
                approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT,  8, STRAIGHT_SPEED);
                approachBeacon.addSegment(DeadReckon.SegmentType.TURN,     90, -TURN_SPEED * turnMultiplier);
                approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 55, STRAIGHT_SPEED * turnMultiplier);
                approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 40, STRAIGHT_SPEED * turnMultiplier);
                break;
            case BEACON_2:
                approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT,  8, STRAIGHT_SPEED);
                approachBeacon.addSegment(DeadReckon.SegmentType.TURN,     90, -TURN_SPEED * turnMultiplier);
                approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 55, STRAIGHT_SPEED * turnMultiplier);
                approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 43, -STRAIGHT_SPEED);
                approachNext.addSegment(DeadReckon.SegmentType.STRAIGHT,   20, 0.2);
                approachNext.addSegment(DeadReckon.SegmentType.SIDEWAYS,   100, 0.8 * turnMultiplier);
                goToNext = true;
                break;
        }

        backOff.addSegment(DeadReckon.SegmentType.STRAIGHT, 6, STRAIGHT_SPEED);
    }
}
