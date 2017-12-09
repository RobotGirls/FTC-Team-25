package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.CameraDevice;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

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

    private static final int TURN_MULTIPLIER = -1;
    private int distance = 0;
    private int whichSide = 0;
    private int combo = 0;
    private int color = 0;
    private int liftJewel = 0;
    private int isBlack = 0;



    // Park combos.
    private static final int BLUE_FAR = 0;
    private static final int RED_FAR = 1;
    private static final int BLUE_NEAR = 2;
    private static final int RED_NEAR = 3;




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

        // Path setup.
        park        = new DeadReckonPath();

        // Arm initialized up
        jewel.setPosition(0.56);    // 145/256

        // Single shot timer tasks for delays.
        stt = new SingleShotTimerTask(this, 1500);          // Delay resetting arm position
        moveDelay = new SingleShotTimerTask(this, 500);     // Delay moving after setting arm down.

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);

        drivetrain.setNoncanonicalMotorDirection();

        sense();


    }

 /*   public void parkPathChoice()
    {
        if (alliance == Alliance.RED) {
            color = 1;
        } else if (alliance == Alliance.BLUE) {
            color = 0;
        }

        if (position == Position.NEAR) {
            distance = 2;
        } else {
            distance = 0;
        }

        combo = color + distance; // + whichSide;

    } */


    @Override
    public void start()
    {

        // Arm goes down
        jewel.setPosition(0.05);
        // 15/256
        addTask(new SingleShotTimerTask(this, 500) {
                    @Override
                    public void handleEvent(RobotEvent e) {
                        robot.addTask(new DeadReckonTask(robot, pushJewel, drivetrain) {
                            @Override
                            public void handleEvent(RobotEvent e) {
                                DeadReckonEvent path = (DeadReckonEvent) e;
                                if (path.kind == EventKind.PATH_DONE) {

                                   /* if (liftJewel == 1) {
                                        jewel.setPosition(0.56);
                                        RobotLog.i("506 Jewel arm reset");
                                    }
                                    */

                                    addTask(new DeadReckonTask(robot, park, drivetrain) {
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
                                jewel.setPosition(0.56);
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
                default:
                    break;
            }
        } setupParkPath();
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

    private void sense()
    {
         colorThiefTask = new ColorThiefTask(this, VuforiaLocalizer.CameraDirection.FRONT) {
            @Override
            public void handleEvent(RobotEvent e) {
                ColorThiefTask.ColorThiefEvent event = (ColorThiefEvent) e;
                particle.setValue(event.toString());
                pushJewel = new DeadReckonPath();

                if (event.kind == EventKind.BLACK) {
                    isBlack = 1;
                } else {
                    if (alliance == Alliance.RED) {
                        if (event.kind == EventKind.RED) {
                            pushJewel.stop();
                            RobotLog.i("506 Sensed RED");
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, Violet.TURN_SPEED);
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, Violet.TURN_SPEED * TURN_MULTIPLIER);
                            // FIXME: Need to add a delay to the last jewel segment because it keeps on pushing the jewel off.
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 11, Violet.STRAIGHT_SPEED);
                            liftJewel = 1;
                        } else {
                            RobotLog.i("506 Sensed BLUE");
                            pushJewel.stop();
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7, Violet.STRAIGHT_SPEED);
                            liftJewel = 1;
                        }
                    } else if (alliance == Alliance.BLUE) {
                        if (event.kind == EventKind.BLUE) {
                            RobotLog.i("506 Sensed BLUE");
                            pushJewel.stop();
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                            liftJewel = 1;
                        } else {
                            RobotLog.i("506 Sensed RED");
                            pushJewel.stop();
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, Violet.TURN_SPEED * TURN_MULTIPLIER);
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, Violet.TURN_SPEED);
                            liftJewel = 1;
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 11, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                        }
                    }
                }
            }
        };

        addTask(colorThiefTask);
       // setupParkPath();
    }

    private void selectAlliance(Alliance color) {
        if (color == Alliance.BLUE) {
            // Blue setup.
            RobotLog.i("506 Alliance: BLUE");
            alliance = Alliance.BLUE;
        } else {
            // Red setup.
            RobotLog.i("506 Alliance: RED");
            alliance = Alliance.RED;
        }
    }

    public void selectPosition(Position choice) {
        if (choice == Position.FAR) {
            position = Position.FAR;
            RobotLog.i("506 Position: FAR");
        } else {
            position = Position.NEAR;
            RobotLog.i("506 Position: NEAR");
        }
    }

    private void setupParkPath()
    {


        if (alliance == Alliance.RED) {
            color = 1;
        } else if (alliance == Alliance.BLUE) {
            color = 0;
        }

        if (position == Position.NEAR) {
            distance = 2;
        } else {
            distance = 0;
        }

        combo = color + distance;

         // + whichSide;

        switch (combo) {
            case BLUE_FAR:
                RobotLog.i("506 Case: BLUE_FAR");
                park.stop();
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 27, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 10, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                break;
            case RED_FAR:
                RobotLog.i("506 Case: RED_FAR");
                park.stop();
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 27, Violet.STRAIGHT_SPEED);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 10 , Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                break;
            case BLUE_NEAR:
                RobotLog.i("506 Case: BLUE_NEAR");
                park.stop();
                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 18, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 7, Violet.STRAIGHT_SPEED);
                break;
            case RED_NEAR:
                RobotLog.i("506 Case: RED_NEAR");
                park.stop();

                // TODO: make the straight and side segments further

                park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 20, Violet.STRAIGHT_SPEED);
                park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 9, Violet.STRAIGHT_SPEED);
                break;
            default:
                break;
        }
    }
}

