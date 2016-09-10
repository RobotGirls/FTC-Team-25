package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.LightSensor;

/**
 * Created by Izzie on 2/17/2016.
 */
@Autonomous(name = "TEST Light")
@Disabled
public class LightSensorTest extends OpMode{

    LightSensor front;
    LightSensor back;

    @Override
    public void init()
    {
        front = hardwareMap.lightSensor.get("frontLight");
        front.enableLed(true);
        back = hardwareMap.lightSensor.get("backLight");
        back.enableLed(true);
    }

    @Override
    public void loop()
    {
        telemetry.addData("Back light: ", back.getRawLightDetected());
        telemetry.addData("Front light: ", front.getRawLightDetected());
    }
}
