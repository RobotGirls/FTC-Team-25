
package team25core;

/*
 * FTC Team 5218: izzielau, December 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;

public class TwoWheelDriveDeadReckon extends DeadReckon {
    private int targetPosition;

    DcMotor rightMotor;
    DcMotor leftMotor;

    public TwoWheelDriveDeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyroSensor, DcMotor motorLeft, DcMotor motorRight)
    {
        super(robot, encoderTicksPerInch, gyroSensor, motorLeft);

        this.rightMotor = motorRight;
        this.leftMotor = motorLeft;
    }

    @Override
    protected void resetEncoders(int ticks)
    {
        leftMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        rightMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        targetPosition = ticks;
    }

    @Override
    protected void motorStraight(double speed)
    {
        leftMotor.setPower(speed);
        rightMotor.setPower(speed);
    }

    @Override
    protected void motorTurn(double speed)
    {
        leftMotor.setPower(speed);
        rightMotor.setPower(-speed);
    }

    @Override
    protected boolean isBusy()
    {
        int lCurrentPosition = leftMotor.getCurrentPosition();

        return (Math.abs(lCurrentPosition) < targetPosition);
    }
}
