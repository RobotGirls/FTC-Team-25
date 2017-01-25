package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.GyroTask;
import team25core.MecanumGearedDriveDeadReckon;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 1/12/2017.
 */
@Autonomous(name = "Daisy: Dead Reckon Gyro Alignment Test", group = "Team25")
@Disabled
public class DeadReckonGyroAlignmentTest extends Robot
{
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private GyroSensor gyroSensor;
    private PersistentTelemetryTask ptt;
    private MecanumGearedDriveDeadReckon adjustTurn;

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

        adjustTurn = new MecanumGearedDriveDeadReckon(this, DaisyConfiguration.TICKS_PER_INCH, DaisyConfiguration.TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);

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
                adjustWithoutGyro();
            }
        });
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        // Nothing.
    }

    private void adjustWithoutGyro()
    {
        double error = 90 - gyroSensor.getHeading();
        RobotLog.i("141 Gyro heading %d", gyroSensor.getHeading());
        RobotLog.i("141 Beacon angle error of %f degrees", error);

        if (error >= 10) {
            RobotLog.i("141 Adjusting angle by turning.");
            adjustTurn.addSegment(DeadReckon.SegmentType.TURN, error, -0.2);

            this.addTask(new DeadReckonTask(this, adjustTurn) {
                @Override
                public void handleEvent(RobotEvent e) {
                    DeadReckonEvent event = (DeadReckonEvent) e;
                    if (event.kind == EventKind.PATH_DONE) {
                        RobotLog.i("141 Path done, checking alignment");
                        adjustWithoutGyro();
                    }
                }
            });
        } else if (error <= -10) {
            RobotLog.i("141 Adjusting angle by turning.");
            adjustTurn.addSegment(DeadReckon.SegmentType.TURN, error, 0.2);

            this.addTask(new DeadReckonTask(this, adjustTurn) {
                @Override
                public void handleEvent(RobotEvent e) {
                    DeadReckonEvent event = (DeadReckonEvent) e;
                    if (event.kind == EventKind.PATH_DONE) {
                        adjustWithoutGyro();
                    }
                }
            });
        } else {
            RobotLog.i("141 Aligned and pushing");
            // goPushBeacon();
        }
    }
}
