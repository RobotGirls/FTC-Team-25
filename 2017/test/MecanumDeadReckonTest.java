package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import opmodes.DaisyConfiguration;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.MecanumGearedDriveDeadReckon;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 12/13/2016.
 */

@Autonomous(name = "Daisy: Mecanum Autonomous Test", group = "Team 25")
public class MecanumDeadReckonTest extends Robot
{
    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor rearLeft;
    DcMotor rearRight;
    final int TICKS_PER_INCH = DaisyConfiguration.TICKS_PER_INCH;
    final int TICKS_PER_DEGREE = DaisyConfiguration.TICKS_PER_DEGREE;
    MecanumGearedDriveDeadReckon path;

    @Override
    public void handleEvent(RobotEvent e)
    {
       // Nothing.
    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        path = new MecanumGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        path.addSegment(DeadReckon.SegmentType.SIDEWAYS, 5, 1.0);
    }

    @Override
    public void start()
    {
        this.addTask(new DeadReckonTask(this, path));
    }
}
