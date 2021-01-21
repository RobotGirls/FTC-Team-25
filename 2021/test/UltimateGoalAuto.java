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


@Autonomous(name = "Scrimmage4", group = "Team 25")
// @Disabled
public class UltimateGoalAuto extends Robot {


    private final static String TAG = "auto code for first scrimmage";
    private MechanumGearedDrivetrain drivetrain1;
    private Telemetry.Item loggingTlm;
    private Telemetry.Item objectSeenTlm;
    private DeadReckonPath launchLinePath;
    private final double STRAIGHT_SPEED = 0.5;
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private Telemetry.Item currentLocationTlm;
    private Telemetry.Item handleEventTlm;
    private int numTimesInHandleEvent = 0;
    private double ringConfidence;
   // private String ringType = "unknown";
    private String ringType;

    DeadReckonPath path = new DeadReckonPath();

    // declaring gamepad variables
    //variables declarations have lowercase then uppercase
    private GamepadTask gamepad;

    RingDetectionTask rdTask;
    RingImageInfo ringImageInfo;

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
                ringImageInfo.getImageInfo(event);
                ringConfidence = ringImageInfo.getConfidence();
                ringType = ringImageInfo.getRingType();
                currentLocationTlm.setValue("in RingDetectionTask handleEvent");
                numTimesInHandleEvent++;
                handleEventTlm.setValue(numTimesInHandleEvent);


                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    objectSeenTlm.setValue(ringType);
                  //  rdTask.stop();
                      if (ringType.equals("Single") ){
                          objectSeenTlm.setValue("single ring");
                      } else if (ringType.equals("Quad")){
                          objectSeenTlm.setValue("quad rings");
                      } else {
                          objectSeenTlm.setValue("no rings");
                      }

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
               }
            }
        };
        currentLocationTlm.setValue("in setRingDetection");
        rdTask.init(telemetry, hardwareMap);
//      //FIXME update quad ring detection to look for single ring or quad ring
        rdTask.setDetectionKind(RingDetectionTask.DetectionKind.EVERYTHING);

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
        currentLocationTlm = telemetry.addData("current location", "in init" );
        handleEventTlm = telemetry.addData("num times in handle event", "0");
        objectSeenTlm = telemetry.addData("saw", "unknown");


        //initializing drivetrain
        drivetrain1 = new MechanumGearedDrivetrain(frontRight, backRight, frontLeft, backLeft);
        drivetrain1.resetEncoders();
        drivetrain1.encodersOn();
        RobotLog.i("start moving");

        //initializing gamepad variables
        gamepad = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1);
        addTask(gamepad);

        ringImageInfo = new RingImageInfo(this);
        setRingDetection();

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
        currentLocationTlm.setValue("in start");
        addTask(rdTask);

        //parkOnLaunchLine();
    }
}
