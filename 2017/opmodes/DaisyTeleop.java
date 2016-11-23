package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import team25core.FourWheelDriveTask;
import team25core.GamepadTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by Katelyn Biesiadecki on 10/22/2016.
 */

@TeleOp(name = "Daisy Teleop", group = "Team25")
public class DaisyTeleop extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private DcMotor flowerPower;
    private DcMotor conveyor;
    private DcMotor launcher;

    private FourWheelDriveTask drive;
    private PersistentTelemetryTask ptt;

    private final double FLOWER_POWER = 0.5;
    private final double CONVEYOR_POWER = 0.5;

    private boolean slow;

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
        flowerPower = hardwareMap.dcMotor.get("flowerPower");
        conveyor = hardwareMap.dcMotor.get("conveyor");
        launcher = hardwareMap.dcMotor.get("launcher");

        slow = false;

        ptt = new PersistentTelemetryTask(this);
    }

    @Override
    public void start()
    {
        drive = new FourWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);
        this.addTask(drive);

        // Gamepad 2: Mechanism Controller
        // (lt bumper)           (rt bumper)
        //                          (y)
        //  arrow pad            (x)   (b)           (b) flower power (accept)    (x) conveyor belt
        //                          (a)              (a) flower power (reject)

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_A_DOWN) {
                    flowerPower.setPower(1.0);
                    conveyor.setPower(-1.0);
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    flowerPower.setPower(-1.0);
                    conveyor.setPower(1.0);
                } else if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    launcher.setPower(1.0);
                } else {
                    flowerPower.setPower(0.0);
                    conveyor.setPower(0.0);
                    launcher.setPower(0.0);
                }
            }
        });

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_A_DOWN) {
                   // Toggles slowness of motors.
                    if (!slow) {
                       drive.slowDown(true);
                       slow = true;
                        ptt.addData("Slow: ","true");
                   } else {
                       drive.slowDown(false);
                       slow = false;
                        ptt.addData("Slow: ","false");
                   }
                }
            }
        });
    }

    @Override
    public void stop()
    {

    }
}
