package team25core;

import com.qualcomm.robotcore.hardware.GyroSensor;

/**
 * Created by katie on 11/14/15.
 */
public class GyroTask extends RobotTask {

    public enum EventKind {
        THRESHOLD_80,
        THRESHOLD_90,
        THRESHOLD_95,
        HIT_TARGET,
        PAST_TARGET,
    }

    public class GyroEvent extends RobotEvent {
        public EventKind kind;

        public GyroEvent(RobotTask task, EventKind kind) {
            super(task);
            this.kind = kind;
        }
    }

    protected int targetHeading = 0;
    protected GyroSensor sensor;
    protected boolean showHeading = false;

    protected GyroEvent t_80;
    protected GyroEvent t_90;
    protected GyroEvent t_95;


    public GyroTask(Robot robot, GyroSensor sensor, int targetHeading, boolean showHeading)
    {
        super(robot);
        this.targetHeading = targetHeading;
        this.sensor = sensor;
        this.showHeading = showHeading;
    }

    @Override
    public void start() {
        sensor.resetZAxisIntegrator();
    }

    @Override
    public void stop() {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice() {
        int currentHeading = sensor.getHeading();

        if(showHeading) {
            robot.telemetry.addData("Current heading is: ", currentHeading);
        }

        if (currentHeading == targetHeading) {
            GyroEvent hitTarget = new GyroEvent(this, EventKind.HIT_TARGET);
            robot.queueEvent(hitTarget);
            return true;
        } else if (currentHeading >= (targetHeading * 0.80) && t_80 == null) {
            t_80 = new GyroEvent(this, EventKind.THRESHOLD_80);
            robot.queueEvent(t_80);
            return false;
        } else if (currentHeading >= (targetHeading * 0.90) && t_90 == null) {
            t_90 = new GyroEvent(this, EventKind.THRESHOLD_90);
            robot.queueEvent(t_90);
            return false;
        } else if (currentHeading >= (targetHeading * 0.95) && t_95 == null) {
            t_95 = new GyroEvent(this, EventKind.THRESHOLD_95);
            robot.queueEvent(t_95);
            return false;
        } else if (currentHeading >= targetHeading) {
            GyroEvent pastTarget = new GyroEvent(this, EventKind.PAST_TARGET);
            robot.queueEvent(pastTarget);
            return false;
        } else {
            return false;
        }
    }
}
