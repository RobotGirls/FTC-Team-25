package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import opmodes.Daisy;
import team25core.DeadReckonPath;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 12/13/2016.
 */

@Autonomous(name = "Daisy: Mecanum Autonomous Test", group = "Team 25")
@Disabled
public class MecanumDeadReckonTest extends Robot
{
    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor rearLeft;
    DcMotor rearRight;
    final int TICKS_PER_INCH = Daisy.TICKS_PER_INCH;
    final int TICKS_PER_DEGREE = Daisy.TICKS_PER_DEGREE;
    DeadReckonPath path;
    MechanumGearedDrivetrain drivetrain;

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

        drivetrain = new MechanumGearedDrivetrain(TICKS_PER_INCH, frontRight, rearRight, frontLeft, rearLeft);

        path = new DeadReckonPath();
        path.addSegment(DeadReckonPath.SegmentType.SIDEWAYS, 5, 1.0);
    }

    @Override
    public void start()
    {
        this.addTask(new DeadReckonTask(this, path, drivetrain));
    }
}
