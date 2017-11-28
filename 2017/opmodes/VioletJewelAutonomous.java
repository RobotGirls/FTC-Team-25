package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.CameraDevice;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.ColorThiefTask;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

/*
 * FTC Team 25: Created by Elizabeth Wu on 10/28/17.
 */

@Autonomous(name = "Violet Jewel Autonomous", group = "Team 25")
public class VioletJewelAutonomous extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor rotate;
    private Servo jewel;
    private ColorThiefTask colorThiefTask;
    private DeviceInterfaceModule cdim;
    private Alliance alliance;
    private Position position;
    private Side side;
    private DeadReckonPath park;
    private DeadReckonPath pushJewel;
    private DeadReckonTask task;
    private SingleShotTimerTask stt;
    private SingleShotTimerTask moveDelay;
    private Telemetry.Item particle;
    private Telemetry.Item allianceItem;
    private Telemetry.Item positionItem;
    private Telemetry.Item pollItem;
    private Telemetry.Item flashItem;

    boolean flashOn = false;
    boolean pollOn = false;

    private final int TICKS_PER_INCH = Violet.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = Violet.TICKS_PER_DEGREE;
    private int turnMultiplier = -1;
    private int moveMultiplier = -1;
    private int color = 0;
    private int distance = 0;
    private int whichSide = 0;
    private int combo = 0;


    // Park combos.
    private static final int BLUE_FAR = 0;
    private static final int RED_FAR = 1;
    private static final int BLUE_NEAR = 2;
    private static final int RED_NEAR = 3;
   /*
    private static final int BLUE_FAR_LEFT = 4;
    private static final int RED_FAR_LEFT = 5;
    private static final int BLUE_NEAR_LEFT = 6;
    private static final int RED_NEAR_LEFT = 7;
    */


    private FourWheelDirectDrivetrain drivetrain;


    public enum Alliance {
        RED,
        BLUE,
    }

    public enum Position {
        NEAR,
        FAR,
    }

    public enum Side {
        LEFT,
        RIGHT,
    }

    @Override
    public void init() {
        telemetry.setAutoClear(false);


        // Hardware mapping.
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        jewel       = hardwareMap.servo.get("jewel");


        // Telemetry setup.
        telemetry.setAutoClear(false);
        allianceItem    = telemetry.addData("ALLIANCE", "Unselected (X/B)");
        positionItem    = telemetry.addData("POSITION", "Unselected (Y/A)");
        particle        = telemetry.addData("Particle: ", "No data");
        //flashItem       = telemetry.addData("Flash:", "Off");

        // Path setup.
       // pushJewel   = new DeadReckonPath();
        park        = new DeadReckonPath();

        jewel.setPosition(0.56);    // 145/256

        // Single shot timer tasks for delays.
        stt = new SingleShotTimerTask(this, 1500);          // Delay resetting arm position
        moveDelay = new SingleShotTimerTask(this, 500);     // Delay moving after setting arm down.

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);

        sense();

    }


    @Override
    public void start()
    {
        jewel.setPosition(0.05);
        // 15/256
        RobotLog.i("506 Arm deployed in start.");
        addTask(new SingleShotTimerTask(this, 500) {
                    @Override
                    public void handleEvent(RobotEvent e) {
                        RobotLog.i("506 Path delay ran.");
                        robot.addTask(new DeadReckonTask(robot, pushJewel, drivetrain) {
                            @Override
                            public void handleEvent(RobotEvent e) {
                                DeadReckonEvent path = (DeadReckonEvent) e;
                                if (path.kind == EventKind.PATH_DONE) {
                                    jewel.setPosition(0.56);
                                    RobotLog.i("506 Arm reset to initial position after path done.");
                                   addTask(new DeadReckonTask(robot, park, drivetrain) {
                                        @Override
                                        public void handleEvent(RobotEvent e) {
                                        }
                                    });


                                }
                            }
                        });
                    }
                });

    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            RobotLog.i("Jewel: Detected " + e.toString());

            switch (event.kind) {
                case BUTTON_X_DOWN:
                    selectAlliance(Alliance.BLUE);
                    allianceItem.setValue("Blue");
                    break;
                case BUTTON_B_DOWN:
                    selectAlliance(Alliance.RED);
                    allianceItem.setValue("Red");
                    break;
                case BUTTON_Y_DOWN:
                    selectPosition(Position.FAR);
                    positionItem.setValue("Far");
                    break;
                case BUTTON_A_DOWN:
                    selectPosition(Position.NEAR);
                    positionItem.setValue("Near");
                    break;
                case RIGHT_BUMPER_DOWN:
                    togglePolling();
                    break;
                // case RIGHT_TRIGGER_DOWN:
                //   toggleFlash();
                //    break;
                default:
                    break;
            }
        }
    }

    private void togglePolling() {
        if (pollOn == false) {
            colorThiefTask.setPollingMode(ColorThiefTask.PollingMode.ON);
            pollOn = true;

        } else {
            colorThiefTask.setPollingMode(ColorThiefTask.PollingMode.OFF);
            pollOn = false;
        }
    }

   /*  Currently not using flash! Front camera doesn't have one.

   private void toggleFlash() {
        if (flashOn == false) {
            CameraDevice.getInstance().setFlashTorchMode(flashOn);
            flashOn = true;
            flashItem.setValue("On");
        } else {
            CameraDevice.getInstance().setFlashTorchMode(!flashOn);
            flashOn = false;
            flashItem.setValue("Off");
        }
    }
    */

    private void sense()
    {
         colorThiefTask = new ColorThiefTask(this) {
            @Override
            public void handleEvent(RobotEvent e) {
                ColorThiefTask.ColorThiefEvent event = (ColorThiefEvent) e;
                particle.setValue(event.toString());

                pushJewel = new DeadReckonPath();

                if (alliance == Alliance.RED) {
                    if (event.kind == EventKind.RED) {
                        pushJewel.stop();
                        RobotLog.i("506 Sensed RED");
                        pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 35, Violet.TURN_SPEED * turnMultiplier);
                        RobotLog.i("506 First turn done");
                        pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 35, Violet.TURN_SPEED);
                        RobotLog.i("506 Second turn done");
                    } else {
                        RobotLog.i("506 Sensed BLUE");
                        //pushJewel.stop();
                        //pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, Violet.STRAIGHT_SPEED * moveMultiplier);
                    }
                } else {
                    if (event.kind == EventKind.BLUE) {
                        RobotLog.i("506 Sensed BLUE");
                        pushJewel.stop();
                        pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 35, Violet.TURN_SPEED * turnMultiplier);
                        pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 35, Violet.TURN_SPEED);
                    } else {
                        RobotLog.i("506 Sensed RED");
                        //pushJewel.stop();
                        //pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -Violet.STRAIGHT_SPEED);
                    }
                }

            }
        };

        addTask(colorThiefTask);
        addTask(stt);

        setupParkPath();
    }

    private void selectAlliance(Alliance color) {
        if (color == Alliance.BLUE) {
            // Blue setup.
            RobotLog.i("506 Doing blue alliance setup.");
            alliance = Alliance.BLUE;
        } else {
            // Red setup.
            RobotLog.i("506 Doing red alliance setup.");
            alliance = Alliance.RED;
        }
    }

    public void selectPosition(Position choice) {
        if (choice == Position.FAR) {
            position = Position.FAR;
        } else {
            position = Position.NEAR;
        }
    }

    private void setupParkPath() {

        if (alliance == Alliance.RED) {
            color = 1;
        } else {
            color = 0;
        }

        if (position == Position.NEAR) {
            distance = 2;
        } else {
            distance = 0;
        }

       /* if (side == Side.LEFT) {
            whichSide = 4;
        } else {
            whichSide = 0;
        } */

        combo = color + distance; // + whichSide;

        switch (combo) {
            case BLUE_FAR:
                RobotLog.i("506 Park Path: BLUE FAR");
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 30, Violet.STRAIGHT_SPEED * turnMultiplier);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 15, Violet.STRAIGHT_SPEED * turnMultiplier);
                break;
            case RED_FAR:
                RobotLog.i("506 Park Path: RED FAR");
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 30, Violet.STRAIGHT_SPEED);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 15, Violet.STRAIGHT_SPEED);
                break;
            case BLUE_NEAR:
                RobotLog.i("506 Park Path: BLUE NEAR");
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, Violet.STRAIGHT_SPEED * turnMultiplier);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, Violet.STRAIGHT_SPEED);

                break;
            case RED_NEAR:
                RobotLog.i("506 Park Path: RED NEAR");
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 25, Violet.STRAIGHT_SPEED);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, Violet.STRAIGHT_SPEED);
                break;
           /*
            case BLUE_FAR_LEFT:
                RobotLog.i("506 Case Blue Far Left");
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 50, Violet.STRAIGHT_SPEED * turnMultiplier);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 15, Violet.STRAIGHT_SPEED * turnMultiplier);
                break;
            case RED_FAR_LEFT:
                RobotLog.i("506 Case Red Far Left");
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, Violet.STRAIGHT_SPEED);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, Violet.STRAIGHT_SPEED * moveMultiplier);
                break;
            case BLUE_NEAR_LEFT:
                RobotLog.i("506 Case Blue Near Left");
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 50, Violet.STRAIGHT_SPEED * turnMultiplier);
                break;
            case RED_NEAR_LEFT:
                RobotLog.i("506 Case Red Near Left");
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, Violet.STRAIGHT_SPEED);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 4, Violet.STRAIGHT_SPEED * moveMultiplier);
                break;

                */
        }
    }

}

