package opmodes;

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

@Autonomous(name = "AutoILT_OneStone", group = "Team 25")
public class SkyStoneAuto_OneSkystone extends Robot {


    private final static String TAG = "STONEZ";

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    //for mechanism
    private Servo grabberServo;
    private Servo foundationHookRightServo;
    private Servo foundationHookLeftServo;

    private final double DOWN_GRABBER_SERVO = (float) 0/ (float)256.0;
    private final double MID_GRABBER_SERVO = (float)  50/ (float)256.0;
    private final double UP_GRABBER_SERVO = (float) 210/ (float)256.0;
    private final double OPEN_FOUNDATION_HOOK_RIGHT_SERVO = (float)216 / (float)256.0;
    private final double OPEN_FOUNDATION_HOOK_LEFT_SERVO  = (float)113 / (float)256.0;
    private final double CLOSE_FOUNDATION_HOOK_RIGHT_SERVO  = (float)91/ (float)256.0;  //FIX ALL FOUNDATION SERVO
    private final double CLOSE_FOUNDATION_HOOK_LEFT_SERVO = (float)238/ (float)256.0;

    //private final double NUM_PIXELS_PER_INCH = 10;  //10 original 63
    private final int STONE_LENGTH_IN_INCHES = 8; //14in

    //private final float HALF_STONE_LENGTH_IN_PIXELS = Math.round(STONE_LENGTH_IN_INCHES/2 * NUM_PIXELS_PER_INCH); //FINDING THE MIDDLE OF THE ROBOT AND WHEN THE SKYSTONE LINES UP WITH THE MIDDLE OF
    //private final double PIXELS_FROM_IMG_MIDPT_TO_LEFT_STONE = 15 * NUM_PIXELS_PER_INCH - HALF_STONE_LENGTH_IN_PIXELS ;

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
    private Telemetry.Item widthTlm;
    private Telemetry.Item marginTlm;
    private Telemetry.Item imageWidthTlm;
    private Telemetry.Item pixelsPerInchTlm;
    private Telemetry.Item distanceBtWWebcamAndGrabberTlm;

    private int numStonesSeen;
    private double numPixelsBtwImgMidptAndStoneMidpt;

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
    private DeadReckonPath getCloserPath;


    private double confidence;
    private double left;
    private double type;
    private double imageMidpoint;
    private double stoneMidpoint;
    private double delta;
    private double margin = 100;
    private double setColor;
    private double width;
    private int imageWidth;
    private boolean inCenter;
    private double realNumPixelsPerInch;
    private final int DISTANCE_FROM_WEBCAM_TO_GRABBER =1;
    private double distance;

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
                    grabberServo.setPosition(MID_GRABBER_SERVO);
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
        RobotLog.i("move Under bridge from build");


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

    public void getCloserPath()
    {
        //FIXME
        RobotLog.i("Move to Foundation");


        //starts when you find a skystone
        this.addTask(new DeadReckonTask(this, getCloserPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("move towards skyStone");
                    startStrafing();
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
                width = event.stones.get(0).getWidth();
                imageWidth = event.stones.get(0).getImageWidth();

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
                widthTlm.setValue(width);
                imageWidthTlm.setValue(imageWidth);

                numStonesSeen = event.stones.size();
                numStonesSeenTlm.setValue(numStonesSeen);
                RobotLog.ii(TAG,"numStonesSeen",numStonesSeen);

                if (event.kind == EventKind.OBJECTS_DETECTED) {

                    numPixelsBtwImgMidptAndStoneMidpt = stoneMidpoint - imageMidpoint;
                    realNumPixelsPerInch = (width/8.0);
                    pixelsPerInchTlm.setValue(realNumPixelsPerInch);
                    distance = (double)(DISTANCE_FROM_WEBCAM_TO_GRABBER * realNumPixelsPerInch);
                    distanceBtWWebcamAndGrabberTlm.setValue(distance);

                    marginTlm.setValue(Math.abs(numPixelsBtwImgMidptAndStoneMidpt - distance));
                    if ((numPixelsBtwImgMidptAndStoneMidpt > 0)  &&
                            (Math.abs(numPixelsBtwImgMidptAndStoneMidpt - distance)  < margin)) {
                    /* old detection if (Math.abs(imageMidpoint - stoneMidpoint) < margin) {
                        inCenter = true
                        RobotLog.i("506 Found gold");
                        sdTask.stop();
                        drivetrain1.stop();*/

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

        getCloserPath = new DeadReckonPath();
        //add path to get to bridge

        blueDepotPath.stop();
        blueDepotPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,0.2, -STRAIGHT_SPEED);  //
        blueDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,1.6, -0.4); //

        redDepotPath.stop();
        redDepotPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,0.1, STRAIGHT_SPEED);  //might change to .2 //original 1.2
        //redDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,1.5, -0.4);  //2
        redDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,1.5, -0.4);  //1.2

        bmoveAcross.stop();
        bmoveAcross.addSegment(DeadReckonPath.SegmentType.STRAIGHT,4.5 ,.2);
        bmoveAcross.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,15.5, .4);

