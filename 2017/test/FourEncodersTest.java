package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.LightSensor;

import team25core.PersistentTelemetryTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/19/2016.
 */
@Autonomous(name = "TEST Encoder", group="Team25")
@Disabled
public class FourEncodersTest extends OpMode{

    private DcMotor frontLeft;
    private DcMotor rearLeft;
    private DcMotor frontRight;
    private DcMotor rearRight;

    private int positionFL;
    private int positionFR;
    private int positionRL;
    private int positionRR;


    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearRight = hardwareMap.dcMotor.get("rearRight");

        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void loop()
    {
        frontLeft.setPower(0.2);
        frontRight.setPower(0.2);
        rearLeft.setPower(0.2);
        rearRight.setPower(0.2);

        positionFL = Math.abs(frontLeft.getCurrentPosition());
        telemetry.addData("Front Left Position: ", positionFL);
        positionFR = Math.abs(frontRight.getCurrentPosition());
        telemetry.addData("Front Right Position: ", positionFR);
        positionRL = Math.abs(rearLeft.getCurrentPosition());
        telemetry.addData("Rear Left Position: ", positionRL);
        positionRR = Math.abs(rearRight.getCurrentPosition());
        telemetry.addData("Rear Right Position: ", positionRR);
    }
}
