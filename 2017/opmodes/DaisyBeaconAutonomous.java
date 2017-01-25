package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.MecanumGearedDriveDeadReckon;
import team25core.GamepadTask;
import team25core.MRLightSensor;
import team25core.OpticalDistanceSensorCriteria;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/5/2016.
 */
@Autonomous(name = "Daisy: Beacon Autonomous", group = "Team25")
@Disabled
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
    private Servo swinger;
    private ColorSensor colorSensor;
    private OpticalDistanceSensor frontOds;
    private MRLightSensor frontLight;
    private DeviceInterfaceModule cdim;
    private DeadReckonTask deadReckonParkTask;
    private BeaconHelper helper;
    private GeneralBeaconArms buttonPushers;
    private PersistentTelemetryTask ptt;
    private MecanumGearedDriveDeadReckon parkPath;
    private MecanumGearedDriveDeadReckon approachBeacon;
    private MecanumGearedDriveDeadReckon approachNext;
    private MecanumGearedDriveDeadReckon lineDetect;
    private MecanumGearedDriveDeadReckon pushBeacon;
    private final int TICKS_PER_INCH = DaisyConfiguration.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = DaisyConfiguration.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = DaisyConfiguration.STRAIGHT_SPEED;
    private final double TURN_SPEED = DaisyConfiguration.TURN_SPEED;
    private final int LAUNCH_POSITION = DaisyConfiguration.LAUNCH_POSITION;
    private final double LEFT_DEPLOY_POS = DaisyConfiguration.LEFT_DEPLOY_POS;
    private final double LEFT_STOW_POS = DaisyConfiguration.LEFT_STOW_POS;
    private final double RIGHT_DEPLOY_POS = DaisyConfiguration.RIGHT_DEPLOY_POS;
    private final double RIGHT_STOW_POS = DaisyConfiguration.RIGHT_STOW_POS;
    private int turnMultiplier = 1;
    private boolean launched;
    private RunToEncoderValueTask runToPositionTask;
    private SingleShotTimerTask stt;
    OpticalDistanceSensorCriteria frontLightCriteria;

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
        LAUNCH_1,
        LAUNCH_2,
    }

    public enum AutonomousBeacon {
        BEACON_1,
        BEACON_2,
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            if (event.kind == GamepadTask.EventKind.BUTTON_X_DOWN) {
                selectAlliance(Alliance.BLUE);
                ptt.addData("ALLIANCE", "Blue");
            } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN) {
                selectAlliance(Alliance.RED);
                ptt.addData("ALLIANCE", "Red");
            } else if (event.kind == GamepadTask.EventKind.LEFT_TRIGGER_DOWN) {
                pathChoice = AutonomousPath.CORNER_PARK;
                ptt.addData("AUTONOMOUS", "Corner Park");
            } else if (event.kind == GamepadTask.EventKind.RIGHT_TRIGGER_DOWN) {
                pathChoice = AutonomousPath.CENTER_PARK;
                ptt.addData("AUTONOMOUS", "Center Park");
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

        if (e instanceof SingleShotTimerTask.SingleShotTimerEvent) {
            conveyor.setPower(0);
            addTask(runToPositionTask);
        } else if (e instanceof RunToEncoderValueTask.RunToEncoderValueEvent) {
            RunToEncoderValueTask.RunToEncoderValueEvent event = (RunToEncoderValueTask.RunToEncoderValueEvent) e;

            if (event.kind == RunToEncoderValueTask.EventKind.DONE) {
                handleEncoderEvent();
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
        } else {
            // Begin to approach the beacon.
            approachBeacon(approachBeacon, true);

        }
    }

    private void approachBeacon(MecanumGearedDriveDeadReckon path, boolean goToNext)
    {
        if (goToNext) {
            this.addTask(new DeadReckonTask(this, path) {
                @Override
                public void handleEvent(RobotEvent e) {
                    if (e instanceof DeadReckonTask.DeadReckonEvent) {
                        DeadReckonTask.DeadReckonEvent drEvent = (DeadReckonTask.DeadReckonEvent) e;

                        if (drEvent.kind == EventKind.PATH_DONE) {
                            detectLine();
                        }
                    }
                }
            });
        } else {
            this.addTask(new DeadReckonTask(this, path) {
                @Override
                public void handleEvent(RobotEvent e) {
                    if (e instanceof DeadReckonTask.DeadReckonEvent) {
                        DeadReckonTask.DeadReckonEvent drEvent = (DeadReckonTask.DeadReckonEvent) e;

                        if (drEvent.kind == EventKind.PATH_DONE) {
                           // park...
                        }
                    }
                }
            });
        }
    }

    private void detectLine()
    {
        this.addTask(new DeadReckonTask(this, lineDetect, frontLightCriteria) {
            @Override
            public void handleEvent(RobotEvent e) {
                if (e instanceof DeadReckonTask.DeadReckonEvent) {
                    DeadReckonTask.DeadReckonEvent drEvent = (DeadReckonTask.DeadReckonEvent) e;

                    if (drEvent.kind == EventKind.SENSOR_SATISFIED) {
                        goPushBeacon();
                        goToNextBeacon();
                    }
                }
            }
        });
    }

    private void goToNextBeacon()
    {
        if (beaconChoice == AutonomousBeacon.BEACON_2) {
            this.addTask(new SingleShotTimerTask(this, 4000) {
                @Override
                public void handleEvent(RobotEvent e)
                {
                    approachBeacon(approachNext, false);
                }
            });
        }
    }

    private void goPushBeacon()
    {
        this.addTask(new DeadReckonTask(this, pushBeacon) {
            @Override
            public void handleEvent(RobotEvent e) {
                if (e instanceof DeadReckonTask.DeadReckonEvent) {
                    DeadReckonTask.DeadReckonEvent drEvent = (DeadReckonTask.DeadReckonEvent) e;

                    if (drEvent.kind == EventKind.PATH_DONE) {
                        helper.doBeaconWork();
                        ptt.addData("Beacon", "Attempting!");
                    }
                }
            }
        });
    }

    private void selectAlliance(Alliance color)
    {
        if (color == Alliance.BLUE) {
            // Do blue setup.
            turnMultiplier = -1;
            alliance = Alliance.BLUE;
            helper = new BeaconHelper(this, BeaconHelper.Alliance.BLUE, buttonPushers, colorSensor, cdim);
        } else {
            // Do red setup.
            turnMultiplier = 1;
            alliance = Alliance.RED;
            helper = new BeaconHelper(this, BeaconHelper.Alliance.RED, buttonPushers, colorSensor, cdim);
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
            approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 8, STRAIGHT_SPEED);
            approachBeacon.addSegment(DeadReckon.SegmentType.TURN, 90, -TURN_SPEED);
            approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 73, STRAIGHT_SPEED);
            approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 46, -STRAIGHT_SPEED);
            lineDetect.addSegment(DeadReckon.SegmentType.SIDEWAYS, 20, 0.1);
            pushBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 7 , -0.2);
        } else if (beaconChoice == AutonomousBeacon.BEACON_2) {
            approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 8, STRAIGHT_SPEED);
            approachBeacon.addSegment(DeadReckon.SegmentType.TURN, 90, -TURN_SPEED);
            approachBeacon.addSegment(DeadReckon.SegmentType.SIDEWAYS, 73, STRAIGHT_SPEED);
            approachBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 46, -STRAIGHT_SPEED);
            lineDetect.addSegment(DeadReckon.SegmentType.SIDEWAYS, 20, 0.1);
            pushBeacon.addSegment(DeadReckon.SegmentType.STRAIGHT, 7, -0.2);
            approachNext.addSegment(DeadReckon.SegmentType.STRAIGHT, 7, -0.2);
            approachNext.addSegment(DeadReckon.SegmentType.SIDEWAYS, 10, 0.8);
        }
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
        cdim = hardwareMap.deviceInterfaceModule.get("cdim");

        leftPusher.setPosition(LEFT_STOW_POS);
        rightPusher.setPosition(RIGHT_STOW_POS);
        swinger.setPosition(0.7);

        // Optical Distance Sensor (front) setup.
        frontOds = hardwareMap.opticalDistanceSensor.get("frontLight");
        frontLight = new MRLightSensor(frontOds);
        frontLightCriteria = new OpticalDistanceSensorCriteria(frontLight, DaisyConfiguration.ODS_MIN, DaisyConfiguration.ODS_MAX);

        // Path setup.
        lineDetect = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        pushBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        approachBeacon = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        parkPath = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        approachNext = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);

        // Launch setup.
        runToPositionTask = new RunToEncoderValueTask(this, launcher, LAUNCH_POSITION, 1.0);
        launched = false;

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

        // Single shot timer task for reloading launcher.
        stt = new SingleShotTimerTask(this, 2000);

        // Button pushers setup.
        buttonPushers = new GeneralBeaconArms(leftPusher, rightPusher, LEFT_DEPLOY_POS,
                RIGHT_DEPLOY_POS, LEFT_STOW_POS, RIGHT_STOW_POS, false);

        // Telemetry setup.
        ptt = new PersistentTelemetryTask(this);
        this.addTask(ptt);
        ptt.addData("Press (X) to select", "Blue alliance!");
        ptt.addData("Press (B) to select", "Red alliance!");
        ptt.addData("Press (A) to select", "Claim 1 Beacon!");
        ptt.addData("Press (Y) to select", "Claim 2 Beacons!");
        ptt.addData("Press (LEFT TRIGGER) to select", "Corner Park!");
        ptt.addData("Press (RIGHT TRIGGER) to select", "Center Park!");
        ptt.addData("Press (LEFT BUMPER) to select", "Launch 1 Ball!");
        ptt.addData("Press (RIGHT BUMPER) to select", "Launch 2 Balls!");

        // Alliance selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));
    }

    @Override
    public void start()
    {
        pathSetup(pathChoice);
        pathSetup(beaconChoice);
        deadReckonParkTask = new DeadReckonTask(this, parkPath);
        addTask(runToPositionTask);
    }
}
