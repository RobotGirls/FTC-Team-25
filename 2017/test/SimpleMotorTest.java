package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.MonitorMotorTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by jeffb on 10/8/2016.
 */
@Autonomous(name="Simple Motor Test", group="Team 25")
public class SimpleMotorTest extends Robot {
    private DcMotor motor;

    @Override
    public void handleEvent(RobotEvent e)
    {

    }

    @Override
    public void init()
    {
        motor = hardwareMap.dcMotor.get("claw");
    }

    @Override
    public void start()
    {
        motor.setPower(0.2);
        this.addTask(new MonitorMotorTask(this, motor));
    }
}
