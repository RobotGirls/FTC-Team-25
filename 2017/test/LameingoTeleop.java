package test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TankDriveTask;
import team25core.TwoWheelDirectDrivetrain;
import team25core.TwoWheelDriveTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/12/2016.
 */

@TeleOp(name="Lameingo: Teleop", group="Team25")
@Disabled
public class LameingoTeleop extends Robot {

    DcMotor left;
    DcMotor right;
    Servo leftPusher;
    Servo rightPusher;
    TwoWheelDirectDrivetrain drivetrain;

    @Override
    public void handleEvent(RobotEvent e)
    {
        GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;
        if (event.kind == GamepadTask.EventKind.BUTTON_A_DOWN) {
            // stows
            leftPusher.setPosition(0);
        } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN) {
            // pushes
            leftPusher.setPosition(1);
        } else if (event.kind == GamepadTask.EventKind.BUTTON_X_DOWN) {
            // pushes
            rightPusher.setPosition(0);
        } else if (event.kind == GamepadTask.EventKind.BUTTON_Y_DOWN) {
            // stows
            rightPusher.setPosition(1);
        }
    }

    @Override
    public void init()
    {
        left = hardwareMap.dcMotor.get("leftMotor");
        right = hardwareMap.dcMotor.get("rightMotor");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightPusher = hardwareMap.servo.get("rightPusher");

        drivetrain = new TwoWheelDirectDrivetrain(LameingoConfiguration.TICKS_PER_INCH, right, left);
    }

    @Override
    public void start()
    {
        this.addTask(new TankDriveTask(this, drivetrain));
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1));
    }

}
