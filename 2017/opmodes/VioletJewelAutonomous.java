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
    // private ColorSensor color;
    // private ColorSensorTask senseColor;
    private ColorThiefTask colorThiefTask;
    private DeviceInterfaceModule cdim;
    private Alliance alliance;
    private Position position;
    private Side side;
    private DeadReckonPath park;
    private DeadReckonPath pushJewel;
    private DeadReckonTask task;
  //  private PersistentTelemetryTask ptt;
    private Telemetry.Item particle;
    private Telemetry.Item allianceItem;
    private Telemetry.Item positionItem;

    boolean flashOn = false;

   // boolean pollOn = false;




    private final int TICKS_PER_INCH = VioletEConstants.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = VioletEConstants.TICKS_PER_DEGREE;
    private int turnMultiplier = -1;
    private int moveMultiplier = -1;
    private int color = 0;
    private int distance = 0;
    private int whichSide = 0;
    private int combo = 0;


    // Combos.
    private static final int BLUE_FAR_RIGHT = 0;
    private static final int RED_FAR_RIGHT = 1;
    private static final int BLUE_NEAR_RIGHT = 2;
    private static final int RED_NEAR_RIGHT = 3;
    private static final int BLUE_FAR_LEFT = 4;
    private static final int RED_FAR_LEFT = 5;
    private static final int BLUE_NEAR_LEFT = 6;
    private static final int RED_NEAR_LEFT = 7;


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

        // ptt = new PersistentTelemetryTask(this);
        //  this.addTask(ptt);
        telemetry.setAutoClear(false);
        allianceItem = telemetry.addData("ALLIANCE", "Unselected (X/B)");
        positionItem = telemetry.addData("POSITION", "Unselected (Y/A");
        particle = telemetry.addData("Particle: ", "No data");


        // Path setup.
        pushJewel   = new DeadReckonPath();
        park        = new DeadReckonPath();

        jewel.setPosition(0.0);


        // Alliance and autonomous choice selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);


        sense();

    }

    @Override
    public void start()
    {

        this.addTask(new DeadReckonTask(this, pushJewel, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {

                   /* TODO: Put this back after we test jewel individually.

                   addTask(new DeadReckonTask(robot, park, drivetrain) {
                        @Override
                        public void handleEvent(RobotEvent e) {

                        }
                    });
                    */
                }
            }
        });
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            RobotLog.i("Jewel: Detected " + e.toString());
            particle.setValue(e.toString());

            switch (event.kind) {
                case BUTTON_X_DOWN:
                    selectAlliance(Alliance.BLUE);
                    allianceItem.setValue("ALLIANCE", "Blue");
                    break;
                case BUTTON_B_DOWN:
                    selectAlliance(Alliance.RED);
                    allianceItem.setValue("ALLIANCE", "Red");
                    break;
                case BUTTON_Y_DOWN:
                    selectPosition(Position.FAR);
                    positionItem.setValue("POSITION", "Far");
                    break;
                case BUTTON_A_DOWN:
                    selectPosition(Position.NEAR);
                    positionItem.setValue("POSITION", "Near");
                    break;
                case RIGHT_BUMPER_DOWN:
                    colorThiefTask.setPollingMode(ColorThiefTask.PollingMode.ON);
                    break;
                case RIGHT_TRIGGER_DOWN:
                    CameraDevice.getInstance().setFlashTorchMode(!flashOn);
                    flashOn = !flashOn;
                    break;
                default:
                    break;

            }
          //  telemetry.update();
        }
    }


   /* TODO: add toggle for poll on/off
    private void togglePolling() {


    }
    */





    private void sense() {
         colorThiefTask = new ColorThiefTask(this) {
            @Override
            public void handleEvent(RobotEvent e) {
                ColorThiefTask.ColorThiefEvent event = (ColorThiefEvent) e;
                particle.setValue(event.toString());

                jewel.setPosition(0.5);
                RobotLog.i("506 Arm deployed.");

                if (alliance == Alliance.RED) {
                    if (event.kind == EventKind.RED) {
                        RobotLog.i("506 Sensed RED");
                        pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -VioletEConstants.STRAIGHT_SPEED);
                        side = Side.LEFT;
                    } else {
                        RobotLog.i("506 Sensed BLUE");
                        pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, VioletEConstants.STRAIGHT_SPEED);
                        side = Side.RIGHT;
                    }
                } else {
                    if (event.kind == EventKind.BLUE) {
                        RobotLog.i("506 Sensed BLUE");
                        pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -VioletEConstants.STRAIGHT_SPEED);
                        side = Side.LEFT;
                    } else {
                        RobotLog.i("506 Sensed RED");
                        pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, VioletEConstants.STRAIGHT_SPEED);
                        side = Side.RIGHT;
                    }
                }

            }
        };
        addTask(colorThiefTask);
    }

    // Previously methods used to push jewel off right and left but now putting the task setup together so code looks cleaner!
 /*
    private void moveLeft()
    {
        this.addTask(new DeadReckonTask(this, pushJewel, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, -VioletEConstants.STRAIGHT_SPEED);
                side  = Side.LEFT;
                goPark();
            }
        });
    }

    private void moveRight()
    {
        this.addTask(new DeadReckonTask(this, pushJewel, drivetrain) {
            @Override
            public void handleEvent(RobotEvent e) {
                pushJewel.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, VioletEConstants.STRAIGHT_SPEED);
                side = Side.RIGHT;
                goPark();
            }
        });
    }

    */

    private void selectAlliance(Alliance color) {
        if (color == Alliance.BLUE) {
            // Blue setup.
            RobotLog.i("506 doing blue setup.");
            alliance = Alliance.BLUE;
        } else {
            // Red setup.
            RobotLog.i("506 doing red setup.");
            alliance = Alliance.RED;
        }
    }

    private void selectPosition(Position choice) {
        if (choice == Position.FAR) {
            position = Position.FAR;
        } else {
            position = Position.NEAR;
        }
    }

    private void goPark()
    {
        jewel.setPosition(0.5);

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

        if (side == Side.LEFT) {
            whichSide = 4;
        } else {
            whichSide = 0;
        }

        combo = color + distance + whichSide;

            switch(combo) {
                case BLUE_FAR_RIGHT:
                    park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 30, VioletEConstants.STRAIGHT_SPEED * turnMultiplier);
                    park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 15, VioletEConstants.STRAIGHT_SPEED * turnMultiplier);
                    break;
                case RED_FAR_RIGHT:
                    park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 30, VioletEConstants.STRAIGHT_SPEED);
                    park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 15, VioletEConstants.STRAIGHT_SPEED);
                    break;
                case BLUE_NEAR_RIGHT:
                    park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, VioletEConstants.STRAIGHT_SPEED * turnMultiplier);
                    break;
                case RED_NEAR_RIGHT:
                    park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 15, VioletEConstants.STRAIGHT_SPEED);
                    break;
                case BLUE_FAR_LEFT:
                    park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 50, VioletEConstants.STRAIGHT_SPEED * turnMultiplier);
                    park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 15, VioletEConstants.STRAIGHT_SPEED * turnMultiplier);
                    break;
                case RED_FAR_LEFT:
                    park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 50, VioletEConstants.STRAIGHT_SPEED);
                    park.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 15, VioletEConstants.STRAIGHT_SPEED * turnMultiplier);
                    break;
                case BLUE_NEAR_LEFT:
                    park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 50, VioletEConstants.STRAIGHT_SPEED * turnMultiplier);
                    break;
                case RED_NEAR_LEFT:
                    park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 50, VioletEConstants.STRAIGHT_SPEED);
                    break;
            }
    }


    // Does the same as above.

       /*
       if (alliance == Alliance.RED) {
           if (position == Position.NEAR) {
               if (lr == LeftRight.LEFT) {
                  park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, VioletEConstants.STRAIGHT_SPEED);
               } else {
                   park.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, VioletEConstants.STRAIGHT_SPEED);
               }
           } else {
               if (lr == LeftRight.LEFT) {
                   // LEFT DEAD RECKON METHOD
               } else {
                   // RIGHT DEAD RECKON METHOD
               }
           }
       } else {
           if (position == Position.NEAR) {
               if (lr == LeftRight.LEFT) {
                   // LEFT DEAD RECKON METHOD
               } else {
                   // RIGHT DEAD RECKON METHOD
               }
           } else {
               if (lr == LeftRight.LEFT) {
                   // LEFT DEAD RECKON METHOD
               } else {
                   // RIGHT DEAD RECKON METHOD
               }
           }

       }
        */

}

