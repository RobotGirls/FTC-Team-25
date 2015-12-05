package team25core;

import com.qualcomm.hardware.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.GyroSensor;

/**
 * Created by katie on 11/14/15.
 */
public class ColorSensorTask extends RobotTask {

    public enum EventKind {
        RED,
        BLUE,
        OTHER,
    }

    public class ColorSensorEvent extends RobotEvent {
        public EventKind kind;

        public ColorSensorEvent(RobotTask task, EventKind kind) {
            super(task);
            this.kind = kind;
        }
    }

    protected ColorSensor colorSensor;
    protected DeviceInterfaceModule cdim;
    protected boolean bEnabled;
    protected int channelNumber;

    public ColorSensorTask(Robot robot, ColorSensor colorSensor, DeviceInterfaceModule cdim, boolean bEnabled, int channelNumber)
    {
        super(robot);
        this.colorSensor = colorSensor;
        this.cdim = cdim;
        this.bEnabled = bEnabled;
        this.channelNumber = channelNumber;
    }

    @Override
    public void start() {
        if (bEnabled) {
            cdim.setDigitalChannelState(channelNumber, bEnabled);
        }
    }

    @Override
    public void stop() {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice() {
        int blue = colorSensor.blue();
        int red = colorSensor.red();

        if (blue > red) {
            ColorSensorEvent blueEvent = new ColorSensorEvent(this, EventKind.BLUE);
            robot.queueEvent(blueEvent);
            return false;
        } else if (red > blue) {
            ColorSensorEvent redEvent = new ColorSensorEvent(this, EventKind.RED);
            robot.queueEvent(redEvent);
            return false;
        }
        else {
            ColorSensorEvent otherEvent = new ColorSensorEvent(this, EventKind.OTHER);
            robot.queueEvent(otherEvent);
            return false;
        }
    }
}
