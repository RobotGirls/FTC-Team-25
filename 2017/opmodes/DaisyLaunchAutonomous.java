package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDriveDeadReckon;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 12/2/2016.
 */

@Autonomous(name = "Daisy: Launch Autononomous", group = "Team25")
@Disabled
public class DaisyLaunchAutonomous extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor launcher;
    private DeadReckonTask deadReckonTask;
    private RunToEncoderValueTask runToPositionTask;
    private SingleShotTimerTask stt;
    private FourWheelDirectDriveDeadReckon path;
    private final int TICKS_PER_INCH = DaisyConfiguration.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = DaisyConfiguration.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = DaisyConfiguration.STRAIGHT_SPEED;
    private final double TURN_SPEED = DaisyConfiguration.TURN_SPEED;
    private final int LAUNCH_POSITION = DaisyConfiguration.LAUNCH_POSITION;
    private final int STARTING_LAUNCH_POSITION = DaisyConfiguration.STARTING_LAUNCH_POSITION;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof SingleShotTimerTask.SingleShotTimerEvent) {
           launcher.setTargetPosition(LAUNCH_POSITION);
        }
    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");
        launcher = hardwareMap.dcMotor.get("launcher");

        path = new FourWheelDirectDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontRight, rearRight, frontLeft, rearLeft);
        deadReckonTask = new DeadReckonTask(this, path);

        runToPositionTask = new RunToEncoderValueTask(this, launcher, null, LAUNCH_POSITION, 1.0);

        stt = new SingleShotTimerTask(this, 1000);
    }

    @Override
    public void start()
    {
        addTask(deadReckonTask);
        addTask(runToPositionTask);
        //addTask(stt);
    }
}
