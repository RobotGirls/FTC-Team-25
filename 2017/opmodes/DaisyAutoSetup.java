package opmodes;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cRangeSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

import team25core.ColorSensorTask;
import team25core.DeadmanMotorTask;
import team25core.GamepadTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 12/3/2016.
 */

@Autonomous(name = "Daisy: Setup for Autonomous", group = "Team25")
public class DaisyAutoSetup extends Robot
{

    private DcMotor launcher;
    private DcMotor conveyor;
    private ColorSensor colorSensor;
    private DistanceSensor rangeSensor;
    private GyroSensor gyroSensor;
    private OpticalDistanceSensor frontOds;
    private DeviceInterfaceModule cdim;
    private DeadmanMotorTask runLauncherBackTask;
    private DeadmanMotorTask runLauncherForwardTask;
    private DeadmanMotorTask runConveyorForwardTask;
    private DeadmanMotorTask runConveyorBackTask;
    private PersistentTelemetryTask ptt;

    @Override
    public void handleEvent(RobotEvent e)
    {
       // Nothing.
    }

    @Override
    public void init()
    {
        conveyor    = hardwareMap.dcMotor.get("conveyor");
        launcher    = hardwareMap.dcMotor.get("launcher");
        colorSensor = hardwareMap.colorSensor.get("color");
        rangeSensor = hardwareMap.get(ModernRoboticsI2cRangeSensor.class, "range");
        gyroSensor  = hardwareMap.gyroSensor.get("gyroSensor");
        cdim        = hardwareMap.deviceInterfaceModule.get("cdim");
        frontOds    = hardwareMap.opticalDistanceSensor.get("frontLight");

        runLauncherForwardTask = new DeadmanMotorTask(this, launcher,  0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_Y);
        runLauncherBackTask    = new DeadmanMotorTask(this, launcher, -0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_A);
        runConveyorForwardTask = new DeadmanMotorTask(this, conveyor,  0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_X);
        runConveyorBackTask    = new DeadmanMotorTask(this, conveyor, -0.1, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.BUTTON_B);

        gyroSensor.calibrate();
        ptt = new PersistentTelemetryTask(this);
        this.addTask(ptt);

        ColorSensorTask colorSensorTask = new ColorSensorTask(this, colorSensor, cdim, true, 0) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                ColorSensorEvent event = (ColorSensorEvent) e;
                if (event.kind == EventKind.PURPLE) {
                    ptt.addData("COLOR STATUS", "Not working");
                }
            }
        };
        colorSensorTask.setModeCompare(Daisy.COLOR_THRESHOLD);
        addTask(colorSensorTask);
    }

    @Override
    public void start()
    {
        addTask(runLauncherForwardTask);
        addTask(runLauncherBackTask);
        addTask(runConveyorForwardTask);
        addTask(runConveyorBackTask);
    }

    @Override
    public void loop()
    {
        double distance = rangeSensor.getDistance(DistanceUnit.CM);
        double light    = frontOds.getRawLightDetected();
        double heading  = gyroSensor.getHeading();
        double color    = colorSensor.red();

        ptt.addData("RANGE", String.valueOf(distance));
        ptt.addData("ODS",   String.valueOf(light));
        ptt.addData("GYRO",  String.valueOf(heading));
        ptt.addData("COLOR (R)", String.valueOf(color));
    }
}
