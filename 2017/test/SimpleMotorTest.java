package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Created by jeffb on 10/8/2016.
 */
@Autonomous(name="Simple Motor Test desiree", group="Team 25")
@Disabled
public class SimpleMotorTest extends OpMode {
    private DcMotor motor;

    @Override
    public void init() {
     motor=hardwareMap.dcMotor.get("motor");
    }

    @Override
    public void loop() {
    motor.setPower(1.0);
    }
}
