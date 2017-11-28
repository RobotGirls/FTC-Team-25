package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.FourWheelDirectDrivetrain;
import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Elizabeth Wu on 3/4/17.
 */

@Autonomous(name = "TEST Sideways", group = "Team 25")
@Disabled
public class DaisySidewaysTest extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;

    private FourWheelDirectDrivetrain drivetrain;

    @Override
    public void init()
    {
        frontLeft   = hardwareMap.dcMotor.get("frontLeft");
        frontRight  = hardwareMap.dcMotor.get("frontRight");
        rearLeft    = hardwareMap.dcMotor.get("rearLeft");
        rearRight   = hardwareMap.dcMotor.get("rearRight");
        drivetrain  = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        drivetrain.resetEncoders();
        drivetrain.encodersOn();
        drivetrain.setNoncanonicalMotorDirection();
    }

    @Override
    public void start()
    {
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            @Override
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_A_DOWN) {
                    drivetrain.strafe(-0.7);
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    drivetrain.strafe(0.7);
                } else if (event.kind == EventKind.BUTTON_X_DOWN) {
                    drivetrain.strafe(-0.4);
                } else if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    drivetrain.strafe(0.4);
                } else {
                    drivetrain.stop();
                    drivetrain.logEncoderCounts();
                    drivetrain.resetEncoders();
                    drivetrain.encodersOn();
                }
            }
        });
    }

    @Override
    public void handleEvent(RobotEvent e)
    {

    }
}
