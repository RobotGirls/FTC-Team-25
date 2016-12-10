package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelGearedDriveDeadReckon;
import team25core.Robot;
import team25core.RobotEvent;
import test.DaisyTurnTest;

/**
 * FTC Team 25: Created by elizabeth and Katelyn Biesiadecki on 12/6/2016.
 */
@Autonomous(name="Daisy: Beacon 1", group="Team25")
@Disabled
public class DaisyBeacon1Autonomous extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;

    private DeadReckonTask beacon1Task;

    private FourWheelGearedDriveDeadReckon beacon1Path;

    private final int TICKS_PER_INCH = DaisyConfiguration.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = DaisyConfiguration.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = DaisyConfiguration.STRAIGHT_SPEED;
    private final double TURN_SPEED = DaisyConfiguration.TURN_SPEED;

    @Override
    public void handleEvent(RobotEvent e)
    {
        // Nothing for now.
    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        beacon1Path = new FourWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        beacon1Path.addSegment(DeadReckon.SegmentType.TURN, 45, TURN_SPEED);
        beacon1Path.addSegment(DeadReckon.SegmentType.STRAIGHT, 64, STRAIGHT_SPEED);

        beacon1Task = new DeadReckonTask(this, beacon1Path);
    }

    @Override
    public void start()
    {
        addTask(beacon1Task);
    }
}
