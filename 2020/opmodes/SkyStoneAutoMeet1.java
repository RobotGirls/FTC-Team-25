package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;
import team25core.StoneDetectionTask;

@Autonomous(name = "AutoMeet2", group = "Team 25")
@Disabled
public class SkyStoneAutoMeet1 extends StandardFourMotorRobot {


    private final static String TAG = "STONEZ";

    //for mechanism
    private Servo grabberServo;
    private Servo foundationHookRightServo;
    private Servo foundationHookLeftServo;
    private final double DOWN_GRABBER_SERVO = (float) 1/ (float)256.0;
    private final double UP_GRABBER_SERVO = (float)80 / (float)256.0;
    private final double OPEN_FOUNDATION_HOOK_RIGHT_SERVO = (float)89 / (float)256.0;
    private final double OPEN_FOUNDATION_HOOK_LEFT_SERVO  = (float)10 / (float)256.0;
    private final double CLOSE_FOUNDATION_HOOK_RIGHT_SERVO  = (float)215/ (float)256.0;  //FIX ALL FOUNDATION SERVO
    private final double CLOSE_FOUNDATION_HOOK_LEFT_SERVO = (float)139 / (float)256.0;


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
    private Telemetry.Item pathTlm;
    private int numStonesSeen;
    private DeadReckonPath redDepotPath;
    private DeadReckonPath blueDepotPath;
    private DeadReckonPath depotPath;
    private DeadReckonPath foundationPath;
    private DeadReckonPath redFoundationPath;
    private DeadReckonPath blueFoundationPath;
    private DeadReckonPath bmoveAcross;
    private DeadReckonPath rmoveAcross;
    private DeadReckonPath moveAcross;
    private DeadReckonPath endRedFoundation;
    private DeadReckonPath endBlueFoundation;
    private DeadReckonPath endFoundationPath;
    private DeadReckonPath foundationUnderBridge;
    private DeadReckonPath redFoundationUnderBridge;
    private DeadReckonPath blueFoundationUnderBridge;
    private DeadReckonPath blueSkyStoneUnderBridge;
    private DeadReckonPath redSkyStoneUnderBridge;
    private DeadReckonPath skyStoneUnderBridge;




    private double confidence;
    private double left;
    private double type;
    private double imageMidpoint;
    private double stoneMidpoint;
    private double delta;
    private double margin = 50;
    private double setColor;
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
                    moveAcross = bmoveAcross;
                    endFoundationPath = endBlueFoundation;
                    foundationUnderBridge = blueFoundationUnderBridge;
                    skyStoneUnderBridge = blueSkyStoneUnderBridge;
                    break;
                case BUTTON_B_DOWN:
                    allianceColor = AllianceColor.RED;
                    allianceTlm.setValue("RED");
                    depotPath = redDepotPath;
                    foundationPath = redFoundationPath;
                    moveAcross = rmoveAcross;
                    endFoundationPath = endRedFoundation;
                    foundationUnderBridge = redFoundationUnderBridge;
                    skyStoneUnderBridge = redSkyStoneUnderBridge;
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

