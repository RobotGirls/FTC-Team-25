package opmodes;

import com.qualcomm.robotcore.util.RobotLog;

import team25core.Drivetrain;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RobotTask;
import team25core.SensorCriteria;

public class DynamicSpeedTask extends RobotTask {

    public enum EventKind {
        DONE,
    }

    public class DynamicSpeedEvent extends RobotEvent {

        public EventKind kind;

        public DynamicSpeedEvent(RobotTask task, EventKind k)
        {
            super(task);
            kind = k;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "DynamicSpeed Event " + kind);
        }
    }

    Drivetrain drivetrain;

    private final static double FAST_SPEED        = 0.7;
    private final static double MEDIUM_FAST_SPEED = 0.2;
    private final static double MEDIUM_SPEED      = 0.152;
    private final static double SLOW_SPEED        = 0.03;
    private final static double SLEW_UP_RATE      = 40;     // The larger the number the slower ramp up.
    private final static double SLEW_DOWN_RATE    = 30;

    private int distance;
    private double currentSpeed;

    public DynamicSpeedTask(Robot robot, int distance, Drivetrain drivetrain)
    {
        super(robot);

        this.drivetrain = drivetrain;
        this.distance = distance;
    }

    @Override
    public void start()
    {
        drivetrain.resetEncoders();
        drivetrain.encodersOn();
        drivetrain.setTargetInches(distance);

        currentSpeed = 0;
    }

    @Override
    public void stop()
    {
        drivetrain.stop();
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        if (drivetrain.percentComplete() < .20) {
            currentSpeed += FAST_SPEED / SLEW_UP_RATE;
            currentSpeed = Math.min(currentSpeed, FAST_SPEED);
            currentSpeed = Math.max(currentSpeed, 0.0);
        } else if (drivetrain.percentComplete() > .80) {
            currentSpeed -= FAST_SPEED / SLEW_DOWN_RATE;
            currentSpeed = Math.min(currentSpeed, FAST_SPEED);
            currentSpeed = Math.max(currentSpeed, MEDIUM_FAST_SPEED);
        }
        drivetrain.straight(currentSpeed);

        if (!drivetrain.isBusy()) {
            return true;
        } else {
            return false;
        }
    }
}
