
package team25core;

/*
 * FTC Team 5218: izzielau, December 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.RobotLog;

public class TwoWheelDirectDriveDeadReckon extends DeadReckon {

    private int targetPosition;
    public int lCurrentPosition;

    Team25DcMotor rightMotor;
    Team25DcMotor leftMotor;

    public TwoWheelDirectDriveDeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyroSensor, Team25DcMotor motorLeft, Team25DcMotor motorRight)
    {
        super(robot, encoderTicksPerInch, gyroSensor, motorLeft);

        this.rightMotor = motorRight;
        this.leftMotor = motorLeft;

        rightMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    @Override
    protected void resetEncoders(int ticks)
    {
        leftMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        rightMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        targetPosition = ticks;
    }

    @Override
    protected void motorStraight(final double speed)
    {
        leftMotor.setPower(speed);
        rightMotor.setPower(speed);
    }

    @Override
    protected void motorTurn(double speed)
    {
        RobotLog.i("251 Turning speed " + speed);
        leftMotor.setPower(speed);
        rightMotor.setPower(-speed);
    }

    @Override
    protected void motorStop()
    {
        leftMotor.setPower(0.0);
        rightMotor.setPower(0.0);
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
