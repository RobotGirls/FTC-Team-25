package test;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DistanceSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import team25core.PersistentTelemetryTask;

/**
 * Created by elizabeth on 1/5/17.
 */

@Autonomous(name = "TEST Range", group = "Team25")
@Disabled
public class RangeSensorTest extends OpMode {

    public DistanceSensor rangeSensor;
    private PersistentTelemetryTask ptt;

    @Override
    public void init()
    {
        rangeSensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "range");
    }

    @Override
    public void start()
    {
    }

    @Override
    public void loop()
    {
        double distance = rangeSensor.getDistance(DistanceUnit.CM);
        telemetry.addData("Distance", Double.toString(distance));
    }
}
