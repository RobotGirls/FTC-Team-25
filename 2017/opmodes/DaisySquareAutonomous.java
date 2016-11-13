package opmodes;

import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDriveDeadReckon;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by Katelyn Biesiadecki on 11/5/2016.
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


    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        squarePath = new FourWheelDirectDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontRight, rearRight, frontLeft, rearLeft);
        squarePath.addSegment(DeadReckon.SegmentType.STRAIGHT, 10, 0.8);
        squarePath.addSegment(DeadReckon.SegmentType.TURN, 45, 0.8);
        squarePath.addSegment(DeadReckon.SegmentType.STRAIGHT, 10, 0.8);
        squarePath.addSegment(DeadReckon.SegmentType.TURN, 45, 0.8);

        deadReckonTask = new DeadReckonTask(this, squarePath);
    }

    @Override
    public void start()
    {
        addTask(deadReckonTask);
    }
}
