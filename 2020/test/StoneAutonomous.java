package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.StoneDetectionTask;

@Autonomous(name = "SkyStone Autonomous2", group = "Team 25")
public class StoneAutonomous extends Robot {


    private final static String TAG = "Margarita";

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    //for mechanism
    //private DcMotor intakeR
    //private DcMotor intakeL
    //private DcMotor
    //Private DcMotor

    private MechanumGearedDrivetrain drivetrain;

    private Telemetry.Item stonePositionTlm;
    private Telemetry.Item stoneTlm;
    private Telemetry.Item stoneConfidTlm;
    private Telemetry.Item stoneTypeTlm;
    private Telemetry.Item stoneMidpointTlm;
    private Telemetry.Item imageMidpointTlm;
    private Telemetry.Item loggingTlm;
    private Telemetry.Item handleEvntTlm;
    private Telemetry.Item deltaTlm;


    private double confidence;
    private double left;
    private double type;
    private double imageMidpoint;
    private double stoneMidpoint;
    private double delta;
    private double margin = 500;
    private boolean inCenter;
    private String stoneType;
    private StoneDetectionTask.EventKind stoneKind;

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
                    break;
                case BUTTON_B_DOWN:
                    allianceColor = AllianceColor.RED;
                    allianceTlm.setValue("RED");
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

    public void goPickupSkystone()
    {
        //FIXME
        RobotLog.i("Go Pick Up Skystone");
    }

    public void setStoneDetection()
    {

        sdTask = new StoneDetectionTask(this, "Webcam1") {
            //starts when you find a skystone
            @Override
            public void handleEvent(RobotEvent e) {
                StoneDetectionTask.StoneDetectionEvent event = (StoneDetectionTask.StoneDetectionEvent) e;
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

                stonePositionTlm.setValue(left);
                stoneConfidTlm.setValue(confidence);
                imageMidpointTlm.setValue(imageMidpoint);
                stoneMidpointTlm.setValue(stoneMidpoint);
                stoneTypeTlm.setValue(stoneType);
                stoneTlm.setValue(stoneKind);
                deltaTlm.setValue(delta);

                if (event.kind == StoneDetectionTask.EventKind.OBJECTS_DETECTED) {
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
        //later adbwill find skystone
        sdTask.setDetectionKind(StoneDetectionTask.DetectionKind.SKY_STONE_DETECTED);

    }


    public void loop()
    {
        super.loop();

    }

    @Override
    public void init()
    {
        /*frontLeft = hardwareMap.get(DcMotor.class, "frontLeft");
        frontRight = hardwareMap.get(DcMotor.class, "frontRight");
        rearLeft = hardwareMap.get(DcMotor.class, "rearLeft");
        rearRight = hardwareMap.get(DcMotor.class, "rearRight");*/
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        //caption: what appears on the phone
        stonePositionTlm = telemetry.addData("LeftOrigin", "unknown");
        stoneConfidTlm = telemetry.addData("Confidence", "N/A");
        stoneTypeTlm = telemetry.addData("StoneType","unknown");
        imageMidpointTlm = telemetry.addData("Image_Mdpt", "unknown");
        stoneMidpointTlm = telemetry.addData("Stone Mdpt", "unknown");
        stoneTlm = telemetry.addData("kind", "unknown");
        deltaTlm = telemetry.addData("delta", "unknown");

        RobotLog.ii(TAG,  "delta: " + delta);


        drivetrain = new MechanumGearedDrivetrain(360, frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();
        RobotLog.i("start moving");

        //initializing gamepad variables
        allianceColor = allianceColor.DEFAULT;
        gamepad = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1);
        addTask(gamepad);

        //telemetry setup
        telemetry.setAutoClear(false);
        allianceTlm = telemetry.addData("ALLIANCE", "Unselected (X-blue /B-red)");
        positionTlm = telemetry.addData("POSITION", "Unselected (Y-build/A-depot)");


        setStoneDetection();
    }

    public void startStrafing()
    {
        //start looking for Skystones
        RobotLog.i("startStrafing");
        addTask(sdTask);
        loggingTlm.setValue("startStrafing:before starting to strafe");
        drivetrain.strafe(SkyStoneConstants25.STRAFE_SPEED);
        loggingTlm.setValue("startStrafing:after starting to strafe");
    }

    public void parkUnderBridge()
    {
        this.addTask(new DeadReckonTask(this, path, drivetrain));
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

        parkUnderBridge();


        /* if (robotPosition == RobotPosition.BUILD_SITE)
        {
            parkUnderBridge();

        }
        else if (robotPosition == RobotPosition.DEPOT)
        {
            RobotLog.i("start: before startStrafing");
            loggingTlm.setValue("start:in DEPOT about to startStrafing");

            startStrafing();
        }*/

    }
}
