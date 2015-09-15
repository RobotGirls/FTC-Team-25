package com.qualcomm.ftcrobotcontroller.opmodes;/*
 * FTC Team 25: cmacfarl, September 03, 2015
 */

import com.qualcomm.robotcore.util.ElapsedTime;

public class SingleShotTimerTask implements RobotTask {

    public enum EventKind {
        EXPIRED,
    }

    public class SingleShotTimerEvent extends RobotEvent {

        EventKind kind;

        public SingleShotTimerEvent(Robot robot, EventKind kind)
        {
            super(robot);
            this.kind = kind;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "Single Shot Timer Event " + kind);
        }
    }

    Robot robot;
    ElapsedTime timer;
    int timeout;

    public SingleShotTimerTask(Robot robot, int timeout)
    {
        this.robot = robot;
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
            robot.queueEvent(new SingleShotTimerEvent(robot, EventKind.EXPIRED));
            return true;
        } else {
            return false;
        }
    }
}
