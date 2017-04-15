package example;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.GamepadTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

@TeleOp(name = "Single Shot Timer Task Example", group = "Team25")
@Disabled
public class SingleShotTimerTaskExample extends Robot
{
    private Servo arm;
    private SingleShotTimerTask sst;
    private int time = 1000;
    private boolean displaced = false;

    @Override
    public void init()
    {
        arm = hardwareMap.servo.get("left");
        arm.setPosition(1);
        sst = new SingleShotTimerTask(this, time);
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        // When sst expires:
        if (displaced) {
            arm.setPosition(1);
            displaced = false;
        } else {
            arm.setPosition(0);
            displaced = true;
        }

        time += 1000;
        this.addTask(new SingleShotTimerTask(this, time));
    }

    @Override
    public void start()
    {
        super.start();
        this.addTask(sst);
    }
}
