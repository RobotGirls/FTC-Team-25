package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import opmodes.Violet;
import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.MechanumGearedDrivetrain;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Elizabeth Wu on 11/14/17.
 */

@Autonomous(name="Violet: Straight Test", group = "Team 25")
public class VioletStraightTest extends Robot {

    private DcMotor frontRight;
    private DcMotor frontLeft;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DeadReckonTask deadReckonTask;
    private final static double STRAIGHT_SPEED = Violet.STRAIGHT_SPEED;
    private final static double TURN_SPEED = Violet.TURN_SPEED;
    private final static int TICKS_PER_INCH = Violet.TICKS_PER_INCH;
    private final static int TICKS_PER_DEGREE = Violet.TICKS_PER_DEGREE;
    private DeadReckonPath deadReckon;
    private MechanumGearedDrivetrain drivetrain;

    @Override
    public void handleEvent(RobotEvent e)
    {
        // Nothing.
    }

    @Override
    public void init()
    {
        frontRight = hardwareMap.dcMotor.get("frontRight");
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        deadReckon = new DeadReckonPath();
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.FORWARD);
        rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        rearRight.setDirection(DcMotorSimple.Direction.FORWARD);

        deadReckon.addSegment(DeadReckonPath.SegmentType.STRAIGHT, 50, STRAIGHT_SPEED);

        int position = frontLeft.getCurrentPosition();
        telemetry.addData("Encoder Position", position);

        drivetrain = new MechanumGearedDrivetrain(Violet.TICKS_PER_INCH, frontRight, rearRight, frontLeft, rearLeft);
    }


    @Override
    public void start()
    {
        deadReckonTask = new DeadReckonTask(this, deadReckon, drivetrain);
        addTask(deadReckonTask);
    }

    public void stop()
    {
        if (deadReckonTask != null) {
            deadReckonTask.stop();
        }
    }
}

