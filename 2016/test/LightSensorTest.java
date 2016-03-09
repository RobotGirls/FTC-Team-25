package test;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.LightSensor;

import org.swerverobotics.library.interfaces.Autonomous;

/**
 * Created by Izzie on 2/17/2016.
 */
@Autonomous(name = "TEST Light")
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
        telemetry.addData("Back light: ", back.getLightDetectedRaw());
        telemetry.addData("Front light: ", front.getLightDetectedRaw());
    }
}