    public void moveUnderBridgeFromBuildSiteSkyStoneBuild()
    {
        RobotLog.i("move Under bridge after skyStone");


        //starts when you have stone and want to move
        this.addTask(new DeadReckonTask(this, skyStoneUnderBridge, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("under bridge from buildsite skystone");

                }
            }

        });
    }

    public void moveStonetoBuild()
    {
        RobotLog.i("Go Pick Up Skystone");


        //starts when you have stone and want to move
        this.addTask(new DeadReckonTask(this, moveAcross, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    grabberServo.setPosition(UP_GRABBER_SERVO);
                    RobotLog.i("Done with taking stone to build");
                    moveUnderBridgeFromBuildSiteSkyStoneBuild();

                }
            }

        });
    }

    public void goPickupSkystone(final DeadReckonPath depotPath)
    {
        //FIXME
        RobotLog.i("Go Pick Up Skystone");


            //starts when you find a skystone
        this.addTask(new DeadReckonTask(this, depotPath, drivetrain1){
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
    public void moveUnderBridgeFromBuildSiteFoundation()
    {
        RobotLog.i("move Under bridge from bu");


        //starts when you have stone and want to move
        this.addTask(new DeadReckonTask(this, foundationUnderBridge, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("under bridge from buildsite foundation");

                }
            }

        });
    }

    public void moveFoundationtoBuild()
    {
        //FIXME
        RobotLog.i("move foundation to build corner");


        this.addTask(new DeadReckonTask(this, endFoundationPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    foundationHookRightServo.setPosition(OPEN_FOUNDATION_HOOK_RIGHT_SERVO);
                    foundationHookLeftServo.setPosition(OPEN_FOUNDATION_HOOK_LEFT_SERVO);
                    RobotLog.i("moved foundation to build corner");
                    moveUnderBridgeFromBuildSiteFoundation();


                }
            }
        });
    }


    public void moveFoundation(final DeadReckonPath foundationPath)
    {
        //FIXME
        RobotLog.i("Move to Foundation");


        //starts when you find a skystone
        this.addTask(new DeadReckonTask(this, foundationPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    foundationHookRightServo.setPosition(CLOSE_FOUNDATION_HOOK_RIGHT_SERVO);
                    foundationHookLeftServo.setPosition(CLOSE_FOUNDATION_HOOK_LEFT_SERVO);
                    RobotLog.i("Done with foundation path to hook foundation");
                    moveFoundationtoBuild();
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
                pathTlm.setValue(setColor);

                numStonesSeen = event.stones.size();
                numStonesSeenTlm.setValue(numStonesSeen);
                RobotLog.ii(TAG,"numStonesSeen",numStonesSeen);

                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    if (Math.abs(imageMidpoint - stoneMidpoint) < margin) {
                        inCenter = true;
                        RobotLog.i("506 Found gold");
                        sdTask.stop();
                        drivetrain1.stop();

                        if (allianceColor == AllianceColor.RED) {
                            goPickupSkystone(redDepotPath);
                            RobotLog.i("506 chose red depot path");
                            pathTlm.setValue("taking red depot path");
                        } else {
                            goPickupSkystone(blueDepotPath);
                            RobotLog.i("506 chose blue depot path");
                            pathTlm.setValue("taking blue depot path" );
                        }
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

        endRedFoundation = new DeadReckonPath();
        endBlueFoundation = new DeadReckonPath();

        redFoundationUnderBridge = new DeadReckonPath();
        blueFoundationUnderBridge = new DeadReckonPath();

        blueSkyStoneUnderBridge = new DeadReckonPath();
        redSkyStoneUnderBridge = new DeadReckonPath();
        //add path to get to bridge

        blueDepotPath.stop();
        blueDepotPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,2, STRAIGHT_SPEED);  //left
        blueDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,7, -STRAIGHT_SPEED); //forward

        redDepotPath.stop();
        redDepotPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,3, STRAIGHT_SPEED); //left
        redDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,7, -STRAIGHT_SPEED); //forward

        bmoveAcross.stop();
        bmoveAcross.addSegment(DeadReckonPath.SegmentType.STRAIGHT,3 ,STRAIGHT_SPEED);
        bmoveAcross.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,14, STRAIGHT_SPEED);

        rmoveAcross.stop();
        rmoveAcross.addSegment(DeadReckonPath.SegmentType.STRAIGHT,3 ,STRAIGHT_SPEED);
        rmoveAcross.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,14, -STRAIGHT_SPEED);

        redFoundationPath.stop();
        redFoundationPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 6, STRAIGHT_SPEED);
        redFoundationPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, STRAIGHT_SPEED);

        endRedFoundation.stop();
        endRedFoundation.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, -STRAIGHT_SPEED);

        redFoundationUnderBridge.stop();
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 9, -STRAIGHT_SPEED);

        blueFoundationPath.stop();
        blueFoundationPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 6, -STRAIGHT_SPEED);
        blueFoundationPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, STRAIGHT_SPEED);

        endBlueFoundation.stop();
        endBlueFoundation.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, -STRAIGHT_SPEED);

        blueFoundationUnderBridge.stop();
        blueFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 7.5, STRAIGHT_SPEED);

        blueSkyStoneUnderBridge.stop();
        blueSkyStoneUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 3.4, -STRAIGHT_SPEED);

        redSkyStoneUnderBridge.stop();
        redSkyStoneUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 2.8, STRAIGHT_SPEED);






    }
    @Override
    public void init()
    {
        grabberServo = hardwareMap.servo.get("grabberServo");
        grabberServo.setPosition(UP_GRABBER_SERVO);

        foundationHookLeftServo = hardwareMap.servo.get("foundationHookLeftServo");
        foundationHookLeftServo.setPosition(OPEN_FOUNDATION_HOOK_LEFT_SERVO);

        foundationHookRightServo = hardwareMap.servo.get("foundationHookRightServo");
        foundationHookRightServo.setPosition(OPEN_FOUNDATION_HOOK_RIGHT_SERVO);

        //caption: what appears on the phone
        stonePositionTlm = telemetry.addData("LeftOrigin", "unknown");
        stoneConfidTlm = telemetry.addData("Confidence", "N/A");
        stoneTypeTlm = telemetry.addData("StoneType","unknown");
        imageMidpointTlm = telemetry.addData("Image_Mdpt", "unknown");
        stoneMidpointTlm = telemetry.addData("Stone Mdpt", "unknown");
        stoneTlm = telemetry.addData("kind", "unknown");
        deltaTlm = telemetry.addData("delta", "unknown");
        numStonesSeenTlm = telemetry.addData("numStones",-1);
        pathTlm = telemetry.addData("AllianceClr", "unknown");

        RobotLog.ii(TAG,  "delta: " + delta);


        drivetrain1 = new MechanumGearedDrivetrain(frontRight, backRight, frontLeft, backLeft);
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
        if (allianceColor == AllianceColor.RED) {
            drivetrain1.strafe(SkyStoneConstants25.STRAFE_SPEED);
        } else {
            drivetrain1.strafe(-SkyStoneConstants25.STRAFE_SPEED);
        }
        loggingTlm.setValue("startStrafing:after starting to strafe");
    }

    public void parkUnderBridge()
    {
        this.addTask(new DeadReckonTask(this, path, drivetrain1));
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
            moveFoundation(foundationPath);

        }
        else if (robotPosition == RobotPosition.DEPOT)
        {
            RobotLog.i("start: before startStrafing");
            loggingTlm.setValue("start:in DEPOT about to startStrafing");

            startStrafing();
        }

    }
}
