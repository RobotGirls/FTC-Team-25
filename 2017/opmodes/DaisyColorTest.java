package opmodes;

import com.qualcomm.hardware.ams.AMSColorSensor;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.I2cDeviceImpl;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.AMSColorSensorImproved;
import team25core.ColorSensorTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 2/25/2017.
 */
@Autonomous(name = "TEST Color", group = "Team 25")
@Disabled
public class DaisyColorTest extends Robot
{
    DeviceInterfaceModule cdim;
    AMSColorSensorImproved colorSensor;


    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void init() {
        cdim = hardwareMap.deviceInterfaceModule.get("cdim");
        // colorSensor = AMSColorSensorImproved.create(AMSColorSensor.Parameters.createForAdaFruit(), new I2cDeviceImpl(cdim, Daisy.COLOR_PORT));
        this.telemetry.addData("RED", colorSensor.red());
        this.telemetry.addData("BLUE", colorSensor.blue());

        super.init();
        ColorSensorTask colorSensorTask = new ColorSensorTask(this, colorSensor, cdim, false, Daisy.COLOR_PORT) {
            @Override
            public void handleEvent(RobotEvent e) {
                ColorSensorTask.ColorSensorEvent event = (ColorSensorTask.ColorSensorEvent) e;
                switch (event.kind) {
                    case YES:
                        RobotLog.i("141 Color sensed");
                        break;
                    case NO:
                        RobotLog.i("141 Color not sensed");
                        break;
                }
            }
        };

        colorSensorTask.setModeSingle(ColorSensorTask.TargetColor.RED, 1760);
        colorSensorTask.setMsDelay(Daisy.COLOR_MS_DELAY);
        colorSensorTask.setReflectColor(true, hardwareMap);

        cdim.setDigitalChannelMode(Daisy.COLOR_PORT, DigitalChannelController.Mode.OUTPUT);
        cdim.setDigitalChannelState(Daisy.COLOR_PORT, false);

        addTask(colorSensorTask);
    }

    @Override
    public void start()
    {
        super.start();
    }
}
