package test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki and Elizabeth Wu on 11/22/2016.
 */

@TeleOp (name = "Daisy: Button Pusher Test", group = "Team 25")
@Disabled
public class DaisyButtonPusherTest extends Robot
{
    private Servo leftBP;
    private Servo rightBP;

    private GamepadTask gt;

    public void init()
    {
        leftBP  = hardwareMap.servo.get("leftBP");
        rightBP = hardwareMap.servo.get("rightBP");

        leftBP.setPosition(0.0);
        rightBP.setPosition(0.0);

        gt = new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2);
    }

    public void start()
    {
        this.addTask(gt);
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        GamepadTask.GamepadEvent event = (GamepadTask.GamepadEvent) e;

        if (event.kind == GamepadTask.EventKind.BUTTON_A_DOWN) {
            rightBP.setPosition(0.0);
            leftBP.setPosition(1.0);
        } else if (event.kind == GamepadTask.EventKind.BUTTON_B_DOWN){
            rightBP.setPosition(1.0);
            leftBP.setPosition(0.0);
        }
    }
}
