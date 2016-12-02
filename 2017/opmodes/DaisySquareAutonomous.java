package opmodes;

import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDriveDeadReckon;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/5/2016.
 */

public class DaisySquareAutonomous extends Robot
{

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DeadReckonTask deadReckonTask;
    private FourWheelDirectDriveDeadReckon squarePath;
    private final int TICKS_PER_INCH = DaisyConfiguration.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = DaisyConfiguration.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = DaisyConfiguration.STRAIGHT_SPEED;
    private final double TURN_SPEED = DaisyConfiguration.TURN_SPEED;

    @Override
    public void handleEvent(RobotEvent e)
    {
        // Nothing... for now!
    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        squarePath = new FourWheelDirectDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontRight, rearRight, frontLeft, rearLeft);
        squarePath.addSegment(DeadReckon.SegmentType.STRAIGHT, 10, STRAIGHT_SPEED);
        squarePath.addSegment(DeadReckon.SegmentType.TURN, 45, TURN_SPEED);
        squarePath.addSegment(DeadReckon.SegmentType.STRAIGHT, 10, STRAIGHT_SPEED);
        squarePath.addSegment(DeadReckon.SegmentType.TURN, 45, TURN_SPEED);

        deadReckonTask = new DeadReckonTask(this, squarePath);
    }

    @Override
    public void start()
    {
        addTask(deadReckonTask);
    }
}
