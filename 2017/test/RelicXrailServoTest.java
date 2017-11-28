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

@TeleOp(name = "Relic X-Rail Servo Test")

// Bella's prototype

public class RelicXrailServoTest extends Robot {

    private DcMotor dropper;
    private Servo rotater;

    private GamepadTask gt;


    @Override
    public void init()
    {
        dropper = hardwareMap.dcMotor.get("dropper");
        rotater = hardwareMap.servo.get("rotater");

        dropper.setPower(0.0);
        rotater.setPosition(0.0);

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

        if (event.kind == GamepadTask.EventKind.BUTTON_A_UP) {
            dropper.setPower(0.0);
        } else if (event.kind == GamepadTask.EventKind.BUTTON_A_DOWN) {
            dropper.setPower(1.0);
        }

        if (event.kind == GamepadTask.EventKind.BUTTON_B_UP) {
            rotater.setPosition(0.0);
        } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN) {
            rotater.setPosition(1.0);
        }

    }
}

