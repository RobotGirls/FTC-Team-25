package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.HoldPositionTask;
import team25core.MechanumGearedDrivetrain;
import team25core.MineralDetectionTask;
import team25core.OneWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;


/*
 * FTC Team 25: Created by Elizabeth Wu, November 24, 2018
 */

@Autonomous(name = "Lilac Rotate Auto", group = "Team 25")
@Disabled
public class LilacRotateAuto extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;
    private Servo   marker;
    //private Servo   latchServo;

    private final int TICKS_PER_INCH = Lilac.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = Lilac.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = 0.4; // Autonomous - slower; it's diff from LilacConstants
    private final double TURN_SPEED = Lilac.TURN_SPEED;

   // private FourWheelDirectDrivetrain drivetrain;
    // GEAR: Switching to Mechanum drivetrain for encoder tick multiplier
    private MechanumGearedDrivetrain drivetrain;

    private OneWheelDirectDrivetrain single;

    private LilacRotateAuto.RobotPosition robotPosition;
    private DeadReckonPath scoreMarker;
    private DeadReckonPath runLatchPath;
    private DeadReckonPath detachPath;
    private DeadReckonPath unlatchScan;
    private DeadReckonPath knockOff;

    private HoldPositionTask holdPosTask;
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
    private static double FIRST = 100;
    private static double SECOND = 200;
    private static double THIRD = 300;



    public static double LATCH_OPEN     = 255 / 256.0;
    public static double LATCH_CLOSED   = 40  / 256.0;
    public static double MARKER_OPEN    = 254 / 256.0;
    public static double MARKER_CLOSED  = 129 / 256.0;


    private Telemetry.Item positionItem;
    private Telemetry.Item actualPosItem;
    private Telemetry.Item runPos;
    private Telemetry.Item noDisconnect;

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
       // latchServo  = hardwareMap.servo.get("latchServo");
        marker      = hardwareMap.servo.get("marker");

        //drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
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
        latchArm.setPower(0.0);
       // latchServo.setPosition(LATCH_CLOSED);

        // Close marker servo
        marker.setPosition(MARKER_CLOSED);

        // Telemetry for selection.
        positionItem = telemetry.addData("POSITION", "Unselected (Y/A)");
        noDisconnect = telemetry.addData("Connect", "!");

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

        // Keep arm in position.
        holdPosTask = new HoldPositionTask(this,latchArm, 1);
        this.addTask(holdPosTask);

        // Setting up mineral detection.
        mdTask = new MineralDetectionTask(this) {
            @Override
            public void handleEvent(RobotEvent e) {
                MineralDetectionEvent event = (MineralDetectionEvent) e;
                confidence1 = event.minerals.get(0).getConfidence();
                left1 = event.minerals.get(0).getLeft();
                RobotLog.i("Saw: " + event.kind + " Confidence: " + event.minerals.get(0).getConfidence());

                imageMidpoint = event.minerals.get(0).getImageWidth() / 2.0;
                goldMidpoint  = (event.minerals.get(0).getWidth() / 2.0) + left1;

                leftMidpointTlm = telemetry.addData("LEFT_MDPT: ", goldMidpoint);
                imageMidpointTlm = telemetry.addData(" IMG_MDPT: ", imageMidpoint);

                RobotLog.i("506 Image midpoint: " + imageMidpoint);
                RobotLog.i("506 Gold midpoint: " + goldMidpoint);

                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    if (Math.abs(imageMidpoint-goldMidpoint) < margin) {
                        drivetrain.stop();
                        stop();
                        inCenter = true;
                        RobotLog.i("506 Found gold");
                       // mdTask.stop();
                       // drivetrain.stop();
                        knockOff();
                    }
                }
            }
        };
        mdTask.init(telemetry,hardwareMap);
        mdTask.setDetectionKind(MineralDetectionTask.DetectionKind.LARGEST_GOLD);
    }

    public void loop()
    {
        super.loop();
        // telemetry.clear();
       // telemetry.addData("Do not disconnect", "!");
        noDisconnect.setValue("Don't disconnect");
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;
            switch (event.kind) {
                case BUTTON_A_DOWN:
                    selectPosition(LilacRotateAuto.RobotPosition.CRATER);
                    robotPosition = RobotPosition.CRATER;
                    positionItem.setValue("Crater");
                    setMarkerPath();
                    break;
                case BUTTON_Y_DOWN:
                    selectPosition(LilacRotateAuto.RobotPosition.MARKER);
                    robotPosition = RobotPosition.MARKER;
                    positionItem.setValue("Marker");
                    setMarkerPath();
                    break;
                default:
                    break;
            }
        }
    }

    private void selectPosition(LilacRotateAuto.RobotPosition choice)
    {
        if (choice == RobotPosition.CRATER) {
            robotPosition = LilacRotateAuto.RobotPosition.CRATER;
            RobotLog.i("506 Position: CRATER");

        } else {
            robotPosition = LilacRotateAuto.RobotPosition.MARKER;
            RobotLog.i("506 Position: Marker");
        }
    }


    private void setMarkerPath()
    {
        switch (robotPosition) {
            case CRATER:
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, -STRAIGHT_SPEED);
            case MARKER:
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15 , -STRAIGHT_SPEED);
                break;
            default:
                break;
        }
    }

    private void setOtherPaths()
    {
        runLatchPath.stop();
        runLatchPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3, -STRAIGHT_SPEED);
        //runLatchPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 55, -STRAIGHT_SPEED);

        detachPath.stop();
        detachPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 55, STRAIGHT_SPEED); // This is to run the arm DOWN

        unlatchScan.stop();
       // unlatchScan.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 2, STRAIGHT_SPEED);
        unlatchScan.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 6, STRAIGHT_SPEED); // Move away from lander
        unlatchScan.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1, -STRAIGHT_SPEED);
        unlatchScan.addSegment(DeadReckonPath.SegmentType.TURN, 13, -TURN_SPEED);
        unlatchScan.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7, -STRAIGHT_SPEED);
        unlatchScan.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 8, STRAIGHT_SPEED);


        knockOff.stop();
        knockOff.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 23, -STRAIGHT_SPEED); // try to push AND park in crater
        //marker was 15
        knockOff.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3, STRAIGHT_SPEED);

    }

    @Override
    public void start()
    {
        this.removeTask(holdPosTask);
        lowerRobot();
    }

    private void lowerRobot()
    {
        RobotLog.i("506 detaching");
        this.addTask(new DeadReckonTask(this, detachPath, single) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Detaching done");
                    moveAway();
                }
            }
        });
    }

    private void moveAway()
    {
        RobotLog.i("506 Moving away");
        this.addTask(new DeadReckonTask(this, unlatchScan,  drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Unlatch and move away done");
                    rotate();
                }
            }
        });
    }

    public void rotate()
    {
        addTask(mdTask);
        initPos = drivetrain.getCurrentPosition();
        RobotLog.i("506 Lilac Current Position: " + initPos);

        drivetrain.turn(-0.15);
    }

    private void knockOff()
    {
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
                    RobotLog.i("506 Going to score marker or park");
                    dropMarker();
                }
            }
        });

    }

    private void dropMarker()
    {
        if (robotPosition == RobotPosition.MARKER) {
            marker.setPosition(MARKER_OPEN);
            RobotLog.i("Marker Scored");
        }
    }


}
