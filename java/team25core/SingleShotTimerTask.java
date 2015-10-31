package team25core;/*
 * FTC Team 25: cmacfarl, September 03, 2015
 */

import com.qualcomm.robotcore.util.ElapsedTime;

public abstract class SingleShotTimerTask extends RobotTask {

    public enum EventKind {
        EXPIRED,
    }

    public class SingleShotTimerEvent extends RobotEvent {

        EventKind kind;

        public SingleShotTimerEvent(RobotTask task, EventKind kind)
        {
            super(task);
            this.kind = kind;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "Single Shot Timer Event " + kind);
        }
    }

    protected ElapsedTime timer;
    protected int timeout;

    public SingleShotTimerTask(Robot robot, int timeout)
    {
        super(robot);

        this.timeout = timeout;
    }

    public void start()
    {
         timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    }

    @Override
    public void stop()
    {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        if (timer.time() > timeout) {
            robot.queueEvent(new SingleShotTimerEvent(this, EventKind.EXPIRED));
            return true;
        } else {
            return false;
        }
    }
}
