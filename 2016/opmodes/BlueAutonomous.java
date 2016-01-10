
package opmodes;

/*
 * FTC Team 5218: izzielau, November 02, 2015
 */

import com.qualcomm.hardware.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.Servo;

import org.swerverobotics.library.ClassFactory;

import team25core.ColorSensorTask;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.MonitorGyroTask;
import team25core.MonitorMotorTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.Team25DcMotor;
import team25core.TwoWheelDirectDriveDeadReckon;
import team25core.TwoWheelGearedDriveDeadReckon;

public class BlueAutonomous extends Robot {

    private final static int TICKS_PER_INCH = 318;
    private final static int LED_CHANNEL = 0;

    private DcMotorController mc;
    private Team25DcMotor leftTread;
    private Team25DcMotor rightTread;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftFlag;
    private Servo rightFlag;

    private TwoWheelGearedDriveDeadReckon deadReckon;
    private TwoWheelGearedDriveDeadReckon deadReckonPush;
    private DeadReckonTask deadReckonPushTask;
    private DeadReckonTask deadReckonTask;
    private MonitorGyroTask monitorGyroTask;
    private BeaconArms pushers;

    private MonitorMotorTask monitorMotorTask;

    public void handleDeadReckonEvent(DeadReckonTask.DeadReckonEvent e)
    {
        switch (e.kind) {
        case SEGMENT_DONE:
            break;
        case PATH_DONE:
            pushers.colorDeploy();
            addTask(new SingleShotTimerTask(this, 750) {
                @Override
                public void handleEvent(RobotEvent e)
                {
                    handleBeacon();
                }
            });

            break;
        }
    }

    public void handleBeacon()
    {
        addTask(new ColorSensorTask(this, color, core, true, true, LED_CHANNEL) {
            public void handleEvent(RobotEvent e)
            {
                ColorSensorEvent event = (ColorSensorEvent) e;

                if (event.kind == EventKind.BLUE) {
                    pushers.rightStow();
                    pushers.colorDeploy();
                } else if (event.kind == EventKind.RED) {
                    pushers.leftStow();
                    pushers.rightDeploy();
                } else {
                    pushers.allStow();
                }

                addTask(new SingleShotTimerTask(this.robot, 1000) {
                    @Override
                    public void handleEvent(RobotEvent e)
                    {
                        deadReckonPushTask = new DeadReckonTask(robot, deadReckonPush);
                        addTask(deadReckonPushTask);
                    }
                });
            }
        });
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            handleDeadReckonEvent((DeadReckonTask.DeadReckonEvent) e);
        }
    }

    @Override
    public void init()
    {
        // Gyro.
        gyro = (ModernRoboticsI2cGyro)hardwareMap.gyroSensor.get("gyro");
        gyro.setHeadingMode(ModernRoboticsI2cGyro.HeadingMode.HEADING_CARDINAL);
        gyro.calibrate();

        // Color.
        color = hardwareMap.colorSensor.get("color");
        core = hardwareMap.deviceInterfaceModule.get("interface");

        core.setDigitalChannelMode(LED_CHANNEL, DigitalChannelController.Mode.OUTPUT);
        core.setDigitalChannelState(LED_CHANNEL, false);

        // Servos.
        rightPusher = hardwareMap.servo.get("rightPusher");
        leftPusher = hardwareMap.servo.get("leftPusher");
        // rightFlag = hardwareMap.servo.get("rightFlag");
        // leftFlag = hardwareMap.servo.get("leftFlag");

        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        // rightFlag.setPosition(NeverlandServoConstants.RIGHT_FLAG_NINETY);
        // leftFlag.setPosition(NeverlandServoConstants.LEFT_FLAG_NINETY);

        pushers = new BeaconArms(rightPusher, leftPusher, true);

        // Treads.
        mc = hardwareMap.dcMotorController.get("motors");

        rightTread = new Team25DcMotor(this, mc, 2);
        leftTread = new Team25DcMotor(this, mc, 1);
        rightTread.stopPeriodic();
        leftTread.stopPeriodic();

        rightTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        rightTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        ClassFactory.createEasyMotorController(this, leftTread, rightTread);

        // Class: Dead reckon.
        deadReckon = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, gyro, leftTread, rightTread);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 38, 0.7);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, 0.4);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 38, 0.7);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, 0.4);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 14, 0.7);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 0, 0);

        // Class: Dead reckon (push beacon button).
        deadReckonPush = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, gyro, leftTread, rightTread);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 7.5, 0.4);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 7.5, -0.4);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 0, 0);
    }

    @Override
    public void start()
    {
        gyro.resetZAxisIntegrator();

        monitorMotorTask = new MonitorMotorTask(this, leftTread);
        deadReckonTask = new DeadReckonTask(this, deadReckon);
        monitorGyroTask = new MonitorGyroTask(this, gyro);

        addTask(monitorMotorTask);
        addTask(monitorGyroTask);
        addTask(deadReckonTask);
    }

    @Override
    public void stop()
    {
        if (deadReckonTask != null) {
            deadReckonTask.stop();
        }
    }
}