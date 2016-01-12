
package team25core;

import com.qualcomm.hardware.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.RobotLog;

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
        ERROR_UPDATE,
    }

    public class GyroEvent extends RobotEvent {
        public EventKind kind;
        public int val;

        public GyroEvent(RobotTask task, EventKind kind) {
            super(task);
            this.kind = kind;
            this.val = 0;
        }

        public GyroEvent(RobotTask task, EventKind kind, int val) {
            super(task);
            this.kind = kind;
            this.val = val;
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
        // sensor.resetZAxisIntegrator();

        if (targetHeading > 0) {
            ((ModernRoboticsI2cGyro)sensor).setHeadingMode(ModernRoboticsI2cGyro.HeadingMode.HEADING_CARDINAL);
        } else {
            ((ModernRoboticsI2cGyro)sensor).setHeadingMode(ModernRoboticsI2cGyro.HeadingMode.HEADING_CARTESIAN);
        }
    }

    @Override
    public void stop() {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice() {
        int currentHeading = sensor.getHeading();
        int absTarget = Math.abs(targetHeading);
        int error;

        if (showHeading) {
            robot.telemetry.addData("Current/target heading is: ", currentHeading + "/" + targetHeading);
        }

        error = (absTarget - currentHeading + 360) % 360;

        GyroEvent errorUpdate = new GyroEvent(this, EventKind.ERROR_UPDATE, error);
        robot.queueEvent(errorUpdate);

        if (error == 0) {
            GyroEvent hitTarget = new GyroEvent(this, EventKind.HIT_TARGET);
            robot.queueEvent(hitTarget);
            return true;
        } else if (error <= (absTarget * 0.80) && t_80 == null) {
            t_80 = new GyroEvent(this, EventKind.THRESHOLD_80);
            robot.queueEvent(t_80);
            return false;
        } else if (error >= (absTarget * 0.90) && t_90 == null) {
            t_90 = new GyroEvent(this, EventKind.THRESHOLD_90);
            robot.queueEvent(t_90);
            return false;
        } else if (error >= (absTarget * 0.95) && t_95 == null) {
            t_95 = new GyroEvent(this, EventKind.THRESHOLD_95);
            robot.queueEvent(t_95);
            return false;
        } else if (error > 345) {
            GyroEvent pastTarget = new GyroEvent(this, EventKind.PAST_TARGET);
            robot.queueEvent(pastTarget);
            return true;
        } else {
            return false;
        }
    }
}
