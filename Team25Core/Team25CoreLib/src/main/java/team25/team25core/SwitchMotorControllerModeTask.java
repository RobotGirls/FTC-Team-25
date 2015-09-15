package team25.team25core;


import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.RobotLog;

/*
 * FTC Team 25: cmacfarl, September 03, 2015
 */
public abstract class SwitchMotorControllerModeTask extends RobotTask {

    public enum EventKind {
        DONE,
    }

    public class SwitchMotorControllerModeEvent extends RobotEvent {

        EventKind kind;
        DcMotorController.DeviceMode mode;

        public SwitchMotorControllerModeEvent(RobotTask task, EventKind kind, DcMotorController.DeviceMode mode)
        {
            super(task);
            this.kind = kind;
            this.mode = mode;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "Switch Mode Event " + kind);
        }
    }

    protected DcMotorController.DeviceMode mode;
    protected DcMotorController controller;

    public SwitchMotorControllerModeTask(Robot robot, DcMotorController controller, DcMotorController.DeviceMode mode)
    {
        super(robot);

        this.mode = mode;
        this.controller = controller;
    }

    @Override
    public void start()
    {
        RobotLog.i("SwitchMotorControllerModeTask: start");
    }

    @Override
    public void stop()
    {
        robot.removeTask(this);
        RobotLog.i("SwitchMotorControllerModeTask: stop");
    }

    @Override
    public boolean timeslice()
    {
        DcMotorController.DeviceMode currMode;

        currMode = controller.getMotorControllerDeviceMode();
        if (currMode == mode) {
            robot.queueEvent(new SwitchMotorControllerModeEvent(this, EventKind.DONE, mode));
            return true;
        } else {
            switch (currMode) {
            case SWITCHING_TO_READ_MODE:
            case SWITCHING_TO_WRITE_MODE:
                /*
                 * Wait...
                 */
                break;
            case READ_ONLY:
                controller.setMotorControllerDeviceMode(DcMotorController.DeviceMode.WRITE_ONLY);
                break;
            case WRITE_ONLY:
                controller.setMotorControllerDeviceMode(DcMotorController.DeviceMode.READ_ONLY);
                break;
            case READ_WRITE:
                break;
            }
        }
        return false;
    }
}
