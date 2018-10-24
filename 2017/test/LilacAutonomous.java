package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.CameraDevice;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import test.Lilac;
import team25core.ColorThiefTask;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.VuforiaBase;

/**
 * FTC Team 25: Created by Bella Heinrichs on 10/23/2018.
 */

@Autonomous(name = "Lilac Autonomous", group = "Team 25")
@Disabled
public class LilacAutonomous extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;
    private Servo marker;
    //private ColorThiefTask colorThiefTask;
    private DeviceInterfaceModule cdim;
    private LilacAutonomous.Alliance alliance;
    private LilacAutonomous.Position position;
    //private LilacAutonomous.Side side;
    private DeadReckonPath latch;
    private DeadReckonPath scoreMarker;
    private DeadReckonTask gold;
    private SingleShotTimerTask stt;
    private SingleShotTimerTask moveDelay;

    private Telemetry.Item allianceItem;
    private Telemetry.Item positionItem;

    private int combo = 0;
    private int color = 0;

    private static final int TURN_MULTIPLIER = -1;
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
        telemetry.setAutoClear(false);


        // Hardware mapping.
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        //latchArm       = hardwareMap.dcMotor.get("latch");


        // Telemetry setup.
        telemetry.setAutoClear(false);
        allianceItem    = telemetry.addData("ALLIANCE", "Unselected (X/B)");
        positionItem    = telemetry.addData("POSITION", "Unselected (Y/A)");

        // Path setup.
        latch        = new DeadReckonPath();


        // Single shot timer tasks for delays.
        //stt = new SingleShotTimerTask(this, 1500);          // Delay resetting arm position
        //moveDelay = new SingleShotTimerTask(this, 500);     // Delay moving after setting arm down.

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);

        drivetrain.setNoncanonicalMotorDirection();

        //sense();

    }

    @Override
    public void start()
    {

        // 15/256
        addTask(new SingleShotTimerTask(this, 500) {
            @Override
            public void handleEvent(RobotEvent e) {
                robot.addTask(new DeadReckonTask(robot, latch, drivetrain) {
                    @Override
                    public void handleEvent(RobotEvent e) {
                        DeadReckonEvent path = (DeadReckonEvent) e;
                        if (path.kind == EventKind.PATH_DONE) {
                            addTask(new DeadReckonTask(robot, scoreMarker, drivetrain) {
                                @Override
                                public void handleEvent(RobotEvent e) {
                                    DeadReckonEvent path = (DeadReckonEvent) e;
                                }
                            });
                        }
                    }
                });
                robot.addTask(new SingleShotTimerTask(robot, 500) {
                    @Override
                    public void handleEvent(RobotEvent e) {
                    }
                });
            }
        });

    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            //RobotLog.i("Jewel: Detected " + e.toString());

            switch (event.kind) {
                case BUTTON_X_DOWN:
                    selectAlliance(LilacAutonomous.Alliance.BLUE);
                    allianceItem.setValue("Blue");
                    break;
                case BUTTON_B_DOWN:
                    selectAlliance(LilacAutonomous.Alliance.RED);
                    allianceItem.setValue("Red");
                    break;
                case BUTTON_Y_DOWN:
                    selectPosition(LilacAutonomous.Position.CRATER);
                    positionItem.setValue("Far");
                    break;
                case BUTTON_A_DOWN:
                    selectPosition(LilacAutonomous.Position.MARKER);
                    positionItem.setValue("Near");
                    break;
                case RIGHT_BUMPER_DOWN:
                    //togglePolling();
                    break;
                default:
                    break;
            }
        } setupParkPath();
    }

    /*private void togglePolling() {
        if (pollOn == false) {
            colorThiefTask.setPollingMode(ColorThiefTask.PollingMode.ON);
            pollOn = true;

        } else {
            colorThiefTask.setPollingMode(ColorThiefTask.PollingMode.OFF);
            pollOn = false;
        }
    } */

    private void selectAlliance(LilacAutonomous.Alliance color) {
        if (color == LilacAutonomous.Alliance.BLUE) {
            // Blue setup.
            RobotLog.i("506 Alliance: BLUE");
            alliance = LilacAutonomous.Alliance.BLUE;
        } else {
            // Red setup.
            RobotLog.i("506 Alliance: RED");
            alliance = LilacAutonomous.Alliance.RED;
        }
    }

    public void selectPosition(LilacAutonomous.Position choice) {
        if (choice == LilacAutonomous.Position.CRATER) {
            position = LilacAutonomous.Position.CRATER;
            RobotLog.i("506 Position: FAR");
        } else {
            position = LilacAutonomous.Position.MARKER;
            RobotLog.i("506 Position: NEAR");
        }
    }

    private void setupParkPath()
    {


        if (alliance == LilacAutonomous.Alliance.RED) {
            color = 1;
        } else if (alliance == LilacAutonomous.Alliance.BLUE) {
            color = 0;
        }

        if (position == LilacAutonomous.Position.MARKER) {
            //distance = 2;
        } else {
            //distance = 0;
        }

        combo = color + distance;

        // + whichSide;

        switch (combo) {
            case BLUE_CRATER:
                RobotLog.i("506 Case: BLUE_CRATER");
                latch.stop();
                latch.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 27, test.Lilac.STRAIGHT_SPEED * TURN_MULTIPLIER);
                break;
            case RED_CRATER:
                RobotLog.i("506 Case: RED_CRATER");
                latch.stop();
                latch.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 27, test.Lilac.STRAIGHT_SPEED);
                break;
            case BLUE_MARKER:
                RobotLog.i("506 Case: BLUE_MARKER");
                latch.stop();
                latch.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 18, test.Lilac.STRAIGHT_SPEED * TURN_MULTIPLIER);
                break;
            case RED_MARKER:
                RobotLog.i("506 Case: RED_MARKER");
                latch.stop();
                latch.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, test.Lilac.STRAIGHT_SPEED);
                break;
            default:
                break;
        }
    }
}
