package opmodes;
/*
 * FTC Team 25: elizabeth, December 21, 2018
 */

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckonPath;
import team25core.DeadmanMotorTask;
import team25core.GamepadTask;
import team25core.MotorStallTask;
import team25core.Robot;
import team25core.RobotEvent;


@Autonomous(name = "Reset Lilac", group = "Team 25")
public class ResetLilac extends Robot {

    private static String TAG = "ResetLilac";

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor latchArm;
    private MotorStallTask motorStallTask;

    @Override
    public void init()
    {
        latchArm = hardwareMap.dcMotor.get("latchArm");

        latchArm.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        latchArm.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        this.addTask(new DeadmanMotorTask(this, latchArm, 0.5, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.LEFT_BUMPER));
        this.addTask(new DeadmanMotorTask(this, latchArm, -0.5, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.LEFT_TRIGGER));
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof MotorStallTask.MotorStallEvent) {
            MotorStallTask.MotorStallEvent event = (MotorStallTask.MotorStallEvent) e;
            if (event.kind == MotorStallTask.EventKind.STALLED) {
                RobotLog.i("Stalled");
                latchArm.setPower(0.0);
                motorStallTask.stop();
            }
        }
    }

    @Override
    public void start()
    {
        latchArm.setPower(-0.5);
        motorStallTask = new MotorStallTask(this, latchArm, telemetry);
        this.addTask(motorStallTask);

    }
}
