package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.FourWheelDirectDriveDeadReckon;
import team25core.FourWheelGearedDriveDeadReckon;
import team25core.GamepadTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/5/2016.
 */
@Autonomous(name = "Daisy: Autonomous", group = "Team25")
public class DaisyAutonomous extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DeadReckonTask deadReckonTask;
    private PersistentTelemetryTask ptt;
    private FourWheelGearedDriveDeadReckon deadReckonPath;
    private final int TICKS_PER_INCH = DaisyConfiguration.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = DaisyConfiguration.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = DaisyConfiguration.STRAIGHT_SPEED;
    private final double TURN_SPEED = DaisyConfiguration.TURN_SPEED;

    private AutonomousPath pathChoice = AutonomousPath.CAP_BALL;

    public enum Alliance {
        RED,
        BLUE
    }

    public enum AutonomousPath {
        CORNER_PARK,
        CENTER_PARK,
        CAP_BALL,
        LAUNCH,
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            if (event.kind == GamepadTask.EventKind.BUTTON_X_DOWN) {
                selectAlliance(Alliance.BLUE);
            } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN) {
                selectAlliance(Alliance.RED);
            } else if (event.kind == GamepadTask.EventKind.LEFT_BUMPER_DOWN) {
                // Do corner park.
                pathChoice = AutonomousPath.CORNER_PARK;
                ptt.addData("AUTONOMOUS", "Corner Park");
            } else if (event.kind == GamepadTask.EventKind.LEFT_TRIGGER_DOWN) {
               // Do launch.
                pathChoice = AutonomousPath.LAUNCH;
                ptt.addData("AUTONOMOUS", "Launch");
            } else if (event.kind == GamepadTask.EventKind.RIGHT_BUMPER_DOWN) {
               // Do cap ball.
                pathChoice = AutonomousPath.CAP_BALL;
                ptt.addData("AUTONOMOUS", "Cap Ball");
            } else if (event.kind == GamepadTask.EventKind.RIGHT_TRIGGER_DOWN) {
               // Do center park.
                pathChoice = AutonomousPath.CENTER_PARK;
                ptt.addData("AUTONOMOUS", "Center Park");
            }
        }
    }

    private void selectAlliance(Alliance color)
{
    if (color == Alliance.BLUE) {
        // Do blue setup.
        ptt.addData("ALLIANCE", "Blue");
    } else {
        // Do red setup.
        ptt.addData("ALLIANCE", "Red");
    }
}

    private FourWheelGearedDriveDeadReckon cornerParkSetup()
    {
        FourWheelGearedDriveDeadReckon cornerParkPath = new FourWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE,
                frontLeft, frontRight, rearLeft, rearRight);
        cornerParkPath.addSegment(DeadReckon.SegmentType.STRAIGHT, 52, STRAIGHT_SPEED);
        cornerParkPath.addSegment(DeadReckon.SegmentType.TURN, 120, TURN_SPEED);
        cornerParkPath.addSegment(DeadReckon.SegmentType.STRAIGHT, 32, STRAIGHT_SPEED);

        return cornerParkPath;
    }

    private FourWheelGearedDriveDeadReckon centerParkSetup()
    {
       FourWheelGearedDriveDeadReckon centerParkPath = new FourWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE,
               frontLeft, frontRight, rearLeft, rearRight);
        centerParkPath.addSegment(DeadReckon.SegmentType.STRAIGHT, 66, STRAIGHT_SPEED);

        return centerParkPath;
    }
   private FourWheelGearedDriveDeadReckon capBallSetup()
   {
      FourWheelGearedDriveDeadReckon cbPath = new FourWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, frontLeft,
              frontRight,rearLeft, rearRight);
       cbPath.addSegment(DeadReckon.SegmentType.STRAIGHT, 62, STRAIGHT_SPEED);

       return cbPath;
   }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        if (pathChoice == AutonomousPath.CORNER_PARK) {
            deadReckonPath = cornerParkSetup();
        } else if (pathChoice == AutonomousPath.CENTER_PARK) {
            deadReckonPath = centerParkSetup();
        } else if (pathChoice == AutonomousPath.CAP_BALL) {
           deadReckonPath = capBallSetup();
        } else {
           // launchSetup();
        }

        // Telemetry setup.
        ptt = new PersistentTelemetryTask(this);
        this.addTask(ptt);
        ptt.addData("Press (x) to select", "BLUE alliance!");
        ptt.addData("Press (b) to select", "RED alliance!");
        ptt.addData("Press (Left Bumper) to select", "Corner Park");
        ptt.addData("Press (Right Trigger) to select", "Center Park");
        ptt.addData("Press (Right Bumper) to select", "Cap Ball");
        ptt.addData("Press (Left Trigger) to select", "Launch");

        // Alliance selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));
    }

    @Override
    public void start()
    {
        deadReckonTask = new DeadReckonTask(this, deadReckonPath);
        addTask(deadReckonTask);
    }
}
