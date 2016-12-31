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
@Autonomous(name = "Daisy: Park Autonomous", group = "Team25")
public class DaisyParkAutonomous extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor launcher;
    private DeadReckonTask deadReckonTask;
    private PersistentTelemetryTask ptt;
    private FourWheelGearedDriveDeadReckon deadReckonPath;
    private final int TICKS_PER_INCH = DaisyConfiguration.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = DaisyConfiguration.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = DaisyConfiguration.STRAIGHT_SPEED;
    private final double TURN_SPEED = DaisyConfiguration.TURN_SPEED;
    private int turnMultiplier = 1;

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
                ptt.addData("ALLIANCE", "Blue");
            } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN) {
                selectAlliance(Alliance.RED);
                ptt.addData("ALLIANCE", "Red");
            } else if (event.kind == GamepadTask.EventKind.LEFT_TRIGGER_DOWN) {
                pathChoice = AutonomousPath.CORNER_PARK;
                ptt.addData("AUTONOMOUS", "Corner Park");
            } else if (event.kind == GamepadTask.EventKind.RIGHT_TRIGGER_DOWN) {
                pathChoice = AutonomousPath.CENTER_PARK;
                ptt.addData("AUTONOMOUS", "Center Park");
            }
        }
    }

    private void selectAlliance(Alliance color)
    {
        if (color == Alliance.BLUE) {
            // Do blue setup.
            turnMultiplier = -1;
        } else {
            // Do red setup.
            turnMultiplier = 1;
        }
    }

    private FourWheelGearedDriveDeadReckon pathSetup(AutonomousPath pathChoice)
    {
        FourWheelGearedDriveDeadReckon path = new FourWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE,
                frontLeft, frontRight, rearLeft, rearRight);

        if (pathChoice == AutonomousPath.CORNER_PARK) {
            path.addSegment(DeadReckon.SegmentType.STRAIGHT,  58, STRAIGHT_SPEED);
            path.addSegment(DeadReckon.SegmentType.TURN,     120, TURN_SPEED * turnMultiplier);
            path.addSegment(DeadReckon.SegmentType.STRAIGHT,  85, STRAIGHT_SPEED);
        } else if (pathChoice == AutonomousPath.CENTER_PARK) {
            path.addSegment(DeadReckon.SegmentType.STRAIGHT,  60, STRAIGHT_SPEED);
        } else if (pathChoice == AutonomousPath.CAP_BALL) {
            path.addSegment(DeadReckon.SegmentType.STRAIGHT,  60, STRAIGHT_SPEED);
        } else if (pathChoice == AutonomousPath.LAUNCH) {
            path.addSegment(DeadReckon.SegmentType.STRAIGHT,   0, STRAIGHT_SPEED);
        }

        return path;
    }

    @Override
    public void init()
    {
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft   = hardwareMap.dcMotor.get("rearLeft");
        rearRight  = hardwareMap.dcMotor.get("rearRight");

        // Telemetry setup.
        ptt = new PersistentTelemetryTask(this);
        this.addTask(ptt);
        ptt.addData("Press (X) to select", "Blue alliance!");
        ptt.addData("Press (B) to select", "Red alliance!");
        ptt.addData("Press (LEFT TRIGGER) to select", "Corner Park!");
        ptt.addData("Press (RIGHT TRIGGER) to select", "Center Park!");

        // Alliance selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));
    }

    @Override
    public void start()
    {
        deadReckonPath = pathSetup(pathChoice);
        deadReckonTask = new DeadReckonTask(this, deadReckonPath);
        addTask(deadReckonTask);
    }
}