        rmoveAcross.stop();
        rmoveAcross.addSegment(DeadReckonPath.SegmentType.STRAIGHT,4.5  ,.3); //STRAIGHT_SPEED
        rmoveAcross.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,12.7 , -.4);  //STRAIGHT_SPEED needs change decrease 3.6/7

        redFoundationPath.stop();
        redFoundationPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 6, STRAIGHT_SPEED);
        redFoundationPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, STRAIGHT_SPEED);

        endRedFoundation.stop();
        endRedFoundation.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, -STRAIGHT_SPEED);

        redFoundationUnderBridge.stop();
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 9, -STRAIGHT_SPEED);
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, STRAIGHT_SPEED);
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 3, STRAIGHT_SPEED);
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 7, -STRAIGHT_SPEED);

        blueFoundationPath.stop();
        blueFoundationPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 6, -STRAIGHT_SPEED);
        blueFoundationPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, STRAIGHT_SPEED);

        endBlueFoundation.stop();
        endBlueFoundation.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, -STRAIGHT_SPEED);

        blueFoundationUnderBridge.stop();
        blueFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 7.5, STRAIGHT_SPEED);
        blueFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.STRAIGHT,6, STRAIGHT_SPEED);
        blueFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 4, -STRAIGHT_SPEED);
        blueFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,7.5 , STRAIGHT_SPEED);

        blueSkyStoneUnderBridge.stop();
        blueSkyStoneUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, -STRAIGHT_SPEED);

        redSkyStoneUnderBridge.stop();
        redSkyStoneUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, STRAIGHT_SPEED); //3.4 needs to be changed

        getCloserPath.stop();
        getCloserPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5.62, -STRAIGHT_SPEED);






    }
    @Override
    public void init()
    {

        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("backLeft");
        rearRight = hardwareMap.dcMotor.get("backRight");

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
        widthTlm = telemetry.addData("stoneWidth", "unknown");
        imageWidthTlm = telemetry.addData("imageWidth", -1);
        marginTlm = telemetry.addData("margin" , "unknown");
        pixelsPerInchTlm = telemetry.addData("pixelsPerInch", "unknown");
        distanceBtWWebcamAndGrabberTlm = telemetry.addData("distance BtW Webcam and Grabber","unknown");
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
        if (allianceColor == AllianceColor.RED) {
            drivetrain1.strafe(SkyStoneConstants25.STRAFE_SPEED);
        } else {
            drivetrain1.strafe(-SkyStoneConstants25.STRAFE_SPEED);
        }
        loggingTlm.setValue("startStrafing:after starting to strafe");
        grabberServo.setPosition(MID_GRABBER_SERVO);
    }


    @Override
    public void start()
    {

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

            //startStrafing();
            getCloserPath();
        }

    }
}
