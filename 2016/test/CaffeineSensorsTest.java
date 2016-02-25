package test;/*
 * FTC Team 25: cmacfarl, February 24, 2016
 */

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

import org.swerverobotics.library.interfaces.Autonomous;

@Autonomous(name = "TEST Sensors")
public class CaffeineSensorsTest extends OpMode {
    public UltrasonicSensor leftUltrasound;
    public UltrasonicSensor rightUltrasound;
    public LightSensor light;
    public ColorSensor color;
    public GyroSensor gyro;

    @Override
    public void init() {
        leftUltrasound = hardwareMap.ultrasonicSensor.get("leftSound");
        rightUltrasound = hardwareMap.ultrasonicSensor.get("rightSound");
        light = hardwareMap.lightSensor.get("light");
        color = hardwareMap.colorSensor.get("color");
        gyro = hardwareMap.gyroSensor.get("gyro");

        gyro.calibrate();
    }

    @Override
    public void loop() {
        light.enableLed(true);
        telemetry.addData("Left ultrasonic: ", leftUltrasound.getUltrasonicLevel());
        telemetry.addData("Right ultrasonic: ", rightUltrasound.getUltrasonicLevel());
        telemetry.addData("NXT Light raw value: ", light.getLightDetectedRaw());
        telemetry.addData("Color red value: ", color.red());
        telemetry.addData("Color blue value: ", color.blue());
        telemetry.addData("Gyro value: ", gyro.getHeading());
    }
}
