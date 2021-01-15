package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.Telemetry;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.StandardFourMotorRobot;


@Autonomous(name = "Scrimmage2", group = "Team 25")
// @Disabled
public class UltimateGoalAuto extends Robot {


    private final static String TAG = "auto code for first scrimmage";
    private MechanumGearedDrivetrain drivetrain1;
    private Telemetry.Item loggingTlm;
    private final double STRAIGHT_SPEED = 0.5;
    private final double TURN_SPEED = 0.25;
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private DeadReckonPath launchLinePath;
    private DeadReckonPath targetZoneAPath;
    private DeadReckonPath targetZoneBPath;
    private DeadReckonPath targetZoneCPath;

    DeadReckonPath path = new DeadReckonPath();

    // declaring gamepad variables
    //variables declarations have lowercase then uppercase
    private GamepadTask gamepad;

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

    public void goToTargetZoneA()
    {
        RobotLog.i("drives to target goal A with wobble goal");

        this.addTask(new DeadReckonTask(this, targetZoneAPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("reached target zone A");
                }
            }
        });
    }

    public void goToTargetZoneB()
    {
        RobotLog.i("drives to target goal B with wobble goal");

        this.addTask(new DeadReckonTask(this, targetZoneBPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("reached target zone B");
                }
            }
        });
    }

    public void goToTargetZoneC()
    {
        RobotLog.i("drives to target goal C with wobble goal");

        this.addTask(new DeadReckonTask(this, targetZoneCPath, drivetrain1){
            @Override
            public void handleEvent(RobotEvent e) {
                DeadReckonEvent path = (DeadReckonEvent) e;
                if (path.kind == EventKind.PATH_DONE)
                {
                    RobotLog.i("reached target zone C");
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
        targetZoneAPath = new DeadReckonPath();
        targetZoneBPath = new DeadReckonPath();
        targetZoneCPath = new DeadReckonPath();

        launchLinePath.stop();
        targetZoneAPath.stop();
        targetZoneBPath.stop();
        targetZoneCPath.stop();

        launchLinePath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 70, -STRAIGHT_SPEED);

        targetZoneAPath.addSegment(DeadReckonPath.SegmentType.TURN, 30, TURN_SPEED);
        targetZoneAPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 75, -STRAIGHT_SPEED);

        targetZoneBPath.addSegment(DeadReckonPath.SegmentType.TURN,10, TURN_SPEED);
        targetZoneBPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 90,-STRAIGHT_SPEED);

        targetZoneCPath.addSegment(DeadReckonPath.SegmentType.TURN,20, TURN_SPEED);
        targetZoneCPath.addSegment(DeadReckonPath.SegmentType.STRAIGHT,105, -STRAIGHT_SPEED);

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

    @Override
    public void start()
    {
        loggingTlm = telemetry.addData("log", "unknown");
        parkOnLaunchLine();
    }
}
