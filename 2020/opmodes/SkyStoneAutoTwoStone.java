package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.StoneDetectionTask;

@Autonomous(name = "TwoSkyStones", group = "Team 25") //AutoMeet9
public class SkyStoneAutoTwoStone extends Robot {


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
    private Telemetry.Item currRobotPositionTlm;
    private Telemetry.Item initialRobotPositionTlm;
    private Telemetry.Item secondInitialRobotPositionTlm;
    private Telemetry.Item targetRobotPositionTlm;
    private Telemetry.Item deltaRobotPositionTlm;


    private int numStonesSeen;
    private double numPixelsBtwImgMidptAndStoneMidpt;

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
    private DeadReckonPath secdCloserPath;
    private DeadReckonPath redsecdCloserPath;
    private DeadReckonPath bluesecdCloserPath;
    private DeadReckonPath secdredDepotPath;
    private DeadReckonPath secdblueDepotPath;
    private DeadReckonPath secddepotPath;
    private DeadReckonPath secdbmoveAcross;
    private DeadReckonPath secdrmoveAcross;
    private DeadReckonPath secdmoveAcross;
    private DeadReckonPath leftRedSideways;
    private DeadReckonPath rightBlueSideways;
    private DeadReckonPath sidewaysPath;





    private double confidence;
    private double left;
    private double type;
    private double imageMidpoint;
    private double stoneMidpoint;
    private double delta;
    private double margin = 50;
    private double setColor;
    private double width;
    private int imageWidth;
    private boolean inCenter;
    private double realNumPixelsPerInch;
    private final int DISTANCE_FROM_WEBCAM_TO_GRABBER =1;
    private double distance;
    private int currRobotPosition;
    private int initialRobotPosition;
    private int secondInitialRobotPosition;
    private int targetRobotPosition;
    private int deltaRobotPosition;

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
                    foundationPath = blueFoundationPath;
                    endFoundationPath = endBlueFoundation;
                    foundationUnderBridge = blueFoundationUnderBridge;
                    secdCloserPath = bluesecdCloserPath;
                    secddepotPath = secdblueDepotPath;
                    secdmoveAcross = secdbmoveAcross;
                    skyStoneUnderBridge = blueSkyStoneUnderBridge;
                    sidewaysPath = rightBlueSideways;
                    break;
                case BUTTON_B_DOWN:
                    allianceColor = AllianceColor.RED;
                    allianceTlm.setValue("RED");
                    foundationPath = redFoundationPath;
                    endFoundationPath = endRedFoundation;
                    foundationUnderBridge = redFoundationUnderBridge;
                    secdCloserPath = redsecdCloserPath;
                    secddepotPath = secdredDepotPath;
                    secdmoveAcross = secdrmoveAcross;
                    skyStoneUnderBridge = redSkyStoneUnderBridge;
                    sidewaysPath = leftRedSideways;
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
                if (path.kind == EventKind.PATH_DONE) {
                    RobotLog.i("under bridge from buildsite skystone");
                }
            }
        });
    }

    public void setGrabberServo(double position)
    {
        RobotLog.i("Setting grabber servo to " + position);
        grabberServo.setPosition(position);
    }

    public void moveStonetoBuild()
    {
        RobotLog.i("Go Pick Up Skystone");
        if (allianceColor == AllianceColor.RED) {
            moveAcross = getRedMoveAcrossPath();
        }
        else if (allianceColor == AllianceColor.BLUE)
        {
            moveAcross = getBlueMoveAcrossPath();
        }

        //starts when you have stone and want to move
        this.addTask(new DeadReckonTask(this, moveAcross, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    currRobotPosition = drivetrain1.getCurrentPosition();
                    currRobotPositionTlm.setValue(currRobotPosition);

                    setGrabberServo(MID_GRABBER_SERVO);
                    RobotLog.i("Done with taking stone to build");
                    //moveUnderBridgeFromBuildSiteSkyStoneBuild();
                    secdCloserPath();
                    //startStrafing();

                }
            }
        });
    }
    public void moveSecndStonetoBuild()
    {
        RobotLog.i("Go Pick Up Skystone");

        //starts when you have stone and want to move
        this.addTask(new DeadReckonTask(this, secdmoveAcross, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    currRobotPosition = drivetrain1.getCurrentPosition();
                    currRobotPositionTlm.setValue(currRobotPosition);

                    setGrabberServo(MID_GRABBER_SERVO);
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
                if (path.kind == EventKind.PATH_DONE) {
                    setGrabberServo(DOWN_GRABBER_SERVO);
                    RobotLog.i("Done with path");
                    moveStonetoBuild();
                }
            }
        });
    }

    public void goPickupSecndSkystone(final DeadReckonPath depotPath)
        {
        //FIXME
        RobotLog.i("Go Pick Up Skystone");

        //starts when you find a skystone
        this.addTask(new DeadReckonTask(this, secddepotPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {
                    setGrabberServo(DOWN_GRABBER_SERVO);
                    RobotLog.i("Done with path");
                    moveSecndStonetoBuild();
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
                if (path.kind == EventKind.PATH_DONE) {
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
                if (path.kind == EventKind.PATH_DONE) {
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
                    initialRobotPosition = drivetrain1.getCurrentPosition();
                    initialRobotPositionTlm.setValue(secondInitialRobotPosition);
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

                            secondInitialRobotPosition = drivetrain1.getCurrentPosition();
                            secondInitialRobotPositionTlm.setValue(secondInitialRobotPosition);
                            goPickupSkystone(getRedDepotPath());
                            sdTask.stop();
                            RobotLog.i("506 chose red depot path");
                            pathTlm.setValue("taking red depot path");
                        } else {
                            goPickupSkystone(getBlueDepotPath());
                            sdTask.stop();
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
    public DeadReckonPath getRedDepotPath() {
        DeadReckonPath path = new DeadReckonPath();
        path.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 0.25, -STRAIGHT_SPEED); // going right, might change to .2 //original 1.2
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1.4, -0.45);  //1.2
        return path;
    }

    public DeadReckonPath getRedMoveAcrossPath() {
        DeadReckonPath path = new DeadReckonPath();
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT,4.5  ,.3); //STRAIGHT_SPEED
        path.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,14 , -.70);  //STRAIGHT_SPEED needs change decrease 3.6/7
        return path;

    }

    public DeadReckonPath getBlueDepotPath() {
        DeadReckonPath path = new DeadReckonPath();
        path.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,0.6, -STRAIGHT_SPEED);  //
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT,1.3, -0.4); //1.2
        return path;
    }

    public DeadReckonPath getBlueMoveAcrossPath() {
        DeadReckonPath path = new DeadReckonPath();
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT,4.5 ,.2);
        path.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,15.5, .70);
        return path;

    }

    public void initPath()
    {

        bluesecdCloserPath = new DeadReckonPath();
        redsecdCloserPath = new DeadReckonPath();

        redFoundationPath = new DeadReckonPath();
        blueFoundationPath = new DeadReckonPath();

        endRedFoundation = new DeadReckonPath();
        endBlueFoundation = new DeadReckonPath();

        redFoundationUnderBridge = new DeadReckonPath();
        blueFoundationUnderBridge = new DeadReckonPath();

        blueSkyStoneUnderBridge = new DeadReckonPath();
        redSkyStoneUnderBridge = new DeadReckonPath();

        secdredDepotPath = new DeadReckonPath();
        secdblueDepotPath = new DeadReckonPath();

        secdrmoveAcross = new DeadReckonPath();
        secdbmoveAcross = new DeadReckonPath();

        rightBlueSideways = new DeadReckonPath();
        leftRedSideways = new DeadReckonPath();

        getCloserPath = new DeadReckonPath();
        //add path to get to bridge

        rightBlueSideways.stop();
        rightBlueSideways.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,11, -.80);
        rightBlueSideways.addSegment(DeadReckonPath.SegmentType.STRAIGHT,2, -.60);

        leftRedSideways.stop();
        leftRedSideways.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,10, .70);
        leftRedSideways.addSegment(DeadReckonPath.SegmentType.STRAIGHT,.5, -.60);


        redFoundationPath.stop();
        redFoundationPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7, 0.8); // move forwards to foundation
        redFoundationPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5,0.8); // strafe to align w/ foundation
        redFoundationPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,5,0.8); // push up to foundation

        endRedFoundation.stop();
        endRedFoundation.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, -0.5); // pull foundation back
        endRedFoundation.addSegment(DeadReckonPath.SegmentType.TURN, 130, -0.7); // turn the foundation into the building site

        redFoundationUnderBridge.stop();
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 5, 0.8); // push foundation against wall (modified)
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.STRAIGHT,4,-0.8);//backs up (NEW LINE)
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 4, -0.4);//strafes sideways (modified)
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 6, -0.8); // backs up to skybridge (modified)
        redFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,2, -0.4); //pushes up against bridge (NEW)

        blueFoundationPath.stop();
        blueFoundationPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 7, 0.8); // move forwards to foundation
        blueFoundationPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5,-0.8); // strafe to align w/ foundation
        blueFoundationPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,5,0.8); // push up to foundation

        endBlueFoundation.stop();
        endBlueFoundation.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 8, -0.5); // pull foundation back
        endBlueFoundation.addSegment(DeadReckonPath.SegmentType.TURN, 120, 0.7); // turn the foundation into the building site

        blueFoundationUnderBridge.stop();
        blueFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 2.5, 0.8); // push foundation against wall
        blueFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.STRAIGHT,11,-0.8);//backs up (NEW LINE)v
        blueFoundationUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 2, 0.4);//pushes up on inside of skybridge

        blueSkyStoneUnderBridge.stop();
        blueSkyStoneUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, -STRAIGHT_SPEED);

        redSkyStoneUnderBridge.stop();
        redSkyStoneUnderBridge.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, STRAIGHT_SPEED); //3.4 needs to be changed

        redsecdCloserPath.stop();
        redsecdCloserPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 7, STRAIGHT_SPEED);
        redsecdCloserPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 2.5, -STRAIGHT_SPEED); // needs testing

        secdredDepotPath.stop();
        secdredDepotPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,0.25, STRAIGHT_SPEED);  //might change to .2 //original 1.2
        //redDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,1.5, -0.4);  //2
        secdredDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,1.4, -0.4);  //1.2

        secdrmoveAcross.stop();
        secdrmoveAcross.addSegment(DeadReckonPath.SegmentType.STRAIGHT,4.5,.3); //STRAIGHT_SPEED
        secdrmoveAcross.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,30, -1.5);  //STRAIGHT_SPEED needs change decrease 3.6/7
        secdrmoveAcross.addSegment(DeadReckonPath.SegmentType.STRAIGHT,3,.4);

        bluesecdCloserPath.stop();
        bluesecdCloserPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 3.5, -STRAIGHT_SPEED);
        bluesecdCloserPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 1.5, -STRAIGHT_SPEED); // needs testing

        secdblueDepotPath.stop();
        secdblueDepotPath.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,0.2, -STRAIGHT_SPEED);  //
        secdblueDepotPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,1.6, -0.4); //

        secdbmoveAcross.stop();
        secdbmoveAcross.addSegment(DeadReckonPath.SegmentType.STRAIGHT,4.5 ,.2);
        secdbmoveAcross.addSegment(DeadReckonPath.SegmentType.SIDEWAYS,30, .4);

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
        setGrabberServo(UP_GRABBER_SERVO);

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
        currRobotPositionTlm = telemetry.addData("currRobotPosition", -1);
        initialRobotPositionTlm = telemetry.addData("initialRobotPosition", -1);
        secondInitialRobotPositionTlm = telemetry.addData("secondInitialRobotPosition", -1);
        targetRobotPositionTlm = telemetry.addData("targetRobotPosition", -1);
        deltaRobotPositionTlm = telemetry.addData("deltaRobotPosition", -1);


        drivetrain1 = new MechanumGearedDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
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

    public void secdCloserPath()
    {
        //get closer to stones to detect + pick up 2nd
        RobotLog.i("Moving closer to stones ");


        //starts when you find a skystone
        this.addTask(new DeadReckonTask(this, secdCloserPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {

                    //secdstartStrafing();
                    sidewaysPath();
                }
            }
        });

    }

    public void sidewaysPath()
    {
        //get closer to stones to detect + pick up 2nd
        RobotLog.i("Moving closer to stones ");


        //starts when you find a skystone
        this.addTask(new DeadReckonTask(this, sidewaysPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE) {

                    secdstartStrafing();
                }
            }
        });

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
        setGrabberServo(MID_GRABBER_SERVO);
    }

    public void secdstartStrafing()
    {
        //setStoneDetection();
        // start looking for second skystone
        RobotLog.i("startStrafing");
        sdTask.start();
        addTask(sdTask);
        loggingTlm.setValue("startStrafing:before starting to strafe");
        if (allianceColor == AllianceColor.RED) {
            drivetrain1.strafe(SkyStoneConstants25.STRAFE_SPEED);
        } else {
            drivetrain1.strafe(-SkyStoneConstants25.STRAFE_SPEED);
        }
        loggingTlm.setValue("secdStartStrafing:after starting to strafe");
        setGrabberServo(MID_GRABBER_SERVO);


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

        if (robotPosition == RobotPosition.BUILD_SITE) {
           //parkUnderBridge();
            moveFoundation(foundationPath);

        } else if (robotPosition == RobotPosition.DEPOT) {
            RobotLog.i("start: before startStrafing");
            loggingTlm.setValue("start:in DEPOT about to startStrafing");

            //startStrafing();
            //initialRobotPosition = drivetrain1.getCurrentPosition();
            //initialRobotPositionTlm.setValue(initialRobotPosition);
            getCloserPath();
        }

    }
}
