
package opmodes;

/*
 * FTC Team 5218: izzielau, November 02, 2015
 */

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.Servo;

import org.swerverobotics.library.ClassFactory;
import org.swerverobotics.library.interfaces.Autonomous;
import org.swerverobotics.library.interfaces.Disabled;

import team25core.ColorSensorTask;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.MonitorGyroTask;
import team25core.MonitorMotorTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.Team25DcMotor;
import team25core.TwoWheelGearedDriveDeadReckon;

@Autonomous(name="BLUE Beacon", group = "AutoTeam25")
@Disabled
public class CaffeineBlueBeaconAutonomous extends Robot {

    public static final int TICKS_PER_DEGREE = NeverlandMotorConstants.ENCODER_TICKS_PER_DEGREE;
    private final static int TICKS_PER_INCH = NeverlandMotorConstants.ENCODER_TICKS_PER_INCH;
    private final static int LED_CHANNEL = 0;

    private DcMotorController mc;
    private Team25DcMotor leftTread;
    private Team25DcMotor rightTread;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;
    private Servo climber;

    private TwoWheelGearedDriveDeadReckon deadReckon;
    private TwoWheelGearedDriveDeadReckon deadReckonPush;
    private DeadReckonTask deadReckonPushTask;
    private DeadReckonTask deadReckonTask;
    private MonitorGyroTask monitorGyroTask;
    private BeaconArms pushers;
    private PersistentTelemetryTask telemetryTask = new PersistentTelemetryTask(this);

    private MonitorMotorTask monitorMotorTask;

    public void handleDeadReckonEvent(DeadReckonTask.DeadReckonEvent e)
    {
        switch (e.kind) {
        case SEGMENT_DONE:
            telemetryTask.addData("Segment " + e.segment_num, "Done");
            break;
        case PATH_DONE:
            pushers.colorDeploy();
            final SingleShotTimerTask sstt = new SingleShotTimerTask(this, 1000) {
                @Override
                public void handleEvent(RobotEvent e) {
                    handleBeacon();

                    SingleShotTimerTask ssttafter = new SingleShotTimerTask(this.robot, 1000) {
                        @Override
                        public void handleEvent(RobotEvent e) {
                            deadReckonPushTask = new DeadReckonTask(this.robot, deadReckonPush);
                            addTask(deadReckonPushTask);
                        }
                    };
                    addTask(ssttafter);
                }
            };
            addTask(sstt);
        }
    }

    public void handleBeacon() {
        addTask(new ColorSensorTask(this, color, core, true, true, LED_CHANNEL) {
            public void handleEvent(RobotEvent e) {
                final BeaconArms pushers = new BeaconArms(rightPusher, leftPusher, true);
                ColorSensorEvent event = (ColorSensorEvent) e;

                if (event.kind == EventKind.RED) {
                    // Hit with left pusher.
                    pushers.rightStow();
                    pushers.colorDeploy();
                } else if (event.kind == EventKind.BLUE) {
                    // Hit with right pusher.
                    pushers.leftStow();
                    pushers.rightDeploy();
                } else {
                    pushers.allStow();
                }

                climber.setPosition(NeverlandServoConstants.CLIMBER_SCORE);

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
        color = hardwareMap.colorSensor.get("color");
        core = hardwareMap.deviceInterfaceModule.get("interface");

        core.setDigitalChannelMode(LED_CHANNEL, DigitalChannelController.Mode.OUTPUT);
        core.setDigitalChannelState(LED_CHANNEL, false);

        // Servos.
        climber = hardwareMap.servo.get("climber");

        rightPusher = hardwareMap.servo.get("rightPusher");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightBumper = hardwareMap.servo.get("rightBumper");
        leftBumper = hardwareMap.servo.get("leftBumper");

        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
        leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
        climber.setPosition(NeverlandServoConstants.CLIMBER_STORE);

        pushers = new BeaconArms(rightPusher, leftPusher, true);

        // Motor controller.
        mc = hardwareMap.dcMotorController.get("motors");

        // Treads.
        rightTread = new Team25DcMotor(this, mc, 1);
        leftTread = new Team25DcMotor(this, mc, 2);
        //rightTread.startPeriodic();
        //leftTread.startPeriodic();

        // Class factory (motor controller).
        ClassFactory.createEasyMotorController(this, leftTread, rightTread);

        rightTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        rightTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        // Class: Dead reckon.
        deadReckon = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 38, 1.0);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, 1.0);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 51, 1.0);
        deadReckon.addSegment(DeadReckon.SegmentType.TURN, 45, 1.0);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 23, 1.0);

        // Class: Dead reckon (push beacon button).
        deadReckonPush = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 6, 1.0);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 6, -0.4);
    }

    @Override
    public void init_loop()
    {
        if (gyro.isCalibrating() == true) {
            telemetry.addData("Gyro", " is calibrating");
        } else {
            telemetry.addData("Gyro", " is not calibrating");
        }
    }

    @Override
    public void start()
    {
        gyro.resetZAxisIntegrator();

        monitorMotorTask = new MonitorMotorTask(this, leftTread);
        addTask(monitorMotorTask);

        deadReckonTask = new DeadReckonTask(this, deadReckon);
        addTask(deadReckonTask);

        monitorGyroTask = new MonitorGyroTask(this, gyro);
        addTask(monitorGyroTask);
    }

    @Override
    public void stop()
    {
        if (deadReckonTask != null) {
            deadReckonTask.stop();
        }
    }
}