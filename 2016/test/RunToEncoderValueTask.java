package com.qualcomm.ftcrobotcontroller.opmodes;/*
 * FTC Team 25: cmacfarl, September 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.Set;

public class RunToEncoderValueTask implements RobotTask {

    public enum EventKind {
        DONE,
    }

    public class RunToEncoderValueEvent extends RobotEvent {

        EventKind kind;

        public RunToEncoderValueEvent(Robot robot, EventKind kind)
        {
            super(robot);
            this.kind = kind;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "RunToEncoderValue Event " + kind);
        }

    }


    protected Robot robot;
    protected DcMotor master;
    protected int encoderValue;
    protected Set<DcMotor> slaves;

    /*
     *
     */
    public RunToEncoderValueTask(Robot robot, DcMotor master, Set<DcMotor> slaves, int encoderValue)
    {
        this.master = master;
        this.robot = robot;
        this.master = master;
        this.slaves = slaves;
        this.encoderValue = encoderValue;
    }

    @Override
    public void start()
    {
        // TODO: ??
    }

    @Override
    public void stop()
    {
        master.setPower(0.0);
        for (DcMotor slave : slaves) {
            slave.setPower(0.0);
        }
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        if (Math.abs(master.getCurrentPosition()) >= encoderValue) {
            robot.queueEvent(new RunToEncoderValueEvent(robot, EventKind.DONE));
            return true;
        } else {
            return false;
        }
    }
}
