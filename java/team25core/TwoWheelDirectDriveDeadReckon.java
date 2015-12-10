
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

    DcMotor rightMotor;
    DcMotor leftMotor;
    PeriodicTimerTask ptt;

    public TwoWheelDirectDriveDeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyroSensor, DcMotor motorLeft, DcMotor motorRight)
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

        ptt = new PeriodicTimerTask(this.robot, 200) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                RobotLog.i("251 Setting power " + speed);
                leftMotor.setPower(speed);
                rightMotor.setPower(speed);
            }
        };
        robot.addTask(ptt);
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
        lCurrentPosition = leftMotor.getCurrentPosition();

        if (Math.abs(lCurrentPosition) >= targetPosition) {
            ptt.stop();
            RobotLog.i("251 Stopping motors");
            leftMotor.setPower(0.0);
            rightMotor.setPower(0.0);
        }

        return (Math.abs(lCurrentPosition) < targetPosition);

    }
}
