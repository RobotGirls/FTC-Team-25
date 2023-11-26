package opmodes.distancesensor;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.DistanceSensorTask;
import team25core.FourWheelDirectDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import team25core.DistanceSensorCriteria;
import team25core.vision.apriltags.AprilTagDetectionTask;

@Autonomous(name = "DistanceSensorProp")
public class DistanceSensorTest extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private FourWheelDirectDrivetrain drivetrain;

    private DistanceSensorTask distanceTask;
    private final static String TAG = "PROP";
    private DistanceSensor rightSensor;
    private DistanceSensor leftSensor;
    private Telemetry.Item tagIdTlm;
    private Telemetry.Item rightSensorTlm;
    private Telemetry.Item leftSensorTlm;

    private Telemetry.Item locationTlm;
    private Telemetry.Item whereAmI;
    private Telemetry.Item eventTlm;

    double rightDistance;
    double leftDistance;

    double minDistance;
    double maxDistance;

    @Override
    public void handleEvent(RobotEvent e)
    {
        whereAmI.setValue("in handleEvent");
        /*
         * Every time we complete a segment drop a note in the robot log.
         */
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
    }
    public void detectProp() {
        RobotLog.ii(TAG, "Setup detectProp");
        distanceTask = new DistanceSensorTask(this, rightSensor, leftSensor, telemetry, 0, 4, 15 ,
                9,false) {
            @Override
            public void handleEvent(RobotEvent e) {
                DistanceSensorEvent event = (DistanceSensorEvent) e;
                switch (event.kind) {
                    case LEFT_DISTANCE:
                        //RobotLog.ii(TAG, " left distance %d", event.distance);
                        locationTlm.setValue("left");
                        break;
                    case RIGHT_DISTANCE:
                        //RobotLog.ii(TAG, " right distance %d", event.distance);
                        locationTlm.setValue("right");
                        break;
                    case UNKNOWN:
                        locationTlm.setValue("middle");
                        break;

                }
            }
        };
    }


    public void init()
    {
        // hardware mapping
        frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        backLeft = hardwareMap.get(DcMotor.class, "backLeft");
        backRight = hardwareMap.get(DcMotor.class, "backRight");

        rightSensor = hardwareMap.get(DistanceSensor.class, "rightSensor");
        leftSensor = hardwareMap.get(DistanceSensor.class, "leftSensor");

        // instantiating FourWheelDirectDrivetrain
        drivetrain = new FourWheelDirectDrivetrain(frontRight, backRight, frontLeft, backLeft);

        detectProp();
        //sets motors position to 0
        drivetrain.resetEncoders();

        //motor will try to tun at the targeted velocity
        drivetrain.encodersOn();

        // telemetry shown on the phone
        whereAmI = telemetry.addData("location in code", "init");
        tagIdTlm = telemetry.addData("tagId", "none");
        rightSensorTlm = telemetry.addData("rightSensor", "none");
        leftSensorTlm = telemetry.addData("leftSensor", "none");
        locationTlm = telemetry.addData("prop position", "none");

        initPaths();
    }

    public void start()
    {
        whereAmI.setValue("in Start");

        addTask(distanceTask);
    }

    public void initPaths() {


    }
}