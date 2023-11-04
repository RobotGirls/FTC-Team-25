package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckonTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.ObjectDetectionNewTask;
import team25core.Robot;
import team25core.RobotEvent;

@Autonomous(name = "AprilTagAuto")
public class CenterstageAutoAprilTags extends Robot {
    private ObjectDetectionNewTask objDetectionTask;
    private final static String TAG = "Prop";
    final double DESIRED_DISTANCE = 12.0; //  this is how close the camera should get to the target (inches)
    //  Set the GAIN constants to control the relationship between the measured position error,
    //  and how much power is applied to the drive motors to correct the error.
    //  Drive = Error * Gain    Make these values smaller for smoother control, or larger
    //  for a more aggressive response.
    final double SPEED_GAIN  =  0.02  ;   //  Forward Speed Control "Gain". eg: Ramp up to 50% power at a 25 inch error.   (0.50 / 25.0)
    final double STRAFE_GAIN =  0.015 ;   //  Strafe Speed Control "Gain".  eg: Ramp up to 25% power at a 25 degree Yaw error.   (0.25 / 25.0)
    final double TURN_GAIN   =  0.01  ;   //  Turn Control "Gain".  eg: Ramp up to 25% power at a 25 degree error. (0.25 / 25.0)

    final double MAX_AUTO_SPEED = 0.5;   //  Clip the approach speed to this max value (adjust for your robot)
    final double MAX_AUTO_STRAFE= 0.5;   //  Clip the approach speed to this max value (adjust for your robot)
    final double MAX_AUTO_TURN  = 0.3;   //  Clip the turn speed to this max value (adjust for your robot)

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private FourWheelDirectDrivetrain drivetrain;

    // FIXME Katelyn find out the tag ID of the AprilTag and replace -1 with that number
    private int desiredTagID;     // Choose the tag you want to approach or set to -1 for ANY tag.

    private final float APRIL_TAG_DECIMATION = 2;

    private final int EXPOSURE_MS = 6;
    private final int GAIN = 250;

    public String position = "left"; // this will contain the actual prop position information in final auto

    @Override
    public void handleEvent(RobotEvent e)
    {
        /*
         * Every time we complete a segment drop a note in the robot log.
         */
        //if (e instanceof DeadReckonTask.DeadReckonEvent) {
          //  RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        //}
    }

    public void findAprilTag()
    {
        RobotLog.ii(TAG, "Setup findAprilTag");
        objDetectionTask = new ObjectDetectionNewTask(this, telemetry, ObjectDetectionNewTask.DetectionKind.APRILTAG_DETECTED){
            @Override
            public void handleEvent(RobotEvent e) {
                TagDetectionEvent event = (TagDetectionEvent) e;
                switch (event.kind) {
                    case OBJECTS_DETECTED:
                        RobotLog.ii(TAG, "Object detected");
                        break;
                }
            }
        };
        objDetectionTask.init(telemetry, hardwareMap);
        objDetectionTask.rateLimit(1000); // currently calling objDetectionTask every second
        objDetectionTask.start();
        objDetectionTask.resumeStreaming();
        objDetectionTask.setAprilTagDecimation(APRIL_TAG_DECIMATION);
        objDetectionTask.doManualExposure(EXPOSURE_MS, GAIN); // Use low exposure time to reduce motion blur
        objDetectionTask.setDesiredTagID(desiredTagID);
        addTask(objDetectionTask);
    }


    // find desired id for blue alliance (1, 2, or 3)
    public void findDesiredID() {
        if (position.equals("left")) {
            desiredTagID = 1; // 4 on red
        }
        else if (position.equals("center")) {
            desiredTagID = 2; // 5 on red
        }
        else {
            desiredTagID = 3; // 6 on red
        }
    }


    @Override
   public void init(){
        //initializes the motors for the wheels
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

    }
   @Override
   public void start(){
        findDesiredID();
        findAprilTag();
   }
}
