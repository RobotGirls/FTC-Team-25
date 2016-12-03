package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelGearedDriveDeadReckon;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by elizabeth on 12/1/16.
 */
@Autonomous(name="Daisy: Corner Park", group="Team25")
public class DaisyCornerParkAutonomous extends Robot {

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;

    private DeadReckonTask cornerParkTask;

    private FourWheelGearedDriveDeadReckon cornerParkPath;

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

        cornerParkPath = new FourWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        cornerParkPath.addSegment(DeadReckon.SegmentType.STRAIGHT, 52, STRAIGHT_SPEED);
        cornerParkPath.addSegment(DeadReckon.SegmentType.TURN, 120, TURN_SPEED);
        cornerParkPath.addSegment(DeadReckon.SegmentType.STRAIGHT, 32, STRAIGHT_SPEED);

        cornerParkTask = new DeadReckonTask(this, cornerParkPath);
    }

    @Override
    public void start()
    {
        addTask(cornerParkTask);
    }

}

