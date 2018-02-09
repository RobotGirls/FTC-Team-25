package opmodes;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;
import com.vuforia.CameraDevice;
import com.vuforia.VuMarkTarget;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;

import team25core.ColorThiefTask;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.IMUSensorCriteria;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;
import team25core.VuMarkIdentificationTask;
import team25core.VuforiaBase;

/*
 * FTC Team 25: Created by Elizabeth Wu on 10/28/17.
 */

@Autonomous(name = "Violet Autonomous", group = "Team 25")
//@Disabled
public class VioletJewelAutonomous extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor rotate;
    private DcMotor linear;
    private Servo jewel;
    private Servo s3bottom;
    private Servo s4bottom;

    private ColorThiefTask colorThiefTask;
    private DeviceInterfaceModule cdim;
    private Alliance alliance;
    private Position position;
    //  private Side side;
    private DeadReckonPath park;
    private DeadReckonPath pushJewel;
    private DeadReckonPath backUp;
    private DeadReckonTask task;
    private SingleShotTimerTask stt;
    private SingleShotTimerTask moveDelay;
    private GlyphAutonomousPathUtility utility;
    private GlyphAutonomousPathUtility.StartStone stonePosition;
    private GlyphAutonomousPathUtility.TargetColumn targetColumn;
    private RelicRecoveryVuMark vuMark;
    private VuMarkIdentificationTask vmIdTask;
    private VuforiaBase vuforiaBase;

    private GlyphAutonomousPathUtility.TargetColumn tgtColumn = GlyphAutonomousPathUtility.TargetColumn.LEFT;


    private Telemetry.Item particle;
    private Telemetry.Item allianceItem;
    private Telemetry.Item positionItem;
    private Telemetry.Item imuStatus;
    private Telemetry.Item pollItem;
    private Telemetry.Item flashItem;
    // private Telemetry.Item vuMarkItem;

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
    private static final int RED_NEAR = 0;
    private static final int RED_FAR = 1;
    private static final int BLUE_NEAR = 2;
    private static final int BLUE_FAR = 3;

    private IMUSensorCriteria imuSensorCriteria;
    BNO055IMU imu;
    Orientation angles;
    Acceleration gravity;

    private FourWheelDirectDrivetrain drivetrain;

    public enum Alliance {
        RED,
        BLUE,
    }

    public enum Position {
        NEAR,
        FAR,
    }

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }


   /* public enum Side {
        LEFT,
        RIGHT,
    } */

    @Override
    public void init() {
        telemetry.setAutoClear(false);

        // Hardware mapping.
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");
        linear = hardwareMap.dcMotor.get("linear");
        jewel = hardwareMap.servo.get("jewel");
        s3bottom = hardwareMap.servo.get("s3");
        s4bottom = hardwareMap.servo.get("s4");
        imu = hardwareMap.get(BNO055IMU.class, "imu");


        // Telemetry setup.
        telemetry.setAutoClear(false);
        allianceItem = telemetry.addData("ALLIANCE", "Unselected (X/B)");
        positionItem = telemetry.addData("POSITION", "Unselected (Y/A)");
        particle = telemetry.addData("Particle: ", "No data");
        imuStatus = telemetry.addData("IMU: ", "All Good");
        //vuMarkItem      = telemetry.addData("VuMark: ", "No data");

        // Path setup.
        backUp = new DeadReckonPath();

        // Arm initialized up
        jewel.setPosition(VioletConstants.JEWEL_UP);

        // Single shot timer tasks for delays.
        stt = new SingleShotTimerTask(this, 1500);          // Delay resetting arm position
        moveDelay = new SingleShotTimerTask(this, 500);     // Delay moving after setting arm down.

        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        // Initialize IMU sensor criteria.
        imuSensorCriteria = new IMUSensorCriteria(imu, VioletConstants.MAX_TILT);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.setNoncanonicalMotorDirection();

        vuforiaBase = new VuforiaBase();
        vuforiaBase.setCameraDirection(VuforiaLocalizer.CameraDirection.FRONT);
        vuforiaBase.init(this);

        utility = new GlyphAutonomousPathUtility();

        // Setting stone position.
        // getStonePosition();
        //RobotLog.i("506 Stone Position is", stonePosition.toString());

        sense();
        detectVuMark(this);


    }

    protected void dispenseGlyph()
    {
        moveClaw(Direction.CLOCKWISE);
        addTask(new SingleShotTimerTask(this, 1000) {
            @Override
            // This handleEvent occurs after half a second passes to lower glyph mechanism.
            public void handleEvent(RobotEvent e) {
                // Open bottom claws
                s3bottom.setPosition(VioletConstants.S3_OPEN);
                s4bottom.setPosition(VioletConstants.S4_OPEN);
                backUp.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 6, -VioletConstants.STRAIGHT_SPEED);
                backUp.addSegment(DeadReckonPath.SegmentType.TURN, 210, VioletConstants.TURN_SPEED);
                backUp.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 6, -VioletConstants.STRAIGHT_SPEED);
                robot.addTask(new DeadReckonTask(robot, backUp, drivetrain, imuSensorCriteria));
            }
        });
    }

    protected void runGlyphPath()
    {
        park = utility.getPath(tgtColumn, stonePosition);
        RobotLog.i("506 start: after utility.getPath");
        this.addTask(new DeadReckonTask(this, park, drivetrain, imuSensorCriteria) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent event = (DeadReckonEvent) e;
                if (event.kind == EventKind.PATH_DONE) {
                    dispenseGlyph();
                } else if (event.kind == EventKind.SENSOR_SATISFIED) {
                    imuStatus.setValue("Catastrophe Averted!!!!!!!");
                }
            }  // end of handleEvent for park
        });  // end of adding park task
    }

    public void start()
    {
        // Put jewel arm down
        jewel.setPosition(VioletConstants.JEWEL_DOWN);
        // Close bottom claws to grab glyph
        s3bottom.setPosition(VioletConstants.S3_CLOSED);
        s4bottom.setPosition(VioletConstants.S4_CLOSED);


        addTask(new SingleShotTimerTask(this, 500) {
            @Override
            // This handleEvent occurs after half a second passes to close the bottom claws of the glyph mechanism.
            public void handleEvent(RobotEvent e) {
                // Lift glyph mechanism up to gain clearance before driving off balancing stone
                moveClaw(Direction.COUNTERCLOCKWISE);
            }
        });

        addTask(new SingleShotTimerTask(this, 1000) {
                @Override

                // This handleEvent occurs after one second passes to lower the arm.
                public void handleEvent(RobotEvent e) {
                    RobotLog.i("506 SST running");
                    robot.addTask(new DeadReckonTask(robot, pushJewel, drivetrain, imuSensorCriteria) {
                        // This handleEvent occurs after the pushJewel runs.
                        @Override
                        public void handleEvent(RobotEvent e) {
                            RobotLog.i("506 inside pushJewel handleEvent");
                            DeadReckonEvent event = (DeadReckonEvent) e;
                            if (event.kind == EventKind.PATH_DONE) {
                                runGlyphPath();
                            } else if (event.kind == EventKind.SENSOR_SATISFIED) {
                                imuStatus.setValue("Catastrophe Averted!!!!!!!");
                            }
                        } // end handleEvent when pushJewel path is done
                    });  // end add pushJewel task
                    robot.addTask(new SingleShotTimerTask(robot, 500) {
                        @Override
                        public void handleEvent(RobotEvent e) {
                            jewel.setPosition(VioletConstants.JEWEL_UP);
                        }
                    });  // end SingleShotTimerTask
                } // handleEvent for 1 sec SST
        }); // 1 sec SST

    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;
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
                    getStonePosition();
                    RobotLog.i("506 Stone Position is " + stonePosition.toString());
                    togglePolling();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Move claw up or down. Uses a 60 motor.
     */
    private void moveClaw(Direction direction)
    {
        if (direction == Direction.CLOCKWISE)                       // down
            linear.setDirection(DcMotorSimple.Direction.REVERSE);
        else                                                        // up
            linear.setDirection(DcMotorSimple.Direction.FORWARD);

        this.addTask(new RunToEncoderValueTask(this, linear, 1550, VioletConstants.CLAW_VERTICAL_POWER));
    }

    private void togglePolling() {
        if (pollOn == false) {
            RobotLog.w("506 togglePolling. pollOn false, turn poll on");
            colorThiefTask.setPollingMode(ColorThiefTask.PollingMode.ON);
            RobotLog.w("506 togglePolling. After color thief");
            vmIdTask.setPollingMode(VuMarkIdentificationTask.PollingMode.ON);
            pollOn = true;

        } else {
            RobotLog.w("506 togglePolling. pollOn true, turn poll off");
            colorThiefTask.setPollingMode(ColorThiefTask.PollingMode.OFF);
            RobotLog.w("506 togglePolling. After color thief");
            vmIdTask.setPollingMode(VuMarkIdentificationTask.PollingMode.OFF);
            pollOn = false;
        }
    }


    private void sense()
    {
        colorThiefTask = new ColorThiefTask(this, vuforiaBase) {
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
                            RobotLog.i("506 Sensed RED, Alliance RED");
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 6, VioletConstants.TURN_SPEED);
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 6, VioletConstants.TURN_SPEED * TURN_MULTIPLIER);
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 0.5, Violet.STRAIGHT_SPEED);
                            liftJewel = 1;
                        } else {
                            RobotLog.i("506 Sensed BLUE, Alliance RED");
                            pushJewel.stop();
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 0.5, Violet.STRAIGHT_SPEED);
                            liftJewel = 1;
                        }
                    } else if (alliance == Alliance.BLUE) {
                        if (event.kind == EventKind.BLUE) {
                            RobotLog.i("506 Sensed BLUE, Alliance BLUE");
                            pushJewel.stop();
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 0.5, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                            liftJewel = 1;
                        } else {
                            RobotLog.i("506 Sensed RED, Alliance BLUE");
                            pushJewel.stop();
                            //pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, VioletConstants.TURN_SPEED * TURN_MULTIPLIER);
                            //pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 30, VioletConstants.TURN_SPEED);
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 5, VioletConstants.TURN_SPEED * TURN_MULTIPLIER);
                            pushJewel.addSegment(DeadReckonPath.SegmentType.TURN, 5, VioletConstants.TURN_SPEED);
                            liftJewel = 1;
                            //pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 11, VioletConstants.STRAIGHT_SPEED * TURN_MULTIPLIER);
                            pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 0.5, Violet.STRAIGHT_SPEED * TURN_MULTIPLIER);
                        }
                    }
                }
            }
        };
        addTask(colorThiefTask);
    }

    private GlyphAutonomousPathUtility.TargetColumn detectVuMark(Robot robot)
    {
        RobotLog.i("506 added VuMark ID task");
        //robot.addTask(new VuMarkIdentificationTask(robot, vuforiaBase) {
        robot.addTask(vmIdTask = new VuMarkIdentificationTask(robot, vuforiaBase) {
            @Override
            public void handleEvent(RobotEvent e) {
                VuMarkIdentificationTask.VuMarkIdentificationEvent position = (VuMarkIdentificationTask.VuMarkIdentificationEvent) e;
                switch (position.kind) {
                    case CENTER:
                        tgtColumn = GlyphAutonomousPathUtility.TargetColumn.CENTER;
                        break;
                    case LEFT:
                        tgtColumn = GlyphAutonomousPathUtility.TargetColumn.LEFT;
                        break;
                    case RIGHT:
                        tgtColumn = GlyphAutonomousPathUtility.TargetColumn.RIGHT;
                        break;
                    default:
                       // RobotLog.i("506 Detect VuMark invalid position kind:", position.kind);
                        break;
                }
            }
        });
        return tgtColumn;
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

    private GlyphAutonomousPathUtility.StartStone getStonePosition()
    {
        if (alliance == Alliance.RED) {
            color = 0;
        } else if (alliance == Alliance.BLUE) {
            color = 2;
        }

        if (position == Position.NEAR) {
            distance = 0;
        } else {
            distance = 1;
        }

        combo = color + distance;

        switch (combo) {
            case BLUE_FAR:
                stonePosition = GlyphAutonomousPathUtility.StartStone.BLUE_FAR;
                RobotLog.i("506 Stone Position: BLUE_FAR");
                break;
            case RED_FAR:
                stonePosition = GlyphAutonomousPathUtility.StartStone.RED_FAR;
                RobotLog.i("506 Stone Position: RED_FAR");
                break;
            case BLUE_NEAR:
                stonePosition = GlyphAutonomousPathUtility.StartStone.BLUE_NEAR;
                RobotLog.i("506 Stone Position: BLUE_NEAR");
                break;
            case RED_NEAR:
                stonePosition = GlyphAutonomousPathUtility.StartStone.RED_NEAR;
                RobotLog.i("506 Stone Position: RED_NEAR");
                break;
            default:
                break;

        }
        return stonePosition;
    }
}

