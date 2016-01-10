
package team25core;

/*
 * FTC Team 5218: izzielau, December 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.RobotLog;

public class TwoWheelGearedDriveDeadReckon extends DeadReckon {

    private int targetPosition;
    public int lCurrentPosition;

    DcMotor rightMotor;
    DcMotor leftMotor;

    public TwoWheelGearedDriveDeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyroSensor, DcMotor motorLeft, DcMotor motorRight)
    {
        super(robot, encoderTicksPerInch, gyroSensor, motorLeft);

        this.rightMotor = motorRight;
        this.leftMotor = motorLeft;

        leftMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    @Override
    protected void resetEncoders(int ticks)
    {
        leftMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        rightMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        leftMotor.setTargetPosition(ticks);
    }

    @Override
    protected void motorStraight(final double speed)
    {
        RobotLog.i("251 Straight speed " + speed);
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
        RobotLog.i("251 Stopping motors");
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
