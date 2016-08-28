package test;/*
 * FTC Team 25: cmacfarl, February 24, 2016
 */

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.UltrasonicSensor;

@Autonomous(name = "TEST Sensors")
public class CaffeineSensorsTest extends OpMode {
    public UltrasonicSensor leftUltrasound;
    public UltrasonicSensor rightUltrasound;
    public LightSensor backLight;
    public LightSensor frontLight;
    public ColorSensor color;
    public GyroSensor gyro;

    @Override
    public void init() {
        leftUltrasound = hardwareMap.ultrasonicSensor.get("leftSound");
        rightUltrasound = hardwareMap.ultrasonicSensor.get("rightSound");
        frontLight = hardwareMap.lightSensor.get("frontLight");
        frontLight.enableLed(true);
        backLight = hardwareMap.lightSensor.get("backLight");
        backLight.enableLed(true);
        color = hardwareMap.colorSensor.get("color");
        gyro = hardwareMap.gyroSensor.get("gyro");

        gyro.calibrate();
    }

    @Override
    public void loop() {
        backLight.enableLed(true);
        telemetry.addData("NXT Left ultrasonic: ", leftUltrasound.getUltrasonicLevel());
        telemetry.addData("NXT Right ultrasonic: ", rightUltrasound.getUltrasonicLevel());
        telemetry.addData("NXT Front light raw: ", frontLight.getLightDetectedRaw());
        telemetry.addData("NXT Back light raw: ", backLight.getLightDetectedRaw());
        telemetry.addData("Color red value: ", color.red());
        telemetry.addData("Color blue value: ", color.blue());
        telemetry.addData("Gyro value: ", gyro.getHeading());
    }
}
