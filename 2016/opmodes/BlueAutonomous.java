
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
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.ColorSensorTask;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.GyroTask;
import team25core.MonitorGyroTask;
import team25core.MonitorMotorTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TwoWheelDriveDeadReckon;

public class BlueAutonomous extends Robot {

    private final static int TICKS_PER_INCH = 100;
    private final static int LED_CHANNEL = 0;

    private DcMotor leftTread;
    private DcMotor rightTread;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftFlag;
    private Servo rightFlag;

    private TwoWheelDriveDeadReckon deadReckon;
    private TwoWheelDriveDeadReckon deadReckonPush;
    private DeadReckonTask deadReckonPushTask;
    private DeadReckonTask deadReckonTask;
    private MonitorGyroTask monitorGyroTask;

    private MonitorMotorTask monitorMotorTask;

    public void handleDeadReckonEvent(DeadReckonTask.DeadReckonEvent e)
    {
        switch (e.kind) {
        case SEGMENT_DONE:
            break;
        case PATH_DONE:
            //handleBeacon();
            break;
        }
    }

    public void handleBeacon() {

        addTask(new ColorSensorTask(this, color, core, true, true, LED_CHANNEL) {
            public void handleEvent(RobotEvent e) {
                BeaconArms pushers = new BeaconArms(rightPusher, leftPusher, true);
                ColorSensorEvent event = (ColorSensorEvent)e;

                if (event.kind == EventKind.BLUE) {
                    pushers.allStow();
                    pushers.colorDeploy();
                } else if (event.kind == EventKind.RED) {
                    pushers.allStow();
                    pushers.rightDeploy();
                } else {
                    pushers.allStow();
                }

                deadReckonPushTask = new DeadReckonTask(robot, deadReckonPush);
                addTask(deadReckonPushTask);
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
        //color = hardwareMap.colorSensor.get("color");
        //core = hardwareMap.deviceInterfaceModule.get("interface");

        //core.setDigitalChannelMode(LED_CHANNEL, DigitalChannelController.Mode.OUTPUT);
        //core.setDigitalChannelState(LED_CHANNEL, false);

        // Servos.
        // rightPusher = hardwareMap.servo.get("rightPusher");
        // leftPusher = hardwareMap.servo.get("leftPusher");
        // rightFlag = hardwareMap.servo.get("rightFlag");
        // leftFlag = hardwareMap.servo.get("leftFlag");

        // rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        // leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        // rightFlag.setPosition(NeverlandServoConstants.RIGHT_FLAG_NINETY);
        // leftFlag.setPosition(NeverlandServoConstants.LEFT_FLAG_NINETY);

        // Treads.
        rightTread = hardwareMap.dcMotor.get("rightTread");
        leftTread = hardwareMap.dcMotor.get("leftTread");

        leftTread.setDirection(DcMotor.Direction.REVERSE);

        rightTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        rightTread.setMode(DcMotorController.RunMode.RUN_TO_POSITION);
        leftTread.setMode(DcMotorController.RunMode.RUN_TO_POSITION);

        // Class: Dead reckon.
        deadReckon = new TwoWheelDriveDeadReckon(this, TICKS_PER_INCH, gyro, leftTread, rightTread);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 52, -0.5);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, 0.2);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 34, -0.5);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, 0.2);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 20, -0.5);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 0, 0);

        deadReckonPush = new TwoWheelDriveDeadReckon(this, TICKS_PER_INCH, gyro, leftTread, rightTread);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 7.5, 0.4);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 7.5, -0.4);
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
        deadReckonTask.stop();
    }
}