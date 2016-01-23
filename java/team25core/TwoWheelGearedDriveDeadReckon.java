
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

    /*
     * Assumes that both motors are on the same controller.
     */
    public TwoWheelGearedDriveDeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyroSensor, DcMotor motorLeft, DcMotor motorRight)
    {
        super(robot, encoderTicksPerInch, gyroSensor, motorLeft);

        this.rightMotor = motorRight;
        this.leftMotor = motorLeft;

        leftMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    public TwoWheelGearedDriveDeadReckon(Robot robot, int encoderTicksPerInch, int encoderTicksPerDegree, DcMotor motorLeft, DcMotor motorRight)
    {
        super(robot, encoderTicksPerInch, encoderTicksPerDegree, motorLeft);

        this.rightMotor = motorRight;
        this.leftMotor = motorLeft;

        leftMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    @Override
    protected void resetEncoders()
    {
        leftMotor.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        rightMotor.setMode(DcMotorController.RunMode.RESET_ENCODERS);
    }

    @Override
    protected void encodersOn()
    {
        leftMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        rightMotor.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
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
        leftMotor.setPower(speed);
        rightMotor.setPower(-speed);
    }

    @Override
    protected void motorStop()
    {
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
