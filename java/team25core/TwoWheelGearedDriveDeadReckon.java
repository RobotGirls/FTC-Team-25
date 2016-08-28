
package team25core;

/*
 * FTC Team 5218: izzielau, December 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.RobotLog;

import org.swerverobotics.library.internal.EasyModernMotorController;

public class TwoWheelGearedDriveDeadReckon extends DeadReckon {

    private int targetPosition;
    public int lCurrentPosition;

    DcMotor rightMotor;
    DcMotor leftMotor;
    MonitorMotorTask mmt;

    /*
     * Assumes that both motors are on the same controller.
     */
    public TwoWheelGearedDriveDeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyroSensor, DcMotor motorLeft, DcMotor motorRight)
    {
        super(robot, encoderTicksPerInch, gyroSensor, motorLeft);

        this.rightMotor = motorRight;
        this.leftMotor = motorLeft;
        this.mmt = null;

        rightMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    public TwoWheelGearedDriveDeadReckon(Robot robot, int encoderTicksPerInch, int encoderTicksPerDegree, DcMotor motorLeft, DcMotor motorRight)
    {
        super(robot, encoderTicksPerInch, encoderTicksPerDegree, motorLeft);

        this.rightMotor = motorRight;
        this.leftMotor = motorLeft;
        this.mmt = null;

        rightMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    @Override
    protected void resetEncoders()
    {
        leftMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
        rightMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
    }

    @Override
    protected void encodersOn()
    {
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        rightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
    }

    @Override
    protected void motorStraight(final double speed)
    {
        EasyModernMotorController mc = (EasyModernMotorController)leftMotor.getController();
        mc.setMotorPower(speed);
    }

    @Override
    protected void motorTurn(double speed)
    {
        if (speed == 0) {
            mmt = new MonitorMotorTask(robot, leftMotor, target) {
                @Override
                public void handleEvent(RobotEvent event)
                {
                    MonitorMotorEvent ev = (MonitorMotorEvent)event;
                    double speed;
                    double e = Math.exp(1.0);

                    double logVal = Math.pow(e, (5.6 * (ev.val / Math.abs(target))));
                    if (currSegment.distance < 0) {
                        speed = -(logVal / 100) - 0.01;
                    } else {
                        speed = (logVal / 100) + 0.01;
                    }
                    rightMotor.setPower(speed);
                    leftMotor.setPower(-speed);

                }
            };
            robot.addTask(mmt);
        } else {
            rightMotor.setPower(speed);
            leftMotor.setPower(-speed);
        }
    }

    @Override
    protected void motorStop()
    {
        if (mmt != null) {
            mmt.stop();
        }
        RobotLog.i("251 Stopping motors");
        motorStraight(0.0);
    }

    @Override
    protected boolean isBusy()
    {
        boolean busy = leftMotor.isBusy();

        if (!busy) {
            motorStop();
        }

        return (busy);
    }
}
