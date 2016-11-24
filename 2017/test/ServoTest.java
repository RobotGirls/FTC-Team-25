package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Created by jeffb on 10/8/2016.
 */
@Autonomous(name="Simple Servo Test", group="Team 25")
@Disabled
public class ServoTest extends OpMode
{
    private Servo servo;
    @Override
    public void init() {
    servo=hardwareMap.servo.get("servo");
    }

    @Override
    public void loop() {
    servo.setPosition(0.5);
    }
}
