package team25core;
/*
 * FTC Team 25: cmacfarl, September 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;

import java.util.Set;

public abstract class RunToEncoderHitechnicValueTask extends RobotTask {

    public enum EventKind {
        DONE,
    }

    public class RunToEncoderHitechnicValueEvent extends RobotEvent {

        EventKind kind;

        public RunToEncoderHitechnicValueEvent(RobotTask task, EventKind kind)
        {
            super(task);
            this.kind = kind;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "RunToEncoderValue Event " + kind);
        }

    }

    protected DcMotor master;
    protected int encoderValue;
    protected Set<DcMotor> slaves;

    public RunToEncoderHitechnicValueTask(Robot robot, DcMotor master, Set<DcMotor> slaves, int encoderValue)
    {
        super(robot);

        this.master = master;
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
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        if (Math.abs(master.getCurrentPosition()) >= encoderValue) {
            robot.queueEvent(new RunToEncoderHitechnicValueEvent(this, EventKind.DONE));
            return true;
        } else {
            return false;
        }
    }
}
