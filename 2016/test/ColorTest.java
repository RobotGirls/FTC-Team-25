package test;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.Servo;

import org.swerverobotics.library.interfaces.Autonomous;
import org.swerverobotics.library.interfaces.Disabled;

import java.util.ServiceConfigurationError;

import opmodes.NeverlandServoConstants;
import team25core.ColorSensorTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by Izzie on 2/10/2016.
 */
@Autonomous(name = "TEST Color", group="AutoTeam25")
@Disabled
public class ColorTest extends Robot {

    private DeviceInterfaceModule core;
    private ColorSensor color;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {
        core = hardwareMap.deviceInterfaceModule.get("interface");
        color = hardwareMap.colorSensor.get("color");
        leftBumper = hardwareMap.servo.get("leftBumper");
        rightBumper = hardwareMap.servo.get("rightBumper");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightPusher = hardwareMap.servo.get("rightPusher");

        leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_DEPLOYED);
        rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);

        core.setDigitalChannelMode(0, DigitalChannelController.Mode.OUTPUT);
        core.setDigitalChannelState(0, false);
    }

    @Override
    public void loop() {
        int red = color.red();
        int blue = color.blue();

        if (red > blue) {
            telemetry.addData("STATE: ", "Red");
        } else if (red < blue) {
            telemetry.addData("STATE: ", "Blue");
        } else {
            telemetry.addData("STATE: ", "Unknown");
        }

        telemetry.addData("RED: ", red);
        telemetry.addData("BLUE: ", blue);
    }
}
