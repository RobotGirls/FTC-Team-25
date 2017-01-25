package test;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import opmodes.DaisyConfiguration;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.MecanumGearedDriveDeadReckon;
import team25core.PersistentTelemetryTask;
import team25core.RangeSensorCriteria;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by elizabeth on 1/5/17.
 */

@Autonomous(name = "RANGE Test", group = "Team25")

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
