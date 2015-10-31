
package team25core;

/*
 * FTC Team 25: izzielau, October 17, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;

public class LimitSwitchTask extends RobotTask {
    protected DeviceInterfaceModule module;
    protected DcMotor motor;
    protected int port;

    public String status;

    // Constructor.
    public LimitSwitchTask(Robot robot, DeviceInterfaceModule module, int limitPort)
    {
        super(robot);

        this.module = module;
        this.port = limitPort;

        this.limitState = new SwitchState();
        this.limitState.switch_closed = false;
        this.limitState.switch_open = false;
        this.limitState.switch_unknown = true;
    }

    // Instance of SwitchState.
    protected SwitchState limitState;

    // Class: boolean limit states.
    public class SwitchState {
        public boolean switch_open;
        public boolean switch_closed;
        public boolean switch_unknown;
    }

    // Class: events.
    public class LimitSwitchEvent extends RobotEvent {
        public EventKind kind;

        public LimitSwitchEvent(RobotTask task, EventKind k)
        {
            super(task);
            kind = k;
        }
    }

    // Enumeration: events.
    public enum EventKind {
        OPEN,
        CLOSED,
        UNKNOWN,
    }

    public boolean limitSwitchClosed()
    {
        int inputByte = module.getDigitalInputStateByte();
        int targetByte = (0x01) << port;

        if ((targetByte & inputByte) == 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void start()
    {
        byte initial = module.getDigitalIOControlByte();
        byte shift   = (byte)(initial << port);
        byte inverse = (byte)~shift;
        byte result  = (byte)(initial & inverse);
        module.setDigitalIOControlByte(result);
    }

    @Override
    public void stop()
    {
        // Remove task.
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice() {
        // If-else statements.
        if ((!limitState.switch_closed) && limitSwitchClosed()) {
            robot.queueEvent(new LimitSwitchEvent(this, EventKind.CLOSED));
            limitState.switch_closed = true;
        } else if ((limitState.switch_closed) && !limitSwitchClosed()) {
            robot.queueEvent(new LimitSwitchEvent(this, EventKind.OPEN));
            limitState.switch_closed = false;
        }

        // This task doesn't stop.
        return false;
    }
}
