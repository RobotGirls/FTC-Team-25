package test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Elizabeth Wu on 9/23/17.
 */

@TeleOp(name = "Glyph Pickup Servo Test")
public class GlyphPickupServoTest extends Robot {

    // Continuous servos work like motors.

    private DcMotor leftTop;
    private DcMotor rightTop;
    private DcMotor leftBottom;
    private DcMotor rightBottom;


    private GamepadTask gt;

    @Override
    public void init()
    {
        leftTop = hardwareMap.dcMotor.get("leftTop");
        rightTop = hardwareMap.dcMotor.get("rightTop");
        leftBottom = hardwareMap.dcMotor.get("leftBottom");
        rightBottom = hardwareMap.dcMotor.get("rightBottom");



        leftTop.setPower(0.0);
        rightTop.setPower(0.0);
        leftBottom.setPower(0.0);
        rightBottom.setPower(0.0);

        gt = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2);
    }


    @Override
    public void start()
    {
        this.addTask(gt);
    }


    @Override
    public void handleEvent(RobotEvent e) {
        GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

        if (event.kind == GamepadTask.EventKind.BUTTON_A_DOWN) {
            leftTop.setPower(1.0);
            leftBottom.setPower(1.0);
            rightTop.setPower(1.0);
            rightBottom.setPower(1.0);
        } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN) {
            leftTop.setPower(0.0);
            leftBottom.setPower(0.0);
            rightTop.setPower(0.0);
            rightBottom.setPower(0.0);
        }

    }

}
