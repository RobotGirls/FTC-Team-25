package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.DeadReckonPath;
import team25core.DeadReckonTask;
import team25core.GamepadTask;
import team25core.MechanumGearedDrivetrain;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RunToEncoderValueTask;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/5/2016.
 */
@Autonomous(name = "DAISY Launch", group = "Team25")
@Disabled
public class DaisyLaunchAutonomous extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor launcher;
    private DcMotor conveyor;
    private Servo capServo;
    private DeadReckonTask deadReckonTask;
    private RunToEncoderValueTask runToPositionTask;
    private SingleShotTimerTask stt;
    private boolean launched;
    private PersistentTelemetryTask ptt;
    private DeadReckonPath path;
    private final int TICKS_PER_INCH = Daisy.TICKS_PER_INCH;
    private final int TICKS_PER_DEGREE = Daisy.TICKS_PER_DEGREE;
    private final double STRAIGHT_SPEED = Daisy.STRAIGHT_SPEED;
    private final double TURN_SPEED = Daisy.TURN_SPEED;
    private final int LAUNCH_POSITION = Daisy.LAUNCH_POSITION;
    private int turnMultiplier = 1;
    private int parkSelection = 0;
    private MechanumGearedDrivetrain drivetrain;

    private AutonomousPath pathChoice = AutonomousPath.CAP_BALL;

    public enum Alliance {
        RED,
        BLUE,
    }

    public enum AutonomousPath {
        CORNER_PARK,
        CENTER_PARK,
        CAP_BALL,
        LAUNCH,
        STAY,
    }

    public enum AutonomousAction {
        LAUNCH_1,
        LAUNCH_2,
        BEACON_1,
        BEACON_2,
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GamepadTask.GamepadEvent) {
            GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

            if (event.kind == GamepadTask.EventKind.LEFT_BUMPER_DOWN) {
                filterParkSelection();
            }
        }

        if (e instanceof SingleShotTimerTask.SingleShotTimerEvent) {
            conveyor.setPower(0);
            addTask(runToPositionTask);
        } else if (e instanceof RunToEncoderValueTask.RunToEncoderValueEvent) {
            RunToEncoderValueTask.RunToEncoderValueEvent event = (RunToEncoderValueTask.RunToEncoderValueEvent) e;
            if (event.kind == RunToEncoderValueTask.EventKind.DONE) {
                if (!launched) {
                    conveyor.setPower(0.5);
                    addTask(stt);
                    launched = true;
                } else {
                    addTask(deadReckonTask);
                }
            }
        }
    }

    private void filterParkSelection()
    {
        if (parkSelection == 0) {
            pathChoice = AutonomousPath.CENTER_PARK;
            ptt.addData("PARK", "Center Park");
            parkSelection = 1;
        } else {
            pathChoice = AutonomousPath.STAY;
            ptt.addData("PARK", "Stay");
            parkSelection = 0;
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

    private DeadReckonPath pathSetup(AutonomousPath pathChoice)
    {
        DeadReckonPath path = new DeadReckonPath();

        if (pathChoice == AutonomousPath.CENTER_PARK) {
            path.addSegment(DeadReckonPath.SegmentType.STRAIGHT,  87, STRAIGHT_SPEED);
        } else if (pathChoice == AutonomousPath.STAY) {
            path.addSegment(DeadReckonPath.SegmentType.STRAIGHT,   0, STRAIGHT_SPEED);
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
        launcher   = hardwareMap.dcMotor.get("launcher");
        conveyor   = hardwareMap.dcMotor.get("conveyor");
        capServo   = hardwareMap.servo.get("capServo");

        capServo.setPosition(1.0);

        runToPositionTask = new RunToEncoderValueTask(this, launcher, LAUNCH_POSITION, 1.0);

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        launcher.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        launcher.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        drivetrain = new MechanumGearedDrivetrain(Daisy.TICKS_PER_INCH, frontRight, rearRight, frontLeft, rearLeft);

        stt = new SingleShotTimerTask(this, 1000);
        launched = false;

        // Telemetry setup.
        ptt = new PersistentTelemetryTask(this);
        this.addTask(ptt);

        ptt.addData("Press (LEFT BUMPER) to select", "Park!");

        /*
        ptt.addData("Press (X) to select", "Blue alliance!");
        ptt.addData("Press (B) to select", "Red alliance!");
        ptt.addData("Press (LEFT TRIGGER) to select", "Center Park!");
        ptt.addData("Press (RIGHT TRIGGER) to select", "Just launch!");
        ptt.addData("Press (LEFT BUMPER) to select", "Launch!");
        */


        // Alliance selection.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));
    }

    @Override
    public void start()
    {
        path = pathSetup(pathChoice);
        deadReckonTask = new DeadReckonTask(this, path, drivetrain);

        addTask(runToPositionTask);
        //addTask(deadReckonTask);
    }
}
