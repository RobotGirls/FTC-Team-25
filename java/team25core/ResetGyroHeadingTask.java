package team25core;/*
 * FTC Team 25: cmacfarl, December 09, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;

public class ResetGyroHeadingTask extends RobotTask {

    public enum EventKind {
        DONE,
    }

    public class ResetGyroHeadingEvent extends RobotEvent {

        EventKind kind;

        public ResetGyroHeadingEvent(RobotTask task, EventKind kind)
        {
            super(task);
            this.kind = kind;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "ResetGyroHeading Event " + kind);
        }
    };

    protected GyroSensor gyro;
    PeriodicTimerTask ptt;

    public ResetGyroHeadingTask(Robot robot, GyroSensor gyro)
    {
        super(robot);
        this.gyro = gyro;
    }

    @Override
    public void start()
    {
        gyro.resetZAxisIntegrator();
        ptt = new PeriodicTimerTask(this.robot, 200) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                gyro.resetZAxisIntegrator();
            }
        };
        this.robot.addTask(ptt);
    }

    @Override
    public void stop()
    {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        if (gyro.getHeading() == 0) {
            robot.queueEvent(new ResetGyroHeadingEvent(this, EventKind.DONE));
            if (ptt != null) {
                ptt.stop();
            }
            return true;
        } else {
            return false;
        }
    }
}
