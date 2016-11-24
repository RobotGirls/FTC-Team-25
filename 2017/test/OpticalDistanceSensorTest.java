package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

/**
 * Created by Katelyn Biesiadecki on 11/12/2016.
 */
@Autonomous(name = "ODS Test")
public class OpticalDistanceSensorTest extends OpMode{

    OpticalDistanceSensor front;
    OpticalDistanceSensor back;

    @Override
    public void init()
    {
        front = hardwareMap.opticalDistanceSensor.get("frontLight");
        back = hardwareMap.opticalDistanceSensor.get("backLight");
    }

    @Override
    public void loop()
    {
        telemetry.addData("Back light: ", back.getRawLightDetected());
        telemetry.addData("Front light: ", front.getRawLightDetected());
    }
}
