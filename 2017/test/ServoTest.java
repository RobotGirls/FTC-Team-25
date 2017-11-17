package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.Robot;
import team25core.RobotEvent;
import team25core.ServoCalibrateTask;

/**
 * Created by jeffb on 10/8/2016.
 */
@Autonomous(name="Simple Servo Test", group="Team 25")
public class ServoTest extends Robot {
    private Servo servo;

    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    @Override
    public void init()
    {
        servo = hardwareMap.servo.get("s4");
    }

    @Override
    public void start()
    {
        this.addTask(new ServoCalibrateTask(this, servo));
    }
}
