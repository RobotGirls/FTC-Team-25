package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.GyroTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 1/12/2017.
 */
@Autonomous(name = "Daisy: Gyro Alignment Test", group = "Team25")
//@Disabled
public class GyroAlignmentTest extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private GyroSensor gyroSensor;
    private int gyroMultiplier = 1;
    private PersistentTelemetryTask ptt;

    @Override
    public void init()
    {
        super.init();

        // Hardware mapping.
        frontLeft  = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft   = hardwareMap.dcMotor.get("rearLeft");
        rearRight  = hardwareMap.dcMotor.get("rearRight");
        gyroSensor  = hardwareMap.gyroSensor.get("gyroSensor");

        // Reset encoders.
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Telemetry.
        ptt = new PersistentTelemetryTask(this);

        // Gyro calibration.
        gyroSensor.calibrate();
        gyroSensor.resetZAxisIntegrator();
    }

    @Override
    public void start()
    {
        super.start();
        ptt.addData("Gyro Heading", gyroSensor.getHeading());
        RobotLog.i("141 Gyro Heading %d", gyroSensor.getHeading());
        this.addTask(new SingleShotTimerTask(this, 700) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                adjustWithGyro();
            }
        });
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof GyroTask.GyroEvent) {
            checkAlignment(e);
        }
    }

    private void checkAlignment(RobotEvent e)
    {
        GyroTask.GyroEvent event = (GyroTask.GyroEvent) e;
        if (event.kind == GyroTask.EventKind.HIT_TARGET) {
            RobotLog.i("141 Hit 90 degrees and aligned.");
            frontLeft.setPower(0);
            rearLeft.setPower(0);
            frontRight.setPower(0);
            rearRight.setPower(0);
        } else if (event.kind == GyroTask.EventKind.PAST_TARGET) {
            RobotLog.i("141 Past 90 degrees; re-aligning.");
            frontLeft.setPower(-0.1 * gyroMultiplier);
            rearLeft.setPower(-0.1 * gyroMultiplier);
            frontRight.setPower(0.1 * gyroMultiplier);
            rearRight.setPower(0.1 * gyroMultiplier);
            gyroMultiplier *= -1;
            addTask(new GyroTask(this, gyroSensor, -270, true));
        }
    }

    private void adjustWithGyro()
    {
        double error = Math.abs(90 - gyroSensor.getHeading());
        RobotLog.i("141 Beacon angle error of %f degrees", error);
        if (error >= 3) {
            RobotLog.i("141 Adjusting angle by turning.");
            frontLeft.setPower(0.1);
            rearLeft.setPower(0.1);
            frontRight.setPower(-0.1);
            rearRight.setPower(-0.1);
            addTask(new GyroTask(this, gyroSensor, 90, true));
        }
    }
}
