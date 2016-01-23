package team25core;

/*
 * FTC Team 25: cmacfarl, September 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;

public class MonitorMotorTask extends RobotTask {

    /*
     * No Events.
     */
    public enum EventKind {
        ERROR_UPDATE,
    }

    public class MonitorMotorEvent extends RobotEvent {
        public EventKind kind;
        public int val;

        public MonitorMotorEvent(RobotTask task, EventKind kind, int val) {
            super(task);
            this.kind = kind;
            this.val = val;
        }
    }

    protected int target;

    protected Robot robot;
    protected DcMotor motor;

    public MonitorMotorTask(Robot robot, DcMotor motor)
    {
        super(robot);

        this.motor = motor;
        this.robot = robot;
        this.target = 0;
    }

    public MonitorMotorTask(Robot robot, DcMotor motor, int target)
    {
        super(robot);

        this.motor = motor;
        this.robot = robot;
        this.target = target;
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        int error;
        int position;

        position = motor.getCurrentPosition();
        error = target - position;

        robot.queueEvent(new MonitorMotorEvent(this, EventKind.ERROR_UPDATE, error));

        robot.telemetry.addData(motor.getConnectionInfo() + " Postion: ", Math.abs(position));
        robot.telemetry.addData(motor.getConnectionInfo() + " Target: ", Math.abs(target));
        robot.telemetry.addData(motor.getConnectionInfo() + " Error: ", Math.abs(error));

        /*
         * Never stops.
         */
        return false;
    }
}
