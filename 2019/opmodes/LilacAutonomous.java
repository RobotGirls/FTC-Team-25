package opmodes;
/*
 * FTC Team 25: Created by Elizabeth Wu, November 24, 2018
 */

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.MineralDetectionTask;
import team25core.OneWheelDirectDrivetrain;
import team25core.OneWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;

@Autonomous(name = "Lilac Autonomous", group = "Team 25")
public class LilacAutonomous extends Robot {


    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;
    private Servo   marker;
    private Servo   latchServo;

    private FourWheelDirectDrivetrain drivetrain;
    private OneWheelDirectDrivetrain single;

    private LilacAutonomous.Position position;
    private DeadReckonPath scoreMarker;
    private DeadReckonPath detachPath;
    private DeadReckonPath unlatchScan;

    private MineralDetectionTask mdTask;


    private Telemetry.Item positionItem;

    public enum Position {
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
        marker      = hardwareMap.servo.get("marker");

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        single     = new OneWheelDirectDrivetrain(latchArm);

        // Telemetry for selection.
        positionItem = telemetry.addData("POSITION", "Unselected (Y/A)");

        // Init selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        // Path setup.
        scoreMarker = new DeadReckonPath();
        detachPath  = new DeadReckonPath();
        unlatchScan = new DeadReckonPath();

        // Segment setup.
        setOtherPaths();

        mdTask = new MineralDetectionTask(this);
        mdTask.init(telemetry, hardwareMap);
        mdTask.setDetectionKind(MineralDetectionTask.DetectionKind.LARGEST_GOLD);
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            switch (event.kind) {
                case BUTTON_Y_DOWN:
                    selectPosition(LilacAutonomous.Position.CRATER);
                    positionItem.setValue("Crater");
                    break;
                case BUTTON_A_DOWN:
                    selectPosition(LilacAutonomous.Position.MARKER);
                    positionItem.setValue("Marker");
                    break;
                default:
                    break;
            }
            setMarkerPath();
        }
    }



    public void selectPosition(LilacAutonomous.Position choice) {
        if (choice == Position.CRATER) {
            position = LilacAutonomous.Position.CRATER;
            RobotLog.i("506 Position: CRATER");
        } else {
            position = LilacAutonomous.Position.MARKER;
            RobotLog.i("506 Position: Marker");
        }
    }

    @Override
    public void start() {
        detach();
    }

    public void detach() {
        RobotLog.i("506 detaching");
        this.addTask(new DeadReckonTask(this, detachPath, single) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Detaching done");
                     moveToFirst();
                }
            }
        });
    }

    public void moveToFirst() {


        this.addTask(new DeadReckonTask(this, unlatchScan, single) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Unlatch and move for first sample done");
                    sample();
                }
            }
        });
    }

    public void sample() {
        //TODO: TEST 11/25/18
        this.addTask(new MineralDetectionTask(this) {
            @Override
            public void handleEvent(RobotEvent e) {
                MineralDetectionEvent event = (MineralDetectionEvent) e;
                RobotLog.i("Saw: " + event.kind + "Confidence: " + event.minerals.get(0).getConfidence());
            }
        });
    }


    public void setMarkerPath() {
        // TODO: implement this AFTER sample + push
        if (position == Position.CRATER) {
            scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, -Lilac.STRAIGHT_SPEED);
        } else {
            scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, -Lilac.STRAIGHT_SPEED);
        }
    }

    public void setOtherPaths() {
        // FIXME: all measurements are approx. as of 11/24/18

        detachPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 2, Lilac.STRAIGHT_SPEED);

        unlatchScan.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 8, Lilac.SIDEWAYS_DETACH_SPEED);
        unlatchScan.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7, -Lilac.STRAIGHT_SPEED);

    }

}
