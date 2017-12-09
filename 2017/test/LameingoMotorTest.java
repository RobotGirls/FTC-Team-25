package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.MonitorMotorTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RobotTask;

/**
 * Created by Izzie on 3/19/2016.
 */

@Autonomous(name = "TEST Motor", group = "AutoTest")
public class LameingoMotorTest extends Robot {

    private DcMotor motor;
    private int positionFL;

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {
        motor = hardwareMap.dcMotor.get("frontLeft");
        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void start() {
        motor.setPower(0.5);
        addTask(new MonitorMotorTask(this, motor));
    }

}
