
package team25core;

/*
 * FTC Team 5218: izzielau, December 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.RobotLog;

public class SingleWheelDirectDriveDeadReckon extends DeadReckon {

    private int targetPosition;
    public int lCurrentPosition;

    Team25DcMotor leftMotor;

    /*
     * For gyro based turns
     */
    public SingleWheelDirectDriveDeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyroSensor, Team25DcMotor motorLeft, Team25DcMotor motorRight)
    {
        super(robot, encoderTicksPerInch, gyroSensor, motorLeft);

        this.leftMotor = motorLeft;
    }

    /*
     * For encoder based turns
     */
    public SingleWheelDirectDriveDeadReckon(Robot robot, int encoderTicksPerInch, int encoderTicksPerDegree, Team25DcMotor motorLeft)
    {
        super(robot, encoderTicksPerInch, encoderTicksPerDegree, motorLeft);

        this.leftMotor = motorLeft;
    }

    @Override
    protected void resetEncoders()
    {
        leftMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
    }

    @Override
    protected void encodersOn()
    {
        leftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
    }

    @Override
    protected void motorStraight(final double speed)
    {
        leftMotor.setPower(speed);
    }

    @Override
    protected void motorTurn(double speed)
    {
        RobotLog.i("251 Turning speed " + speed);
        leftMotor.setPower(speed);
    }

    @Override
    protected void motorSideways(double speed)
    {
        // Unsupported operation.
    }

    @Override
    protected void motorStop()
    {
        leftMotor.setPower(0.0);
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
