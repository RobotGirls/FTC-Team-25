package opmodes;

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
import team25core.SingleShotTimerTask;

import static test.MDConstants.LEFT_MAX;
import static test.MDConstants.LEFT_MIN;


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

    private FourWheelDirectDrivetrain drivetrain;
    private OneWheelDirectDrivetrain single;

    private LilacAutonomous.RobotPosition robotPosition;
    private DeadReckonPath scoreMarker;
    private DeadReckonPath detachPath;
    private DeadReckonPath unlatchScan;
    private DeadReckonPath knockOff;


    private MineralDetectionTask mdTask;
    private double confidence1;
    private double left1;
    private double leftMin;
    private boolean inCenter;
    private int step;
    private double imageMidpoint;
    private double goldMidpoint;
    private double margin = 40;


    public static double LATCH_OPEN = 160.0 / 256.0 ;
    public static double LATCH_CLOSED = 220.0 / 256.0 ;


    private Telemetry.Item positionItem;

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

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
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

        // Telemetry for selection.
        positionItem = telemetry.addData("POSITION", "Unselected (Y/A)");

        // Init selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        // Path setup.
        scoreMarker = new DeadReckonPath();
        detachPath  = new DeadReckonPath();
        unlatchScan = new DeadReckonPath();
        knockOff   = new DeadReckonPath();

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

                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    if (Math.abs(imageMidpoint-goldMidpoint) < margin) {
                        inCenter = true;
                        knockOff();
                        mdTask.stop();
                        drivetrain.stop();
                        drivetrain.setStrafeReverse(false);
                    }
                }
            }
        };

        mdTask.init(telemetry,hardwareMap);
        mdTask.setDetectionKind(MineralDetectionTask.DetectionKind.LARGEST_GOLD);
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            switch (event.kind) {
                case BUTTON_A_DOWN:
                    selectPosition(LilacAutonomous.RobotPosition.CRATER);
                    positionItem.setValue("Crater");
                    break;
                case BUTTON_Y_DOWN:
                    selectPosition(LilacAutonomous.RobotPosition.MARKER);
                    positionItem.setValue("Marker");
                    break;
                default:
                    break;
            }
            setMarkerPath();
        }
    }

    public void selectPosition(LilacAutonomous.RobotPosition choice) {
        if (choice == RobotPosition.CRATER) {
            robotPosition = LilacAutonomous.RobotPosition.CRATER;
            RobotLog.i("506 Position: CRATER");
        } else {
            robotPosition = LilacAutonomous.RobotPosition.MARKER;
            RobotLog.i("506 Position: Marker");
        }
    }


    public void setMarkerPath() {
        // TODO: implement this AFTER sample + push
        if (robotPosition == RobotPosition.CRATER) {
            // For now - either score marker & park there, or park crater no marker score
            scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, -Lilac.STRAIGHT_SPEED);
        } else {
            // For now - score marker & park there
            scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, -Lilac.STRAIGHT_SPEED);
        }
    }

    public void setOtherPaths() {
        // TODO: all measurements are approx. as of 11/24/18

        detachPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 0.09, Lilac.STRAIGHT_SPEED);

        unlatchScan.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 3, Lilac.SIDEWAYS_DETACH_SPEED);
        unlatchScan.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 2.5, -Lilac.STRAIGHT_SPEED);

        knockOff.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 2.5, -Lilac.STRAIGHT_SPEED);


    }

    @Override
    public void start() {
        //unlatchArm();
        moveAway();

    }

    public void unlatchArm() {
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
    }

    private void lowerRobot() {
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

    private void moveAway() {
        this.addTask(new DeadReckonTask(this, unlatchScan, drivetrain) {
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

    public void sample() {
        addTask(mdTask);
        //drivetrain.strafeReverse(-0.1);
        drivetrain.strafeReverse(0.1);
    }

    private void knockOff() {
        this.addTask(new DeadReckonTask(this, knockOff, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 Knock off done");
                    dropMarker();
                }
            }
        });
    }

    private void dropMarker() {
       /* if (step == 1) {
            //this.addTask(new DeadReckonTask(this, firstTime, drivetrain) {
                @Override

            });
        } */
    }



}
