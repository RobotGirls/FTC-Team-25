package test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.LightSensor;

import org.swerverobotics.library.interfaces.Autonomous;

/**
 * Created by Izzie on 2/17/2016.
 */
@Autonomous(name = "TEST Light")
public class LightSensorTest extends OpMode{

    LightSensor sensor;

    @Override
    public void init()
    {
        sensor = hardwareMap.lightSensor.get("light");
        sensor.enableLed(true);
    }

    @Override
    public void loop()
    {
        telemetry.addData("Light: ", sensor.getLightDetectedRaw());
    }
}
