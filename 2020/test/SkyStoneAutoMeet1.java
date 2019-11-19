package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.StoneDetectionTask;

@Autonomous(name = "AutoMeet1", group = "Team 25")
public class SkyStoneAutoMeet1 extends Robot {


    private final static String TAG = "Margarita";

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    //for mechanism
    private Servo grabberServo;
    private final double DOWN_GRABBER_SERVO = (float) 115/ (float)256.0;
    private final double UP_GRABBER_SERVO = (float)160 / (float)256.0;

    private MechanumGearedDrivetrain drivetrain1;

    private Telemetry.Item stonePositionTlm;
    private Telemetry.Item stoneTlm;
    private Telemetry.Item stoneConfidTlm;
    private Telemetry.Item stoneTypeTlm;
    private Telemetry.Item stoneMidpointTlm;
    private Telemetry.Item imageMidpointTlm;
    private Telemetry.Item loggingTlm;
    private Telemetry.Item handleEvntTlm;
    private Telemetry.Item deltaTlm;
    private Telemetry.Item numStonesSeenTlm;
    private int numStonesSeen;
    private DeadReckonPath redDepotPath;
    private DeadReckonPath blueDepotPath;
    private DeadReckonPath depotPath;
    private DeadReckonPath foundationPath;
    private DeadReckonPath redFoundationPath;
    private DeadReckonPath blueFoundationPath;
    private DeadReckonPath bmoveAcross;
    private DeadReckonPath rmoveAcross;


    private double confidence;
    private double left;
    private double type;
    private double imageMidpoint;
    private double stoneMidpoint;
    private double delta;
    private double margin = 50;
    private boolean inCenter;
    private String stoneType;
    private StoneDetectionTask.EventKind stoneKind;
    private final double STRAIGHT_SPEED = 0.5;
    private final double TURN_SPEED = 1;

    DeadReckonPath path = new DeadReckonPath();

    StoneDetectionTask sdTask;

    private enum AllianceColor {
        BLUE, // Button X
        RED, // Button B
        DEFAULT
    }

    private enum RobotPosition {
        BUILD_SITE, // Button Y
        DEPOT, // Button A
        DEFAULT
    }

    // declaring gamepad variables
    //variables declarations have lowercase then uppercase
    private GamepadTask gamepad;
    protected AllianceColor allianceColor;
    protected RobotPosition robotPosition;

