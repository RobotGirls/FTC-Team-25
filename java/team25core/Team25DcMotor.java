package team25core;

/*
 * FTC Team 25: cmacfarl, December 10, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.RobotLog;

public class Team25DcMotor extends DcMotor
{
    protected Robot robot;
    protected double power;
    protected int targetPosition;

    protected PeriodicTimerTask ptt = new PeriodicTimerTask(null, 200) {
        @Override
        public void handleEvent(RobotEvent e)
        {
            Team25DcMotor.super.setPower(Team25DcMotor.this.power);
        }
    };


    public Team25DcMotor(Robot robot, DcMotorController controller, int portNumber)
    {
        super(controller, portNumber);
        this.robot = robot;
        this.ptt.setRobot(robot);
        this.robot.addTask(ptt);
        this.power = 0.0;
    }

    @Override
    public void setPower(double power)
    {
        if (power > 1.0) {
            power = 1.0;
        } else if (power < -1.0) {
            power = -1.0;
        }

        this.power = power;
    }

    @Override
    public void setTargetPosition(int position)
    {
        targetPosition = position;
    }

    public boolean isBusy()
    {
        int currentPosition = getCurrentPosition();

        return (Math.abs(currentPosition) < targetPosition);
    }
}
