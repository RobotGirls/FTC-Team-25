package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.I2cController;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.GyroTask;
import team25core.MecanumGearedDriveDeadReckon;
import team25core.MonitorGyroTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 1/12/2017.
 */
@Autonomous(name = "Daisy: Gyro Alignment Test", group = "Team25")
@Disabled
public class GyroAlignmentTest extends Robot {
    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    private GyroSensor gyroSensor;
    private int gyroMultiplier = 1;
    private PersistentTelemetryTask ptt;
    private MonitorGyroTask gyroMonitor;
    private MecanumGearedDriveDeadReckon adjustTurn;
    private DeviceInterfaceModule cdim;

    I2cController.I2cPortReadyCallback colorSensorCallback;
    I2cController.I2cPortReadyCallback rangeSensorCallback;
    private int colorPort = 0;
    private int rangePort = 2;


    @Override
    public void init() {
        super.init();

        // Hardware mapping.
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");
        gyroSensor = hardwareMap.gyroSensor.get("gyroSensor");
        cdim = hardwareMap.deviceInterfaceModule.get("cdim");

        this.colorSensorCallback = cdim.getI2cPortReadyCallback(colorPort);
        cdim.deregisterForPortReadyCallback(colorPort);

        this.rangeSensorCallback = cdim.getI2cPortReadyCallback(rangePort);
        cdim.deregisterForPortReadyCallback(rangePort);

        gyroMonitor = new MonitorGyroTask(this, gyroSensor);

        // Reset encoders.
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        adjustTurn = new MecanumGearedDriveDeadReckon(this, Daisy.TICKS_PER_INCH, Daisy.TICKS_PER_DEGREE, frontLeft, frontRight, rearLeft, rearRight);
        // Telemetry.
        ptt = new PersistentTelemetryTask(this);

        // Gyro calibration.
        gyroSensor.calibrate();
        addTask(gyroMonitor);
        //gyroSensor.resetZAxisIntegrator();
    }

    @Override
    public void start() {
        super.start();
        ptt.addData("Gyro Heading", gyroSensor.getHeading());
        RobotLog.i("141 Gyro Heading %d", gyroSensor.getHeading());
        this.addTask(new SingleShotTimerTask(this, 700) {
            @Override
            public void handleEvent(RobotEvent e) {
                adjustWithGyro();
            }
        });
    }

    @Override
    public void handleEvent(RobotEvent e) {
        if (e instanceof GyroTask.GyroEvent) {
            checkAlignment(e);
        }
    }

    // We don't use this anymore, but don't want to delete it just in case.
    private void checkAlignment(RobotEvent e) {
        GyroTask.GyroEvent event = (GyroTask.GyroEvent) e;
        if (event.kind == GyroTask.EventKind.HIT_TARGET) {
            RobotLog.i("141 Hit 90 degrees and aligned.");
            frontLeft.setPower(0);
            rearLeft.setPower(0);
            frontRight.setPower(0);
            rearRight.setPower(0);
        } else if (event.kind == GyroTask.EventKind.PAST_TARGET) {
            RobotLog.i("141 Past 90 degrees; re-aligning.");
            frontLeft.setPower(0.2 * gyroMultiplier);
            rearLeft.setPower(0.2 * gyroMultiplier);
            frontRight.setPower(0.2 * gyroMultiplier);
            rearRight.setPower(0.2 * gyroMultiplier);
            gyroMultiplier *= -1;
            addTask(new GyroTask(this, gyroSensor, -270, true));
        }
    }

    private void adjustWithGyro() {
        double error = 90 - gyroSensor.getHeading();
        RobotLog.i("141 Gyro heading %d", gyroSensor.getHeading());
        RobotLog.i("141 Beacon angle error of %f degrees", error);

       /* if (alliance == Alliance.BLUE) {
            error *= -1;
        } */

        if (error >= 2) {
            RobotLog.i("141 Adjusting angle by turning.");
            adjustTurn.addSegment(DeadReckon.SegmentType.TURN, error, -0.1);

            this.addTask(new DeadReckonTask(this, adjustTurn) {
                @Override
                public void handleEvent(RobotEvent e) {
                    DeadReckonEvent event = (DeadReckonEvent) e;
                    if (event.kind == EventKind.PATH_DONE) {
                        RobotLog.i("141 Path done, checking alignment");
                        adjustWithGyro();
                    }
                }
            });
        } else if (error <= -2) {
            RobotLog.i("141 Adjusting angle by turning.");
            adjustTurn.addSegment(DeadReckon.SegmentType.TURN, error, 0.1);

            this.addTask(new DeadReckonTask(this, adjustTurn) {
                @Override
                public void handleEvent(RobotEvent e) {
                    DeadReckonEvent event = (DeadReckonEvent) e;
                    if (event.kind == DeadReckonTask.EventKind.PATH_DONE) {
                        adjustWithGyro();
                    }
                }
            });
        } else {
            RobotLog.i("141 Aligned and pushing.");
            //goPushBeacon();
            //goToNextBeacon();
        }
    }
}
