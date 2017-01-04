package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDriveDeadReckon;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by Lizzie on 11/19/2016.
 */
@Autonomous(name = "EASY 2 autonomous", group = "AutoTest")
@Disabled

public class CapCenterAutonomous extends Robot{

    public static final int TICKS_PER_DEGREE = 20;
    private final static int TICKS_PER_INCH = 20;

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;

    private FourWheelDirectDriveDeadReckon pushCapBallDeadReckon;
    private DeadReckonTask pushCapBallTask;

    private FourWheelDirectDriveDeadReckon parkCenterDeadReckon;
    private DeadReckonTask parkCenterTask;

    @Override
    public void init() {
        frontLeft = hardwareMap.dcMotor.get("motorFL");
        frontRight = hardwareMap.dcMotor.get("motorFR");
        backLeft = hardwareMap.dcMotor.get("motorBL");
        backRight = hardwareMap.dcMotor.get("motorBR");

        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        pushCapBallDeadReckon = new FourWheelDirectDriveDeadReckon
                (this, TICKS_PER_DEGREE, TICKS_PER_INCH, frontRight, backRight, frontLeft, backLeft);
        pushCapBallDeadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 45, .75);

        parkCenterDeadReckon = new FourWheelDirectDriveDeadReckon
                (this, TICKS_PER_DEGREE, TICKS_PER_INCH, frontRight, backRight, frontLeft, backLeft);
        parkCenterDeadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, -.35);
        parkCenterDeadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 11, .75);

        pushCapBallTask = new DeadReckonTask(this, pushCapBallDeadReckon);
        parkCenterTask = new DeadReckonTask(this, parkCenterDeadReckon);
    }

    @Override
    public void handleEvent(RobotEvent e) {
        DeadReckonTask.DeadReckonEvent event = (DeadReckonTask.DeadReckonEvent)e;

        if (event.kind == DeadReckonTask.EventKind.PATH_DONE) {
            addTask(parkCenterTask);
        }
    }

    @Override
    public void start() {

        addTask(pushCapBallTask);
    }
}
