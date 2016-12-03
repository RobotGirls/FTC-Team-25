package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.DeadmanMotorTask;
import team25core.FourWheelDirectDriveDeadReckon;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 12/2/2016.
 */

@Autonomous(name = "Daisy: Launch Autononomous", group = "Team25")
public class DaisyLaunchAutonomous extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor launcher;
    private DcMotor conveyor;
    private DeadReckonTask deadReckonTask;
    private RunToEncoderValueTask runToPositionTask;
    private DeadmanMotorTask runLauncherBackTask;
    private DeadmanMotorTask runLauncherForwardTask;
    private SingleShotTimerTask stt;
    private FourWheelDirectDriveDeadReckon path;
    private final int TICKS_PER_INCH = DaisyConfiguration.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = DaisyConfiguration.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = DaisyConfiguration.STRAIGHT_SPEED;
    private final double TURN_SPEED = DaisyConfiguration.TURN_SPEED;
    private final int LAUNCH_POSITION = DaisyConfiguration.LAUNCH_POSITION;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof SingleShotTimerTask.SingleShotTimerEvent) {
            conveyor.setPower(0);
        } else if (e instanceof RunToEncoderValueTask.RunToEncoderValueEvent) {
            RunToEncoderValueTask.RunToEncoderValueEvent event = (RunToEncoderValueTask.RunToEncoderValueEvent) e;
            if (event.kind == RunToEncoderValueTask.EventKind.DONE) {
                conveyor.setPower(0.5);
                addTask(stt);
            }
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
        conveyor = hardwareMap.dcMotor.get("conveyor");

        path = new FourWheelDirectDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontRight, rearRight, frontLeft, rearLeft);
        path.addSegment(DeadReckon.SegmentType.STRAIGHT, 0, STRAIGHT_SPEED);
        path.addSegment(DeadReckon.SegmentType.TURN, 0, TURN_SPEED);
        deadReckonTask = new DeadReckonTask(this, path);

        runToPositionTask = new RunToEncoderValueTask(this, launcher, LAUNCH_POSITION, 1.0);

        launcher.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        runLauncherForwardTask = new DeadmanMotorTask(this, launcher, 0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_Y);
        runLauncherBackTask = new DeadmanMotorTask(this, launcher, -0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_A);

        addTask(runLauncherForwardTask);
        addTask(runLauncherBackTask);

        stt = new SingleShotTimerTask(this, 1000);
    }

    @Override
    public void start()
    {
        // Remove task does not actually remove task, but it should... fix later.
        removeTask(runLauncherForwardTask);
        removeTask(runLauncherBackTask);

        runLauncherForwardTask = new DeadmanMotorTask(this, launcher, 0.0, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_Y);
        runLauncherBackTask = new DeadmanMotorTask(this, launcher, -0.0, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_A);

        addTask(runLauncherForwardTask);
        addTask(runLauncherBackTask);

        addTask(deadReckonTask);
        addTask(runToPositionTask);
    }
}
