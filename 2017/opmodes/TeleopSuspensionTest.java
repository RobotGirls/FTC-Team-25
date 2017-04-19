package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.GamepadTask;
import team25core.MecanumWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 3/21/2017.
 */
@TeleOp(name = "Teleop Suspension Test", group = "Team 25")
@Disabled
public class TeleopSuspensionTest extends Robot
{
    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor rearLeft;
    DcMotor rearRight;
    MecanumWheelDriveTask drive;

    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");
    }

    @Override
    public void start()
    {
        drive = new MecanumWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);
        this.addTask(drive);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1)
        {
           @Override
           public void handleEvent(RobotEvent e)
           {
                GamepadEvent event = (GamepadEvent) e;
               if (event.kind == EventKind.BUTTON_A_DOWN) {
                   drive.suspendTask(false);
               } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                   drive.suspendTask(true);
               }
           }
        });
    }

    @Override
    public void handleEvent(RobotEvent e)
    {

    }
}
