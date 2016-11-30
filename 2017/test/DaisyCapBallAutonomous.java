package test;

import com.qualcomm.robotcore.hardware.DcMotor;

import opmodes.DaisyConfiguration;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDriveDeadReckon;
import team25core.FourWheelGearedDriveDeadReckon;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by elizabeth on 11/29/16.
 */
public class DaisyCapBallAutonomous extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;

    private DeadReckonTask cbTask;

    private FourWheelGearedDriveDeadReckon cbPath;
    private final int TICKS_PER_INCH = DaisyConfiguration.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = DaisyConfiguration.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = DaisyConfiguration.STRAIGHT_SPEED;
    private final double TURN_SPEED = DaisyConfiguration.TURN_SPEED;


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

        cbPath = new FourWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight,rearLeft, rearRight);
        cbPath.addSegment(DeadReckon.SegmentType.STRAIGHT, 62, STRAIGHT_SPEED);

    }

    @Override
    public void start()
    {
        addTask(cbTask);
    }
}
