package team25core;/*
 * FTC Team 25: cmacfarl, December 09, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;

public class ResetMotorEncoderTask extends RobotTask {

    public enum EventKind {
        DONE,
    }

    public class ResetMotorEncoderEvent extends RobotEvent {

        EventKind kind;

        public ResetMotorEncoderEvent(RobotTask task, EventKind kind)
        {
            super(task);
            this.kind = kind;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "ResetMotorEncoder Event " + kind);
        }
    };

    protected DcMotor motor;

    public ResetMotorEncoderTask(Robot robot, DcMotor motor)
    {
        super(robot);
        this.motor = motor;
    }

    @Override
    public void start()
    {
        motor.setMode(DcMotor.RunMode.RESET_ENCODERS);
    }

    @Override
    public void stop()
    {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        if (motor.getCurrentPosition() == 0) {
            robot.queueEvent(new ResetMotorEncoderEvent(this, EventKind.DONE));
            return true;
        } else {
            return false;
        }
    }
}
