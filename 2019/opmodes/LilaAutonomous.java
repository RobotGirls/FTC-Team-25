package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.RunToEncoderValueTask;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Bella Heinrichs on 10/23/2018.
 */

@Autonomous(name = "Lila Autonomous", group = "Team 25")
@Disabled
public class LilaAutonomous extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;
    private Servo   marker;
    private Servo   latchServo;

    private LilaAutonomous.Alliance alliance;
    private LilaAutonomous.Position position;
    private DeadReckonPath latch;
    private DeadReckonPath detachRobot;
    private DeadReckonPath scoreMarker;
    private DeadReckonTask gold;
    private SingleShotTimerTask stt;
    private SingleShotTimerTask moveDelay;

    private Telemetry.Item allianceItem;
    private Telemetry.Item positionItem;

    private int combo = 0;
    private int color = 0;

    private static final int SPEED_MULTIPLIER = -1;
    private int distance = 0;


    // Park combos.
    private static final int BLUE_CRATER = 0;
    private static final int RED_CRATER = 1;
    private static final int BLUE_MARKER = 2;
    private static final int RED_MARKER = 3;

    private FourWheelDirectDrivetrain drivetrain;

    public enum Alliance {
        RED,
        BLUE,
    }

    public enum Position {
        MARKER,
        CRATER,
    }


    @Override
    public void init() {
        // telemetry.setAutoClear(false);

        // Hardware mapping.
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        latchArm    = hardwareMap.dcMotor.get("latch");
        marker      = hardwareMap.servo.get("marker");

        // Telemetry setup.
        // telemetry.setAutoClear(false);
        allianceItem    = telemetry.addData("ALLIANCE", "Unselected (X/B)");
        positionItem    = telemetry.addData("POSITION", "Unselected (Y/A)");

        // Path setup.
        latch        = new DeadReckonPath();
        detachRobot  = new DeadReckonPath();
        scoreMarker  = new DeadReckonPath();

        // Single shot timer tasks for delays.
        // stt = new SingleShotTimerTask(this, 1500);          // Delay resetting arm position
        // moveDelay = new SingleShotTimerTask(this, 500);     // Delay moving after setting arm down.

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            switch (event.kind) {
                case BUTTON_X_DOWN:
                    selectAlliance(LilaAutonomous.Alliance.BLUE);
                    allianceItem.setValue("Blue");
                    break;
                case BUTTON_B_DOWN:
                    selectAlliance(LilaAutonomous.Alliance.RED);
                    allianceItem.setValue("Red");
                    break;
                case BUTTON_Y_DOWN:
                    selectPosition(LilaAutonomous.Position.CRATER);
                    positionItem.setValue("Crater");
                    break;
                case BUTTON_A_DOWN:
                    selectPosition(LilaAutonomous.Position.MARKER);
                    positionItem.setValue("Marker");
                    break;
                default:
                    break;
            }
            setMarkerPath();
        }
        //setLatchPath();
    }

    @Override
    public void start()
    {
        moveToDepot();
        /*
        addTask(new SingleShotTimerTask(this, 500) {

            @Override
            public void handleEvent(RobotEvent e) {
                doLowerRobot();
            }
        });
        */
    }

    public void moveToDepot() {
        RobotLog.i("506 doMoveToDepot");
       /* scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 8, Lilac.STRAIGHT_SPEED);
        scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,30, Lilac.STRAIGHT_SPEED);
        scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -Lilac.STRAIGHT_SPEED);
        scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,90, -Lilac.STRAIGHT_SPEED);
        scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, -Lilac.STRAIGHT_SPEED);*/
        this.addTask(new DeadReckonTask(this, scoreMarker, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("506 scoreMarker path done");
                } else {
                    RobotLog.i("506 scoreMarker handler being called");
                }
            }
        });
    }

     public void detachLatch() {
        this.addTask(new DeadReckonTask(this, detachRobot, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    moveToDepot();
                }
            }
        });
    }


    public void lowerRobot() {
        this.addTask(new RunToEncoderValueTask(this, latchArm, 0, 1.0  ) {
            @Override
            public void handleEvent(RobotEvent e) {
                // Right now the "LATCH" path both de-latches the robot and navigates to depot
                detachLatch();
            }
        });
    }


    private void selectAlliance(LilaAutonomous.Alliance color) {
        if (color == LilaAutonomous.Alliance.BLUE) {
            // Blue setup.
            RobotLog.i("506 Alliance: BLUE");
            alliance = LilaAutonomous.Alliance.BLUE;
        } else {
            // Red setup.
            RobotLog.i("506 Alliance: RED");
            alliance = LilaAutonomous.Alliance.RED;
        }
    }

    public void selectPosition(LilaAutonomous.Position choice) {
        if (choice == LilaAutonomous.Position.CRATER) {
            position = LilaAutonomous.Position.CRATER;
            RobotLog.i("506 Position: CRATER");
        } else {
            position = LilaAutonomous.Position.MARKER;
            RobotLog.i("506 Position: MARKER");
        }
    }

    private void setLatchPath()
    {
        latch.stop();
        /*latch.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 2, Lilac.STRAIGHT_SPEED); // Right
        latch.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 27, Lilac.STRAIGHT_SPEED * SPEED_MULTIPLIER);
        if (position == Position.MARKER) {
            latch.addSegment(DeadReckonPath.SegmentType.TURN, 135, Lilac.TURN_SPEED);
            latch.addSegment(DeadReckonPath.SegmentType.LEFT_DIAGONAL, 25, Lilac.STRAIGHT_SPEED * SPEED_MULTIPLIER);
            // Change to front right diagonal after implementing that in deadReckonPath segment types
            // Jk we need to figure out the speeds here though
            latch.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 72, Lilac.STRAIGHT_SPEED);*/
    }

    private void setDetachRobot()
    {
        detachRobot.stop();
        /* detachRobot.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, Lilac.SIDEWAYS_DETACH_SPEED);
        detachRobot.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3, - Lilac.SIDEWAYS_DETACH_SPEED);
        detachRobot.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, - Lilac.SIDEWAYS_DETACH_SPEED);*/
    }


    private void setMarkerPath() { /*
        // Edit later when we figure out sensing gold block & need to implement
        // specific marker paths based off different positions of gold block.

        if (alliance == LilaAutonomous.Alliance.RED) {  // Blue and crater = 0; Red and crater = 1, Blue and marker = 2, Red and marker = 3

            color = 1;
        } else if (alliance == LilaAutonomous.Alliance.BLUE) {
            color = 0;
        }

        if (position == LilaAutonomous.Position.MARKER) {
            distance = 2;
        } else {
            distance = 0;
        }

        combo = color + distance;

        // + whichSide;

        switch (combo) {
            case BLUE_CRATER:
                RobotLog.i("506 Case: BLUE_CRATER");
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 8, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,30, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,90, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, Lilac.STRAIGHT_SPEED);
                break;
            case RED_CRATER:
                RobotLog.i("506 Case: RED_CRATER");
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 8, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,30, Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.TURN,90, -Lilac.STRAIGHT_SPEED);
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, -Lilac.STRAIGHT_SPEED);
                break;
            case BLUE_MARKER:
                RobotLog.i("506 Case: BLUE_MARKER");
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 24, -Lilac.STRAIGHT_SPEED);
                break;
            case RED_MARKER:
                RobotLog.i("506 Case: RED_MARKER");
                scoreMarker.stop();
                scoreMarker.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 24, -Lilac.STRAIGHT_SPEED);
                break;
            default:
                break;
        }
    } */
    }
}
