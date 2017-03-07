package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;

import team25core.Robot;
import team25core.RobotEvent;

/**
 * Created by Elizabeth on 12/27/2016.
 */
@Autonomous(name = "COLOR Test", group="AutoTeam25")
@Disabled
public class ColorTest2 extends Robot {

    private DeviceInterfaceModule core;
    private ColorSensor color;

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {
        core = hardwareMap.deviceInterfaceModule.get("cdim");
        color = hardwareMap.colorSensor.get("color");

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
