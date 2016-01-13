package team25core;

/*
 * FTC Team 25: cmacfarl, December 10, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;

import org.swerverobotics.library.internal.EasyModernMotorController;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Team25DcMotor extends DcMotor
{
    protected Robot robot;
    protected double power;
    protected int targetPosition;
    protected Set<Team25DcMotor> slaves = null;
    private final static double powerMax = 1.0;
    private final static double powerMin = -1.0;

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
        this.power = 0.0;
    }

    public void stopPeriodic()
    {
        this.robot.removeTask(ptt);
    }

    public void startPeriodic()
    {
        this.robot.addTask(ptt);
    }

    /**
     * Sets the power for this motor and any of its slaves.
     *
     * @param double The power to apply to the motor.
     */
    public void setPower(double power)
    {
        power = Range.clip(power, powerMin, powerMax);

        if (slaves != null) {
            if (slaves.size() == 1) {
                Team25DcMotor m = (Team25DcMotor) slaves.toArray()[0];
                DcMotorController mc = m.getController();
                if (mc instanceof org.swerverobotics.library.internal.EasyModernMotorController) {
                    ((EasyModernMotorController) mc).setMotorPower(power);
                    return;
                }
            }
            for (Team25DcMotor m : slaves) {
                m.setPower(power);
            }
        }

        this.power = power;
        super.setPower(power);
    }

    /**
     * Rotates two motors in opposite directions.  This motor must be
     * bound with one, and only one, slave.
     *
     * @param power The motor power to apply to the master.  The slave is
     * applied a negated power value.
     */
    public void turn(double power)
    {
        if ((slaves == null) || (slaves.size() != 1)) {
            throw new UnsupportedOperationException("Turn must be called only with a motor with one slave");
        }

        power = Range.clip(power, powerMin, powerMax);
        this.power = power;
        super.setPower(power);
        ((Team25DcMotor)slaves.toArray()[0]).setPower(-power);
    }

    public void setTargetPosition(int position)
    {
        targetPosition = position;
    }

    public boolean isBusy()
    {
        int currentPosition = getCurrentPosition();

        return (Math.abs(currentPosition) < targetPosition);
    }

    /**
     * Bind a set of motors to this motor as slaves.  The setPower operation will
     * this apply to all of the slaves in the set.
     *
     * @param slaves A set of motors to bind to this master.
     */
    public void bind(Set<Team25DcMotor> slaves)
    {
        this.slaves = slaves;
    }

    /**
     * A convenience function to bind a single motor to this master.  Will throw
     * away any previous binds/pairs.  If you want to bind multiple motors to
     * this motor use bind().
     *
     * @param slave A motor to bind to this master.
     */
    public void pair(Team25DcMotor slave)
    {
        this.slaves = new HashSet<Team25DcMotor>();
        this.slaves.add(slave);
    }

    public void unbind()
    {
        this.slaves = null;
    }
}
