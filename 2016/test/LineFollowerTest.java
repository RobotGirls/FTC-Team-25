package test;

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RobotLog;

import org.swerverobotics.library.ClassFactory;
import org.swerverobotics.library.interfaces.Autonomous;
import org.swerverobotics.library.interfaces.Disabled;

import opmodes.NeverlandLightConstants;
import opmodes.NeverlandMotorConstants;
import opmodes.NeverlandServoConstants;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.LightSensorCriteria;
import team25core.LineFollowerTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.Team25DcMotor;
import team25core.TwoWheelGearedDriveDeadReckon;

/*
 * FTC Team 5218: izzielau, January 20, 2016
 */

@Autonomous(name = "TEST Line Follower")
@Disabled
public class LineFollowerTest extends Robot {

    protected final static int TICKS_PER_DEGREE = NeverlandMotorConstants.ENCODER_TICKS_PER_DEGREE;
    protected final static int TICKS_PER_INCH = NeverlandMotorConstants.ENCODER_TICKS_PER_INCH;

    protected final static int LIGHT_MIN = NeverlandLightConstants.LIGHT_MINIMUM;
    protected final static int LIGHT_MAX = NeverlandLightConstants.LIGHT_MAXIMUM;

    private final static int LED_CHANNEL = 0;

    private DcMotorController mc;
    private Team25DcMotor leftTread;
    private Team25DcMotor rightTread;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private LightSensor light;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;
    private Servo climber;

    private LightSensorCriteria whiteCriteria;
    private LightSensorCriteria blackCriteria;

    private DeadReckon deadReckon;
    private DeadReckonTask deadReckonTask;
    private DeadReckon passLine;
    private DeadReckonTask passLineTask;
    private LineFollowerTask followLine;

    private PersistentTelemetryTask telemetryTask = new PersistentTelemetryTask(this);

    @Override
    public void handleEvent(RobotEvent e) {

    }

    public void handleDeadReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case SEGMENT_DONE:
                telemetryTask.addData("Segment " + e.segment_num, "Done");
                break;
            case SENSOR_SATISFIED:
                RobotLog.i("251 LF Sensor satisfied (initial), look for black");
                passLineTask = new DeadReckonTask(this, passLine, blackCriteria) {
                    public void handleEvent(RobotEvent e) {
                        handlePassReckonEvent((DeadReckonTask.DeadReckonEvent)e);
                    }
                };
                addTask(passLineTask);
                break;
        }
    }

    public void handlePassReckonEvent(DeadReckonTask.DeadReckonEvent e) {
        switch (e.kind) {
            case SEGMENT_DONE:
                telemetryTask.addData("Segment " + e.segment_num, "Done");
                break;
            case SENSOR_SATISFIED:
                // If black is found, follow line.
                RobotLog.i("251 LF Sensor satisfied (pass), follow line");
                followLine = new LineFollowerTask(this, light, leftTread, rightTread, TICKS_PER_INCH, 20, true);
                addTask(followLine);
                break;
        }
    }

    @Override
    public void init() {

        // Gyro.
        gyro = (ModernRoboticsI2cGyro)hardwareMap.gyroSensor.get("gyro");
        gyro.setHeadingMode(ModernRoboticsI2cGyro.HeadingMode.HEADING_CARDINAL);
        gyro.calibrate();

        // Color.
        color = hardwareMap.colorSensor.get("color");
        core = hardwareMap.deviceInterfaceModule.get("interface");

        core.setDigitalChannelMode(LED_CHANNEL, DigitalChannelController.Mode.OUTPUT);
        core.setDigitalChannelState(LED_CHANNEL, false);

        // Light.
        light = hardwareMap.lightSensor.get("light");
        light.enableLed(true);

        // Light criteria.
        whiteCriteria = new LightSensorCriteria(light, LIGHT_MIN, LIGHT_MAX);
        // blackCriteria = new LightSensorCriteria(light, LightSensorCriteria.LightPolarity.BLACK, LIGHT_MIN, LIGHT_MAX);

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

        // Motor controller.
        mc = hardwareMap.dcMotorController.get("motors");

        // Treads.
        rightTread = new Team25DcMotor(this, mc, 1);
        leftTread = new Team25DcMotor(this, mc, 2);

        // Class factory (motor controller).
        ClassFactory.createEasyMotorController(this, leftTread, rightTread);

        rightTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        rightTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        leftTread.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        // Dead reckon: target line.
        deadReckon = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckon.addSegment(DeadReckon.SegmentType.STRAIGHT, 10, 0.5);

        // Dead reckon: pass line.
        passLine = new TwoWheelGearedDriveDeadReckon(this, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        passLine.addSegment(DeadReckon.SegmentType.STRAIGHT, 8, 0.5);
    }

    @Override
    public void start() {
        // Dead reckon task.
        deadReckonTask = new DeadReckonTask(this, deadReckon, whiteCriteria) {
            public void handleEvent(RobotEvent e) {
                handleDeadReckonEvent((DeadReckonTask.DeadReckonEvent) e);
            }
        };
        addTask(deadReckonTask);
    }
}