    //declaring telemetry item
    private Telemetry.Item allianceTlm;
    private Telemetry.Item positionTlm;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
        //decide what alliance and position of robot
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;
            switch (event.kind) {
                case BUTTON_X_DOWN:
                    allianceColor = AllianceColor.BLUE;
                    allianceTlm.setValue("BLUE");
                    depotPath = blueDepotPath;
                    foundationPath = blueFoundationPath;
                    break;
                case BUTTON_B_DOWN:
                    allianceColor = AllianceColor.RED;
                    allianceTlm.setValue("RED");
                    depotPath = redDepotPath;
                    foundationPath = redFoundationPath;
                    break;
                case BUTTON_Y_DOWN:
                    robotPosition = RobotPosition.BUILD_SITE;
                    positionTlm.setValue("BUILD SITE");
                    break;
                case BUTTON_A_DOWN:
                    robotPosition = RobotPosition.DEPOT;
                    positionTlm.setValue("DEPOT");
                    break;

            }
        }
    }

    public void moveStonetoBuild()
    {
        RobotLog.i("Go Pick Up Skystone");


        //starts when you have stone and want to move
        this.addTask(new DeadReckonTask(this, bmoveAcross, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    grabberServo.setPosition(UP_GRABBER_SERVO);
                    RobotLog.i("Done with path");

                }
            }

        });
    }

    public void goPickupSkystone()
    {
        //FIXME
        RobotLog.i("Go Pick Up Skystone");


            //starts when you find a skystone
        this.addTask(new DeadReckonTask(this, blueDepotPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    grabberServo.setPosition(DOWN_GRABBER_SERVO);
                    RobotLog.i("Done with path");
                    moveStonetoBuild();
                }
            }
        });
    }

    public void setStoneDetection()
    {

        sdTask = new StoneDetectionTask(this, "Webcam1") {
            //starts when you find a skystone
            @Override
            public void handleEvent(RobotEvent e) {
                StoneDetectionEvent event = (StoneDetectionEvent) e;
                //0 gives you the first stone on list of stones
                confidence = event.stones.get(0).getConfidence();
                left = event.stones.get(0).getLeft();

                RobotLog.ii(TAG, "Saw: " + event.kind + " Confidence: " + confidence);
                RobotLog.i("startHandleEvent");

                imageMidpoint = event.stones.get(0).getImageWidth() / 2.0;
                stoneMidpoint = (event.stones.get(0).getWidth() / 2.0) + left;
                stoneType = event.stones.get(0).getLabel();
                stoneKind = event.kind;
                delta = Math.abs(imageMidpoint - stoneMidpoint);
                RobotLog.i("type");

                stonePositionTlm.setValue(left);
                stoneConfidTlm.setValue(confidence);
                imageMidpointTlm.setValue(imageMidpoint);
                stoneMidpointTlm.setValue(stoneMidpoint);
                stoneTypeTlm.setValue(stoneType);
                stoneTlm.setValue(stoneKind);
                deltaTlm.setValue(delta);

                numStonesSeen = event.stones.size();
                numStonesSeenTlm.setValue(numStonesSeen);
                RobotLog.ii(TAG,"numStonesSeen",numStonesSeen);

                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    if (Math.abs(imageMidpoint - stoneMidpoint) < margin) {
                        inCenter = true;
                        RobotLog.i("506 Found gold");
                        sdTask.stop();
                        drivetrain1.stop();
                        goPickupSkystone();
                    }
                }
            }
        };

        sdTask.init(telemetry, hardwareMap);
        //later adbwill find skystone
        sdTask.setDetectionKind(StoneDetectionTask.DetectionKind.LARGEST_SKY_STONE_DETECTED);

    }


    public void loop()
    {
        super.loop();

    }
    public void initPath()
    {
        blueDepotPath = new DeadReckonPath();
        redDepotPath = new DeadReckonPath();
        redFoundationPath = new DeadReckonPath();
        blueFoundationPath = new DeadReckonPath();
        bmoveAcross = new DeadReckonPath();
        rmoveAcross = new DeadReckonPath();

        blueDepotPath.stop();
        blueDepotPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,3.4, STRAIGHT_SPEED);  //3forprogramming
        blueDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,7, -STRAIGHT_SPEED); //5.75for programming

        redDepotPath.stop();
        redDepotPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,3.4, -STRAIGHT_SPEED);
        redDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,7, STRAIGHT_SPEED);

        bmoveAcross.stop();
        bmoveAcross.addSegment(DeadReckonPath.SegmentType.STRAIGHT,3 ,STRAIGHT_SPEED);
        bmoveAcross.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,14, -STRAIGHT_SPEED);

        rmoveAcross.stop();
        rmoveAcross.addSegment(DeadReckonPath.SegmentType.STRAIGHT,3 ,STRAIGHT_SPEED); //FIXME
        rmoveAcross.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,14, STRAIGHT_SPEED); //FIXME





    }
    @Override
    public void init()
    {

        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");
        grabberServo = hardwareMap.servo.get("grabberServo");

        grabberServo.setPosition(UP_GRABBER_SERVO);

        //caption: what appears on the phone
        stonePositionTlm = telemetry.addData("LeftOrigin", "unknown");
        stoneConfidTlm = telemetry.addData("Confidence", "N/A");
        stoneTypeTlm = telemetry.addData("StoneType","unknown");
        imageMidpointTlm = telemetry.addData("Image_Mdpt", "unknown");
        stoneMidpointTlm = telemetry.addData("Stone Mdpt", "unknown");
        stoneTlm = telemetry.addData("kind", "unknown");
        deltaTlm = telemetry.addData("delta", "unknown");
        numStonesSeenTlm = telemetry.addData("numStones",-1);

        RobotLog.ii(TAG,  "delta: " + delta);


        drivetrain1 = new MechanumGearedDrivetrain(360, frontRight, rearRight, frontLeft, rearLeft);
        drivetrain1.resetEncoders();
        drivetrain1.encodersOn();
        RobotLog.i("start moving");

        //initializing gamepad variables
        allianceColor = allianceColor.DEFAULT;
        gamepad = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1);
        addTask(gamepad);

        //telemetry setup
        telemetry.setAutoClear(false);
        allianceTlm = telemetry.addData("ALLIANCE", "Unselected (X-blue /B-red)");
        positionTlm = telemetry.addData("POSITION", "Unselected (Y-build/A-depot)");

        initPath();

        setStoneDetection();
    }

    public void startStrafing()
    {
        //start looking for Skystones
        RobotLog.i("startStrafing");
        addTask(sdTask);
        loggingTlm.setValue("startStrafing:before starting to strafe");
        drivetrain1.strafe(SkyStoneConstants25.STRAFE_SPEED);
        loggingTlm.setValue("startStrafing:after starting to strafe");
    }

    public void parkUnderBridge()
    {
        this.addTask(new DeadReckonTask(this, path, drivetrain1));
    }

    public void moveFoundation()
    {
        this.addTask(new DeadReckonTask(this, foundationPath, drivetrain1));
    }
    @Override
    public void start()
    {


        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 2.5, -1.0);



        loggingTlm = telemetry.addData("log", "unknown");
        handleEvntTlm = telemetry.addData("detecting","unknown");

        /**
         * Alternatively, this could be an anonymous class declaration that implements
         * handleEvent() for task specific event handlers.
         */

        //parkUnderBridge();

        if (robotPosition == RobotPosition.BUILD_SITE)
        {
           //parkUnderBridge();
            moveFoundation();

        }
        else if (robotPosition == RobotPosition.DEPOT)
        {
            RobotLog.i("start: before startStrafing");
            loggingTlm.setValue("start:in DEPOT about to startStrafing");

            startStrafing();
        }

    }
}
