package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.MineralDetectionTask;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;


/*
 * FTC Team 25: Created by Elizabeth Wu, November 24, 2018
 */

@Autonomous(name = "Lilac Autonomous", group = "Team 25")
public class LilacAutonomous extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;
    private Servo   marker;
    private Servo   latchServo;

    private final int TICKS_PER_INCH = Lilac.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = Lilac.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = 0.5; // Autonomous - slower; it's diff from LilacConstants
    private final double TURN_SPEED = Lilac.TURN_SPEED;

   // private FourWheelDirectDrivetrain drivetrain;
    // GEAR: Switching to Mechanum drivetrain for encoder tick multiplier
    private MechanumGearedDrivetrain drivetrain;

    private OneWheelDirectDrivetrain single;

    private LilacAutonomous.RobotPosition robotPosition;
    private DeadReckonPath scoreMarker;
    private DeadReckonPath runLatchPath;
    private DeadReckonPath detachPath;
    private DeadReckonPath unlatchScan;
    private DeadReckonPath knockOff;

    // Stuff for Mineral Detection
    private MineralDetectionTask mdTask;
    private double  confidence1;
    private double  left1;
    private double  leftMin;
    private boolean inCenter;
    private int     step;
    private double  imageMidpoint;
    private double  goldMidpoint;
    private double  margin = 50;
    private double  initPos;
    private double  finalPos;
    private double  deltaPos;
    private static double FIRST;
    private static double SECOND;
    private static double THIRD;



    public static double LATCH_OPEN     = 255 / 256.0;
    public static double LATCH_CLOSED   = 35  / 256.0;
    public static double MARKER_OPEN    = 250 / 256.0;
    public static double MARKER_CLOSED  = 129 / 256.0;


    private Telemetry.Item positionItem;
    private Telemetry.Item actualPosItem;
    private Telemetry.Item runPos;

    private Telemetry.Item leftMidpointTlm;
    private Telemetry.Item imageMidpointTlm;

    public enum RobotPosition {
        MARKER,
        CRATER,
    }

    @Override
    public void init() {

        // Hardware mapping.
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        latchArm    = hardwareMap.dcMotor.get("latchArm");
        latchServo  = hardwareMap.servo.get("latchServo");
        marker      = hardwareMap.servo.get("marker");

        //drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        //GEAR
        drivetrain = new MechanumGearedDrivetrain(TICKS_PER_INCH, frontRight, rearRight, frontLeft, rearLeft);
        single     = new OneWheelDirectDrivetrain(latchArm);

        drivetrain.resetEncoders();
        drivetrain.encodersOn();

        single.resetEncoders();
        single.encodersOn();

        // Encoder + brake for latchArm and latchServo
        latchArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        latchArm.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        latchArm.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        latchServo.setPosition(LATCH_CLOSED);

        // Close marker servo
        marker.setPosition(MARKER_CLOSED);

        // Telemetry for selection.
        positionItem = telemetry.addData("POSITION", "Unselected (Y/A)");


        // Path setup.
        scoreMarker     = new DeadReckonPath();
        runLatchPath    = new DeadReckonPath();
        detachPath      = new DeadReckonPath();
        unlatchScan     = new DeadReckonPath();
        knockOff        = new DeadReckonPath();

        // Init selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));


        // Segment setup.
        setOtherPaths();

        mdTask = new MineralDetectionTask(this) {
            @Override
            public void handleEvent(RobotEvent e) {
                MineralDetectionEvent event = (MineralDetectionEvent) e;
                confidence1 = event.minerals.get(0).getConfidence();
                left1 = event.minerals.get(0).getLeft();
                RobotLog.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>left in init"+left1 +"blah");
                RobotLog.i("Saw: " + event.kind + " Confidence: " + event.minerals.get(0).getConfidence());
                RobotLog.i("Saw: " + event.kind + " LEFT: " + event.minerals.get(0).getLeft());

                imageMidpoint = event.minerals.get(0).getImageWidth() / 2.0;
                goldMidpoint  = (event.minerals.get(0).getWidth() / 2.0) + left1;

                leftMidpointTlm = telemetry.addData("LEFT_MDPT: ", goldMidpoint);
                imageMidpointTlm = telemetry.addData(" IMG_MDPT: ", imageMidpoint);

                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    if (Math.abs(imageMidpoint-goldMidpoint) < margin) {
                        inCenter = true;
                        RobotLog.i("506 Found gold");
                        mdTask.stop();
                        drivetrain.stop();
                        knockOff();
                    }
                }
            }
        };
        mdTask.init(telemetry,hardwareMap);
        mdTask.setDetectionKind(MineralDetectionTask.DetectionKind.LARGEST_GOLD);
    }


    public void other_init() {

        // Telemetry for selection.
        positionItem = telemetry.addData("POSITION", "Unselected (Y/A)");

        mdTask = new MineralDetectionTask(this) {
            @Override
            public void handleEvent(RobotEvent e) {
                MineralDetectionEvent event = (MineralDetectionEvent) e;
                confidence1 = event.minerals.get(0).getConfidence();
                left1 = event.minerals.get(0).getLeft();
                RobotLog.i(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>left in init"+left1 +"blah");
                RobotLog.i("Saw: " + event.kind + " Confidence: " + event.minerals.get(0).getConfidence());
                RobotLog.i("Saw: " + event.kind + " LEFT: " + event.minerals.get(0).getLeft());

                imageMidpoint = event.minerals.get(0).getImageWidth() / 2.0;
                goldMidpoint  = (event.minerals.get(0).getWidth() / 2.0) + left1;

                leftMidpointTlm = telemetry.addData("LEFT_MDPT: ", goldMidpoint);
                imageMidpointTlm = telemetry.addData(" IMG_MDPT: ", imageMidpoint);

                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    if (Math.abs(imageMidpoint-goldMidpoint) < margin) {
                        inCenter = true;
                        RobotLog.i("506 Found gold");
                        mdTask.stop();
                    }
                }
            }
        };
        mdTask.init(telemetry,hardwareMap);
        mdTask.setDetectionKind(MineralDetectionTask.DetectionKind.LARGEST_GOLD);
        addTask(mdTask);
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;
            switch (event.kind) {
                case BUTTON_A_DOWN:
                    selectPosition(LilacAutonomous.RobotPosition.CRATER);
                    robotPosition = RobotPosition.CRATER;
                    positionItem.setValue("Crater");
                    //setMarkerPath();
                    break;
                case BUTTON_Y_DOWN:
                    selectPosition(LilacAutonomous.RobotPosition.MARKER);
                    robotPosition = RobotPosition.MARKER;
                    positionItem.setValue("Marker");
                    //setMarkerPath();
                    break;
                default:
                    break;
            }
        }
    }

    private void selectPosition(LilacAutonomous.RobotPosition choice)
    {
        if (choice == RobotPosition.CRATER) {
            robotPosition = LilacAutonomous.RobotPosition.CRATER;
            RobotLog.i("506 Position: CRATER");

        } else {
            robotPosition = LilacAutonomous.RobotPosition.MARKER;
            RobotLog.i("506 Position: Marker");
        }
    }


    private void setMarkerPath()
    {
        switch (robotPosition) {
            case CRATER:
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 30, -STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 25 , -STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN, 6 , TURN_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 3 , STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20 , STRAIGHT_SPEED);
                break;
            case MARKER:
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15 , -STRAIGHT_SPEED);
                break;
            default:
                break;
        }
    }

    public void setOtherPaths()
    {
        runLatchPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3, -STRAIGHT_SPEED);

        // FIXME
        detachPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 0.1, STRAIGHT_SPEED);

        unlatchScan.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 4, STRAIGHT_SPEED);
        unlatchScan.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 11, -STRAIGHT_SPEED);
        //unlatchScan.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, , STRAIGHT_SPEED);

        knockOff.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 25, -STRAIGHT_SPEED);

    }

    @Override
    public void start() {
       moveAway();
    }

    public void runLatch()
    {
        RobotLog.i("506 running latch in");
        this.addTask(new DeadReckonTask(this, runLatchPath, single) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Run latch done");
                    lowerRobot();
                }
            }
        });

        latchServo.setPosition(LATCH_OPEN);
    }


    /*public void unlatchArm() {
        latchServo.setPosition(LATCH_OPEN);
        this.addTask(new SingleShotTimerTask(this, 2000) { // 2 second
            @Override
            public void handleEvent(RobotEvent e) {
                SingleShotTimerEvent event = (SingleShotTimerEvent) e;
                if (event.kind == EventKind.EXPIRED) {
                    RobotLog.i("506 unlatchArm: finished");
                    lowerRobot();
                }
            }
        });
    } */

    private void lowerRobot()
    {
        RobotLog.i("506 detaching");
        this.addTask(new DeadReckonTask(this, detachPath, single) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Detaching done");
                    //moveAway();
                }
            }
        });
    }

    private void moveAway()
    {
        this.addTask(new DeadReckonTask(this, unlatchScan,  drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Unlatch and move away done");
                    sample();
                }
            }
        });
    }

    private void detectWait() {
        // TODO Single shot timer task
    }

    public void sample()
    {
        addTask(mdTask);
        initPos = drivetrain.getCurrentPosition();
        RobotLog.i("506 Lilac Current Position: " + initPos);

        drivetrain.strafe(-0.3);
    }

    private void knockOff()
    {
        finalPos = drivetrain.getCurrentPosition();
        deltaPos = finalPos - initPos;

        if (robotPosition == RobotPosition.CRATER) {
            if (deltaPos < FIRST) {
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN, 50, TURN_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, -STRAIGHT_SPEED);
            } else if (deltaPos > FIRST && deltaPos < THIRD) {
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, -STRAIGHT_SPEED);
            } else if (deltaPos > SECOND) {
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN, 50, -TURN_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, -STRAIGHT_SPEED);
            }
        } else {
            if (deltaPos < FIRST) {
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN, 50, TURN_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, -STRAIGHT_SPEED);
            } else if (deltaPos > FIRST && deltaPos < THIRD) {
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, -STRAIGHT_SPEED);
            } else if (deltaPos > SECOND) {
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN, 50, -TURN_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, -STRAIGHT_SPEED);
            }
        }

        this.addTask(new DeadReckonTask(this, knockOff, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Knock off done");
                    toMarker();
                }
            }
        });
    }

    private void toMarker()
    {
        this.addTask(new DeadReckonTask(this, scoreMarker, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Going to score marker");
                    dropMarker();
                }
            }
        });

    }

    private void dropMarker()
    {
        marker.setPosition(MARKER_OPEN);
        RobotLog.i("Marker Scored");
    }


}
