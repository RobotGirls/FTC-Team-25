
package team25core;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;

/**
 * Created by katie on 12/14/15.
 */
public class ColorSensorTask extends RobotTask {

    public final static float BEACON_THRESHOLD = 287;

    public enum EventKind {
        RED,
        BLUE,
        PURPLE,
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
    protected boolean color;
    protected int channelNumber;
    protected int count;

    public ColorSensorTask(Robot robot, ColorSensor colorSensor, DeviceInterfaceModule cdim, boolean bEnabled, boolean showColor, int channelNumber)
    {
        super(robot);
        this.colorSensor = colorSensor;
        this.cdim = cdim;
        this.bEnabled = bEnabled;
        this.channelNumber = channelNumber;
        this.color = showColor;
    }

    @Override
    public void start() {
        count = 0;
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

        if (color) {
            robot.telemetry.addData("B:", blue);
            robot.telemetry.addData("R:", red);
        }

        if (count < 50) {
            count++;
            return false;
        }

        if ((blue > red) && (blue > BEACON_THRESHOLD)) {
            ColorSensorEvent blueEvent = new ColorSensorEvent(this, EventKind.BLUE);
            robot.queueEvent(blueEvent);
            return true;
        } else if ((red > blue) && (red > BEACON_THRESHOLD)) {
            ColorSensorEvent redEvent = new ColorSensorEvent(this, EventKind.RED);
            robot.queueEvent(redEvent);
            return true;
        } else {
            ColorSensorEvent purpleEvent = new ColorSensorEvent(this, EventKind.PURPLE);
            robot.queueEvent(purpleEvent);
            return true;
        }
    }
}
