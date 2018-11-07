package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/*
 * FTC Team 25: Created by Elizabeth Wu, November 06, 2018
 */
@Autonomous(name="Motor Test", group="Team 25")
public class MotorTest extends OpMode {

    private DcMotor motor;

    public MotorTest() {
        super();
    }

    @Override
    public void init() {
        motor = hardwareMap.dcMotor.get("frontRight");
    }

    @Override
    public void loop() {
        motor.setPower(0.5);
    }
}
