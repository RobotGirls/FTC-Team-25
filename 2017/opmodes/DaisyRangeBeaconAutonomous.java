package opmodes;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.I2cController;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.GyroTask;
import team25core.MRLightSensor;
import team25core.MecanumGearedDriveDeadReckon;
import team25core.OpticalDistanceSensorCriteria;
import team25core.PersistentTelemetryTask;
import team25core.RangeSensorCriteria;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/5/2016.
 */
@Autonomous(name = "Daisy: Range Beacon Autonomous", group = "Team25")

public class DaisyRangeBeaconAutonomous extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor launcher;
    private DcMotor conveyor;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo swinger;
    private ColorSensor colorSensor;
    private OpticalDistanceSensor frontOds;
    private MRLightSensor frontLight;
    private DistanceSensor rangeSensor;
    private GyroSensor gyroSensor;
    private DeviceInterfaceModule cdim;
    private DeadReckonTask deadReckonParkTask;
    private BeaconHelper helper;
    private BeaconArms buttonPushers;
    private PersistentTelemetryTask ptt;
    private MecanumGearedDriveDeadReckon parkPath;
    private MecanumGearedDriveDeadReckon approachBeacon;
    private MecanumGearedDriveDeadReckon approachNext;
    private MecanumGearedDriveDeadReckon lineDetect;
    private MecanumGearedDriveDeadReckon secondLineDetect;
    //private MecanumGearedDriveDeadReckon pushBeacon;
    //private MecanumGearedDriveDeadReckon secondPushBeacon;
    private MecanumGearedDriveDeadReckon adjustTurn;
    private final int TICKS_PER_INCH = Daisy.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = Daisy.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = Daisy.STRAIGHT_SPEED;
    private final double TURN_SPEED = Daisy.TURN_SPEED;
    private final int LAUNCH_POSITION = Daisy.LAUNCH_POSITION;
    private final double LEFT_DEPLOY_POS = Daisy.LEFT_DEPLOY_POS;
    private final double LEFT_STOW_POS = Daisy.LEFT_STOW_POS;
    private final double RIGHT_DEPLOY_POS = Daisy.RIGHT_DEPLOY_POS;
    private final double RIGHT_STOW_POS = Daisy.RIGHT_STOW_POS;
    private int turnMultiplier = 1;
    private int gyroMultiplier = 1;
    private int target;
    private boolean launched;
    private boolean enableGyroTest;
    private boolean goToNext;
    private RunToEncoderValueTask runToPositionTask;
    private SingleShotTimerTask stt;
    OpticalDistanceSensorCriteria frontLightCriteria;
    RangeSensorCriteria rangeSensorCriteria;

    I2cController.I2cPortReadyCallback colorSensorCallback;
    I2cController.I2cPortReadyCallback rangeSensorCallback;
    private int colorPort = 0;
    private int rangePort = 2;

    private Alliance alliance = Alliance.RED;
    private AutonomousPath pathChoice = AutonomousPath.STAY;
    private AutonomousAction actionChoice = AutonomousAction.LAUNCH_2;
    private AutonomousBeacon beaconChoice = AutonomousBeacon.BEACON_1;

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
        BEACON_1,
        BEACON_2,
    }

    @Override
    public void init()
    {
        // Hardware mapping.
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft   = hardwareMap.dcMotor.get("rearLeft");
        rearRight  = hardwareMap.dcMotor.get("rearRight");
        launcher = hardwareMap.dcMotor.get("launcher");
        conveyor = hardwareMap.dcMotor.get("conveyor");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightPusher = hardwareMap.servo.get("rightPusher");
        swinger = hardwareMap.servo.get("odsSwinger");
        colorSensor = hardwareMap.colorSensor.get("color");
        rangeSensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "range");
        gyroSensor = hardwareMap.gyroSensor.get("gyroSensor");
        cdim = hardwareMap.deviceInterfaceModule.get("cdim");

        // Callback.
        this.colorSensorCallback = cdim.getI2cPortReadyCallback(colorPort);
        this.rangeSensorCallback = cdim.getI2cPortReadyCallback(rangePort);

        // Initialize pushers.
        leftPusher.setPosition(LEFT_STOW_POS);
        rightPusher.setPosition(RIGHT_STOW_POS);
        swinger.setPosition(0.7);

        // Optical Distance Sensor (front) setup.
        frontOds = hardwareMap.opticalDistanceSensor.get("frontLight");
        frontLight = new MRLightSensor(frontOds);
        frontLightCriteria = new OpticalDistanceSensorCriteria(frontLight, Daisy.ODS_MIN, Daisy.ODS_MAX);

        // Range Sensor setup.
        rangeSensorCriteria = new RangeSensorCriteria(rangeSensor, 8);

        // Path setup.
        lineDetect = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        secondLineDetect = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        //pushBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        //secondPushBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        approachBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        parkPath = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        approachNext = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        adjustTurn = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);

        // Launch setup.
        runToPositionTask = new RunToEncoderValueTask(this, launcher, LAUNCH_POSITION, 1.0);
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
        buttonPushers = new BeaconArms(this, leftPusher, rightPusher, LEFT_DEPLOY_POS,
                RIGHT_DEPLOY_POS, LEFT_STOW_POS, RIGHT_STOW_POS, true);

        // Telemetry setup.
        ptt = new PersistentTelemetryTask(this);
        this.addTask(ptt);
        ptt.addData("Press (X) to select", "Blue alliance!");
        ptt.addData("Press (B) to select", "Red alliance!");
        ptt.addData("Press (A) to select", "Claim 1 Beacon!");
        ptt.addData("Press (Y) to select", "Claim 2 Beacons!");
        ptt.addData("Press (RIGHT TRIGGER) to select", "Center Park!");
        ptt.addData("Press (LEFT TRIGGER) to select", "Launch 0 Balls!");
        ptt.addData("Press (LEFT BUMPER) to select", "Launch 1 Ball!");
        ptt.addData("Press (RIGHT BUMPER) to select", "Launch 2 Balls!");

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        // Gyro calibration.
        gyroSensor.calibrate();
        // gyroSensor.resetZAxisIntegrator();

    }

    @Override
    public void start()
    {
        pathSetup(pathChoice);
        pathSetup(beaconChoice);
        deadReckonParkTask = new DeadReckonTask(this, parkPath);

        // Launch first particle.
       if (actionChoice != AutonomousAction.LAUNCH_0) {
           addTask(runToPositionTask);
       } else {
           approachBeacon(approachBeacon, lineDetect);
           RobotLog.i("141 Approaching first beacon.");
       }

    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            if (event.kind == GamepadTask.EventKind.BUTTON_X_DOWN) {
                selectAlliance(Alliance.BLUE);
                ptt.addData("ALLIANCE", "Blue");
            } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN) {
                selectAlliance(Alliance.RED);
                ptt.addData("ALLIANCE", "Red");
            } else if (event.kind == GamepadTask.EventKind.LEFT_TRIGGER_DOWN) {
                actionChoice = AutonomousAction.LAUNCH_0;
                ptt.addData("LAUNCH", "Launch 0 Balls");
            } else if (event.kind == GamepadTask.EventKind.RIGHT_TRIGGER_DOWN) {
                pathChoice = AutonomousPath.CENTER_PARK;
                ptt.addData("PARK", "Center Park");
            } else if (event.kind == GamepadTask.EventKind.LEFT_BUMPER_DOWN) {
                actionChoice = AutonomousAction.LAUNCH_1;
                ptt.addData("LAUNCH", "Launch 1 Ball");
            } else if (event.kind == GamepadTask.EventKind.RIGHT_BUMPER_DOWN) {
                actionChoice = AutonomousAction.LAUNCH_2;
                ptt.addData("LAUNCH", "Launch 2 Balls");
            } else if (event.kind == GamepadTask.EventKind.BUTTON_A_DOWN) {
                beaconChoice = AutonomousBeacon.BEACON_1;
                ptt.addData("BEACON", "Claim 1 Beacon");
            } else if (event.kind == GamepadTask.EventKind.BUTTON_Y_DOWN) {
                beaconChoice = AutonomousBeacon.BEACON_2;
                ptt.addData("BEACON", "Claim 2 Beacons");
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
            addTask(runToPositionTask);
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
        } else {
            // Begin to approach the beacon.
            approachBeacon(approachBeacon, lineDetect);
            RobotLog.i("141 Approaching first beacon.");

        }
    }

    private void approachBeacon(MecanumGearedDriveDeadReckon path, final MecanumGearedDriveDeadReckon detectLinePath)
    {
        this.addTask(new DeadReckonTask(this, path) {
            @Override
            public void handleEvent(RobotEvent e) {
                if (e instanceof DeadReckonEvent) {
                    DeadReckonEvent drEvent = (DeadReckonEvent) e;

                    if (drEvent.kind == EventKind.PATH_DONE) {
                        RobotLog.i("141 Approached beacon.");
                        detectLine(detectLinePath);
                    }
                }
            }
        });
    }

    private void detectLine(MecanumGearedDriveDeadReckon path)
    {
        RobotLog.i("141 Attempting to detect white line.");
        cdim.deregisterForPortReadyCallback(colorPort);
        cdim.deregisterForPortReadyCallback(rangePort);

        this.addTask(new DeadReckonTask(this, path, frontLightCriteria) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                DeadReckonEvent drEvent = (DeadReckonEvent) e;

                if (drEvent.kind == EventKind.SENSOR_SATISFIED) {
                    this.stop();
                    ptt.addData("Gyro Heading", gyroSensor.getHeading());
                    RobotLog.i("141 Gyro Heading %d", gyroSensor.getHeading());
                    this.robot.addTask(new SingleShotTimerTask(this.robot, 700) {
                        @Override
                        public void handleEvent(RobotEvent e)
                        {
                            adjustWithGyro();
                        }
                    });
                    RobotLog.i("141 Detected white line.");
                } else if (drEvent.kind == EventKind.PATH_DONE) {
                    RobotLog.i("141 Line detect path done.");

                    // Missed white line, try again.
                }
            }
        });
    }

    private void adjustWithGyro()
    {
        double error = target - gyroSensor.getHeading();
        RobotLog.i("141 Gyro heading %d", gyroSensor.getHeading());
        RobotLog.i("141 Beacon angle error of %f degrees", error);

        if (error >= 2) {
            RobotLog.i("141 Adjusting angle by turning.");
            adjustTurn.addSegment(DeadReckon.SegmentType.TURN, error, -0.1 * turnMultiplier);

            this.addTask(new DeadReckonTask(this, adjustTurn) {
                @Override
                public void handleEvent(RobotEvent e) {
                    DeadReckonEvent event = (DeadReckonEvent) e;
                    if (event.kind == EventKind.PATH_DONE) {
                        RobotLog.i("141 Path done, checking alignment");
                        adjustWithGyro();
                    }
                }
            });
        } else if (error <= -2) {
            RobotLog.i("141 Adjusting angle by turning.");
            adjustTurn.addSegment(DeadReckon.SegmentType.TURN, error, 0.1 * turnMultiplier);

            this.addTask(new DeadReckonTask(this, adjustTurn) {
                @Override
                public void handleEvent(RobotEvent e) {
                    DeadReckonEvent event = (DeadReckonEvent) e;
                    if (event.kind == EventKind.PATH_DONE) {
                        adjustWithGyro();
                    }
                }
            });
        } else {
            RobotLog.i("141 Aligned and pushing.");
            cdim.registerForI2cPortReadyCallback(colorSensorCallback, colorPort);
            cdim.registerForI2cPortReadyCallback(rangeSensorCallback, rangePort);

            goPushBeacon();
            goToNextBeacon();
        }
    }

    private void goPushBeacon()
    {
        RobotLog.i("141 Inside goPushBeacon()");
        MecanumGearedDriveDeadReckon pushBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        pushBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 30, -0.2);

        this.addTask(new DeadReckonTask(this, pushBeacon, rangeSensorCriteria) {
            @Override
            public void handleEvent(RobotEvent e) {
                if (e instanceof DeadReckonEvent) {
                    DeadReckonEvent drEvent = (DeadReckonEvent) e;

                    if (drEvent.kind == EventKind.SENSOR_SATISFIED) {
                        helper.doBeaconWork();
                        ptt.addData("Beacon", "Attempting!");
                        RobotLog.i("141 Doing beacon work.");
                    } else {
                        RobotLog.i("141 FAIL.");
                        // back up and try again.
                    }
                }
            }
        });
    }

    private void goToNextBeacon()
    {
        if (beaconChoice == AutonomousBeacon.BEACON_2 && goToNext) {
            RobotLog.i("141 Ready to approach the next beacon in 7 seconds.");
            this.addTask(new SingleShotTimerTask(this, 7000) {
                @Override
                public void handleEvent(RobotEvent e)
                {
                    approachBeacon(approachNext, secondLineDetect);
                    RobotLog.i("141 Approaching second beacon.");
                }
            });
        }

        goToNext = false;
    }

    private void selectAlliance(Alliance color)
    {
        if (color == Alliance.BLUE) {
            // Do blue setup.
            turnMultiplier = -1;
            alliance = Alliance.BLUE;
            helper = new BeaconHelper(this, BeaconHelper.Alliance.BLUE, buttonPushers, colorSensor, cdim);
            ((ModernRoboticsI2cGyro)gyroSensor).setHeadingMode(ModernRoboticsI2cGyro.HeadingMode.HEADING_CARTESIAN);
            target = 90;
        } else {
            // Do red setup.
            turnMultiplier = 1;
            alliance = Alliance.RED;
            helper = new BeaconHelper(this, BeaconHelper.Alliance.RED, buttonPushers, colorSensor, cdim);
            ((ModernRoboticsI2cGyro)gyroSensor).setHeadingMode(ModernRoboticsI2cGyro.HeadingMode.HEADING_CARDINAL);
            target = 270;
        }
    }

    private void pathSetup(AutonomousPath pathChoice)
    {
        if (pathChoice == AutonomousPath.CORNER_PARK) {
            parkPath.addSegment(DeadReckon.SegmentType.STRAIGHT,  58, STRAIGHT_SPEED);
            parkPath.addSegment(DeadReckon.SegmentType.TURN,     120, TURN_SPEED * turnMultiplier);
            parkPath.addSegment(DeadReckon.SegmentType.STRAIGHT,  85, STRAIGHT_SPEED);
        } else if (pathChoice == AutonomousPath.CENTER_PARK) {
            parkPath.addSegment(DeadReckon.SegmentType.STRAIGHT,  60, STRAIGHT_SPEED);
        }
    }

    private void pathSetup(AutonomousBeacon beaconChoice)
    {
        if (beaconChoice == AutonomousBeacon.BEACON_1) {
            approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT,  8, STRAIGHT_SPEED);
            approachBeacon.addSegment(DeadReckon.SegmentType.TURN,     90, -TURN_SPEED * turnMultiplier);
            approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 55, STRAIGHT_SPEED * turnMultiplier);
            approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 50, -STRAIGHT_SPEED);
            lineDetect.addSegment(DeadReckon.SegmentType.SIDEWAYS, 40, 0.2 * turnMultiplier);
        } else if (beaconChoice == AutonomousBeacon.BEACON_2) {
            approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 8, STRAIGHT_SPEED);
            approachBeacon.addSegment(DeadReckon.SegmentType.TURN, 90, -TURN_SPEED * turnMultiplier);
            approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 55, STRAIGHT_SPEED * turnMultiplier);
            approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 50, -STRAIGHT_SPEED);
            lineDetect.addSegment(DeadReckon.SegmentType.SIDEWAYS, 40, 0.2 * turnMultiplier);
            secondLineDetect.addSegment(DeadReckon.SegmentType.SIDEWAYS, 40, 0.2 * turnMultiplier);
            approachNext.addSegment(DeadReckon.SegmentType.STRAIGHT, 13, 0.2);
            //approachNext.addSegment(DeadReckon.SegmentType.TURN, 5, -0.2 * turnMultiplier);
            approachNext.addSegment(DeadReckon.SegmentType.SIDEWAYS, 67, 0.8 * turnMultiplier);

            goToNext = true;
        }
    }
}
