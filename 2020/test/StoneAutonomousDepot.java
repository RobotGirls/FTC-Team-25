package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import opmodes.SkyStoneConstants25;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.MechanumGearedDrivetrain;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;
import team25core.StoneDetectionTask;

@Autonomous(name = "Stone Autonomous", group = "Team 25")
public class StoneAutonomousDepot extends StandardFourMotorRobot {


    private final static String TAG = "Margarita";

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;

    private MechanumGearedDrivetrain drivetrain;

    private Telemetry.Item stonePositionTlm;
    private Telemetry.Item stoneTlm;
    private Telemetry.Item stoneConfidTlm;
    private Telemetry.Item stoneTypeTlm;
    private Telemetry.Item stoneMidpointTlm;
    private Telemetry.Item imageMidpointTlm;

    private double confidence;
    private double left;
    private double type;
    private double imageMidpoint;
    private double stoneMidpoint;
    private double margin;
    private boolean inCenter;

    StoneDetectionTask sdTask;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            RobotLog.i("Completed path segment %d", ((DeadReckonTask.DeadReckonEvent)e).segment_num);
        }
    }
    public void goPickupSkystone()
    {
        //FIXME
    }

    public void setStoneDetection()
    {
        //caption: what appears on the phone
        stonePositionTlm = telemetry.addData("LeftOrigin", "unknown");
        stoneConfidTlm = telemetry.addData("Confidence", "N/A");
        stoneTypeTlm = telemetry.addData("StoneType","unknown");
        imageMidpointTlm = telemetry.addData("Image_Mdpt", "unknown");
        stoneMidpointTlm = telemetry.addData("Stone Mdpt", "unknown");


        sdTask = new StoneDetectionTask(this, "Webcam1") {


            //starts when you find a skystone
            @Override
            public void handleEvent(RobotEvent e) {
                StoneDetectionEvent event = (StoneDetectionEvent) e;
                //0 gives you the first stone on list of stones
                confidence = event.stones.get(0).getConfidence();
                left = event.stones.get(0).getLeft();

                RobotLog.ii(TAG, "Saw: " + event.kind + " Confidence: " + confidence);

                imageMidpoint = event.stones.get(0).getImageWidth() / 2.0;
                stoneMidpoint = (event.stones.get(0).getWidth() / 2.0) + left;

                stonePositionTlm.setValue(left);
                stoneConfidTlm.setValue(confidence);
                imageMidpointTlm.setValue(imageMidpoint);
                stoneMidpointTlm.setValue(stoneMidpoint);

                if (event.kind == EventKind.OBJECTS_DETECTED) {
                    if (Math.abs(imageMidpoint - stoneMidpoint) < margin) {
                        inCenter = true;
                        RobotLog.i("506 Found gold");
                        sdTask.stop();
                        drivetrain.stop();
                        goPickupSkystone();
                    }
                }

            }

        };
        sdTask.init(telemetry, hardwareMap);
        //later will find skystone
        sdTask.setDetectionKind(StoneDetectionTask.DetectionKind.SKY_STONE_DETECTED);
    }
    @Override
        public void init()
        {

            drivetrain = new MechanumGearedDrivetrain(frontLeft, backLeft, frontRight, backRight);
            drivetrain.encodersOn();
            drivetrain.resetEncoders();

            setStoneDetection();
        }
    public void startStrafing()
    {
        //start looking for Skystones
        addTask(sdTask);
        drivetrain.strafe(SkyStoneConstants25.STRAFE_SPEED);
    }
    @Override
    public void start()
    {
        DeadReckonPath path = new DeadReckonPath();

        /*path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 1.0);
        path.addSegment(DeadReckonPath.SegmentType.TURN, 90, 1.0);
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 1.0);
        path.addSegment(DeadReckonPath.SegmentType.TURN, 90, 1.0);
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 1.0);
        path.addSegment(DeadReckonPath.SegmentType.TURN, 90, 1.0);
        path.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 10, 1.0);
        *
         */

        /**
         * Alternatively, this could be an anonymous class declaration that implements
         * handleEvent() for task specific event handlers.
         */
        //this.addTask(new DeadReckonTask(this, path, drivetrain));

        startStrafing();
    }
}
