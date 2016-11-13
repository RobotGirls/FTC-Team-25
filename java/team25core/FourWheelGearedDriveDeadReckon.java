
package team25core;

/*
 * FTC Team 25: katie
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.RobotLog;

public class FourWheelGearedDriveDeadReckon extends DeadReckon {

    private int targetPosition;
    public int lCurrentPosition;

    DcMotor frontRightMotor;
    DcMotor frontLeftMotor;
    DcMotor rearRightMotor;
    DcMotor rearLeftMotor;
    MonitorMotorTask mmt;

    /*
     * Assumes that both motors are on the same controller.
     */
    public FourWheelGearedDriveDeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyroSensor, DcMotor motorLeftFront, DcMotor motorRightFront, DcMotor motorLeftRear, DcMotor motorRightRear)
    {
        super(robot, encoderTicksPerInch, gyroSensor, motorLeftFront);

        this.frontRightMotor = motorRightFront;
        this.frontLeftMotor = motorLeftFront;
        this.rearRightMotor = motorRightRear;
        this.rearLeftMotor = motorLeftRear;
        this.mmt = null;

        frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
        rearRightMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    public FourWheelGearedDriveDeadReckon(Robot robot, int encoderTicksPerInch, int encoderTicksPerDegree, DcMotor motorLeftFront, DcMotor motorRightFront, DcMotor motorLeftRear, DcMotor motorRightRear)
    {
        super(robot, encoderTicksPerInch, encoderTicksPerDegree, motorLeftFront);

        this.frontRightMotor = motorRightFront;
        this.frontLeftMotor = motorLeftFront;
        this.rearRightMotor = motorRightRear;
        this.rearLeftMotor = motorLeftRear;
        this.mmt = null;

        frontRightMotor.setDirection(DcMotor.Direction.REVERSE);
        rearRightMotor.setDirection(DcMotor.Direction.REVERSE);
    }

    @Override
    protected void resetEncoders()
    {
        frontLeftMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
        frontRightMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
        rearLeftMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
        rearRightMotor.setMode(DcMotor.RunMode.RESET_ENCODERS);
    }

    @Override
    protected void encodersOn()
    {
        frontLeftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeftMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRightMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    protected void motorStraight(final double speed)
    {
        frontLeftMotor.setPower(speed);
        frontRightMotor.setPower(speed);
        rearLeftMotor.setPower(speed);
        rearRightMotor.setPower(speed);
    }

    @Override
    protected void motorTurn(double speed)
    {
        if (speed == 0) {
            mmt = new MonitorMotorTask(robot, frontLeftMotor, target) {
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

                    frontRightMotor.setPower(speed);
                    rearRightMotor.setPower(speed);
                    frontLeftMotor.setPower(-speed);
                    rearLeftMotor.setPower(-speed);

                }
            };
            robot.addTask(mmt);
        } else {
            frontRightMotor.setPower(speed);
            rearRightMotor.setPower(speed);
            frontLeftMotor.setPower(-speed);
            rearLeftMotor.setPower(-speed);
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
        boolean busy = frontLeftMotor.isBusy();

        if (!busy) {
            motorStop();
        }

        return (busy);
    }
}
