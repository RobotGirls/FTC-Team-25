package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

//import opmodes.SkyStoneAutoTwoStone;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.RingDetectionTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;
import team25core.StoneDetectionTask;


@Autonomous(name = "Scrimmage2", group = "Team 25")
// @Disabled
public class UltimateGoalAuto extends Robot {


    private final static String TAG = "auto code for first scrimmage";
    private MechanumGearedDrivetrain drivetrain1;
    private Telemetry.Item loggingTlm;
    private DeadReckonPath launchLinePath;
    private final double STRAIGHT_SPEED = 0.5;
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    DeadReckonPath path = new DeadReckonPath();

    // declaring gamepad variables
    //variables declarations have lowercase then uppercase
    private GamepadTask gamepad;

    RingDetectionTask rdTask;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent) e).segment_num);
        }
    }


    public void parkOnLaunchLine()
    {
        RobotLog.i("drives straight onto the launch line");


        //starts when you have stone and want to move
        this.addTask(new DeadReckonTask(this, launchLinePath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("finished parking");
                }
            }
        });
    }


    public void loop()
    {
        super.loop();
    }


    public void initPath()
    {
        launchLinePath = new DeadReckonPath();
        launchLinePath.stop();
        launchLinePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 3, -STRAIGHT_SPEED);
    }

    public void setRingDetection()
    {

        rdTask = new RingDetectionTask(this, "Webcam1") {
            //the handleEvent method is called when a ring is detected
            @Override
            public void handleEvent(RobotEvent e) {
                RingDetectionTask.RingDetectionEvent event = (RingDetectionEvent) e;
//                //0 gives you the first stone on list of stones
//                confidence = event.stones.get(0).getConfidence();
//                left = event.stones.get(0).getLeft();
//
//                RobotLog.ii(TAG, "Saw: " + event.kind + " Confidence: " + confidence);
//                RobotLog.i("startHandleEvent");
//
//                imageMidpoint = event.stones.get(0).getImageWidth() / 2.0;
//                stoneMidpoint = (event.stones.get(0).getWidth() / 2.0) + left;
//                width = event.stones.get(0).getWidth();
//                imageWidth = event.stones.get(0).getImageWidth();
//
//                stoneType = event.stones.get(0).getLabel();
//                stoneKind = event.kind;
//                delta = Math.abs(imageMidpoint - stoneMidpoint);
//                RobotLog.i("type");
//
//                stonePositionTlm.setValue(left);
//                stoneConfidTlm.setValue(confidence);
//                imageMidpointTlm.setValue(imageMidpoint);
//                stoneMidpointTlm.setValue(stoneMidpoint);
//                stoneTypeTlm.setValue(stoneType);
//                stoneTlm.setValue(stoneKind);
//                deltaTlm.setValue(delta);
//                pathTlm.setValue(setColor);
//                widthTlm.setValue(width);
//                imageWidthTlm.setValue(imageWidth);
//
//                numStonesSeen = event.stones.size();
//                numStonesSeenTlm.setValue(numStonesSeen);
//                RobotLog.ii(TAG,"numStonesSeen",numStonesSeen);
//
//                if (event.kind == EventKind.OBJECTS_DETECTED) {
//
//                    numPixelsBtwImgMidptAndStoneMidpt = stoneMidpoint - imageMidpoint;
//                    realNumPixelsPerInch = (width/8.0);
//                    pixelsPerInchTlm.setValue(realNumPixelsPerInch);
//                    distance = (double)(DISTANCE_FROM_WEBCAM_TO_GRABBER * realNumPixelsPerInch);
//                    distanceBtWWebcamAndGrabberTlm.setValue(distance);
//
//                    marginTlm.setValue(Math.abs(numPixelsBtwImgMidptAndStoneMidpt - distance));
//                    if ((numPixelsBtwImgMidptAndStoneMidpt > 0)  &&
//                            (Math.abs(numPixelsBtwImgMidptAndStoneMidpt - distance)  < margin)) {
//                    /* old detection if (Math.abs(imageMidpoint - stoneMidpoint) < margin) {
//                        inCenter = true
//                        RobotLog.i("506 Found gold");
//                        sdTask.stop();
//                        drivetrain1.stop();*/
//
//                        if (allianceColor == SkyStoneAutoTwoStone.AllianceColor.RED) {
//
//                            secondInitialRobotPosition = drivetrain1.getCurrentPosition();
//                            secondInitialRobotPositionTlm.setValue(secondInitialRobotPosition);
//                            goPickupSkystone(getRedDepotPath());
//                            sdTask.stop();
//                            RobotLog.i("506 chose red depot path");
//                            pathTlm.setValue("taking red depot path");
//                        } else {
//                            goPickupSkystone(getBlueDepotPath());
//                            sdTask.stop();
//                            RobotLog.i("506 chose blue depot path");
//                            pathTlm.setValue("taking blue depot path" );
//                        }
//                    }
//                }
            }
        };

        rdTask.init(telemetry, hardwareMap);
//        //later adbwill find skystone
        rdTask.setDetectionKind(RingDetectionTask.DetectionKind.QUAD_RING_DETECTED);

    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        //caption: what appears on the phone
        loggingTlm = telemetry.addData("distance traveled", "unknown");

        //initializing drivetrain
        drivetrain1 = new MechanumGearedDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain1.resetEncoders();
        drivetrain1.encodersOn();
        RobotLog.i("start moving");

        //initializing gamepad variables
        gamepad = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1);
        addTask(gamepad);

        //initializing autonomous path
        initPath();
    }


//    public void startStrafing()
//    {
//        //start looking for Skystones
//        RobotLog.i("startStrafing");
//        addTask(sdTask);
//        loggingTlm.setValue("startStrafing:before starting to strafe");
//        if (allianceColor == AllianceColor.RED) {
//            drivetrain1.strafe(opmodes.SkyStoneConstants25.STRAFE_SPEED);
//        } else {
//            drivetrain1.strafe(-opmodes.SkyStoneConstants25.STRAFE_SPEED);
//        }
//        loggingTlm.setValue("startStrafing:after starting to strafe");
//    }


    @Override
    public void start()
    {
        loggingTlm = telemetry.addData("log", "unknown");
        parkOnLaunchLine();
    }
}
