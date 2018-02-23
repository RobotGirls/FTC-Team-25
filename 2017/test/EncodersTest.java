package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * FTC Team 25: Created by Breanna Chan on 2/20/2018.
 */
@Autonomous(name = "TEST Encoder Single", group="Test")
//@Disabled
public class EncodersTest extends OpMode{

    private DcMotor motor;

    private int positionEncoder;


    @Override
    public void init()
    {
        motor = hardwareMap.dcMotor.get("rotate");

        motor.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void loop()
    {
        motor.setPower(0.2);

        positionEncoder = Math.abs(motor.getCurrentPosition());
        telemetry.addData("Motor Position: ", positionEncoder);
    }
}
