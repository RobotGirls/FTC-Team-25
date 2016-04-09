
package opmodes;

/*
 * FTC Team 5218: izzielau, October 6, 2015
 */

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.swerverobotics.library.ClassFactory;
import org.swerverobotics.library.interfaces.TeleOp;

import team25core.DeadmanMotorTask;
import team25core.GamepadTask;
import team25core.LightSensorCriteria;
import team25core.MonitorGyroTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TwoWheelDriveTask;

@TeleOp(name="Caffeine Teleop", group = "5218")
public class CaffeineTeleop extends Robot {

    private final static int LED_CHANNEL = 0;

    private DcMotorController mc;
    private DcMotor leftTread;
    private DcMotor rightTread;
    private DcMotor hook;
    private DeviceInterfaceModule core;
    private ModernRoboticsI2cGyro gyro;
    private ColorSensor color;
    private LightSensor frontLight;
    private LightSensor backLight;
    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;
    private Servo climber;

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

        // Light.
        frontLight = hardwareMap.lightSensor.get("frontLight");
        frontLight.enableLed(false);
        backLight = hardwareMap.lightSensor.get("backLight");
        backLight.enableLed(false);

        // Servos.
        rightPusher = hardwareMap.servo.get("rightPusher");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightBumper = hardwareMap.servo.get("rightBumper");
        leftBumper = hardwareMap.servo.get("leftBumper");
        climber = hardwareMap.servo.get("climber");

        rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
        leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
        rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
        leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
        climber.setPosition(NeverlandServoConstants.CLIMBER_STORE);

        // Motor controller.
        mc = hardwareMap.dcMotorController.get("motors");

        // Treads.
        rightTread = hardwareMap.dcMotor.get("rightTread");
        leftTread = hardwareMap.dcMotor.get("leftTread");

        // Class factory.
        ClassFactory.createEasyMotorController(this, leftTread, rightTread);
        leftTread.setDirection(DcMotor.Direction.REVERSE);

        rightTread.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        leftTread.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        // Hook.
        hook = hardwareMap.dcMotor.get("hook");
        hook.setDirection(DcMotor.Direction.REVERSE);
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        // No-op.
    }

    @Override
    public void start()
    {
        super.start();

        // Display: gyro heading.
        final MonitorGyroTask displayHeading = new MonitorGyroTask(this, gyro);
        this.addTask(displayHeading);

        // Joystick control: controls two motor drive.
        final TwoWheelDriveTask drive = new TwoWheelDriveTask(this, rightTread, leftTread);
        this.addTask(drive);

        // Right bumper: extends hook.
        DeadmanMotorTask hookExtend = new DeadmanMotorTask(this, hook, 1.0, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_BUMPER);
        this.addTask(hookExtend);

        // Right trigger: retracts hook.
        DeadmanMotorTask hookRetract = new DeadmanMotorTask(this, hook, -1.0, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_TRIGGER);
        this.addTask(hookRetract);

        // Buttons: moves flags to position - (X) raise left, (A) lower left, (Y) raise right, (B) lower right.
        // Display: gyro heading - (LB) start task.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e)
            {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_UP);
                    rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_UP);
                } else if (event.kind == EventKind.BUTTON_X_DOWN) {
                    leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
                    rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    climber.setPosition(NeverlandServoConstants.CLIMBER_STORE);
                } else if (event.kind == EventKind.BUTTON_A_DOWN) {
                    climber.setPosition(NeverlandServoConstants.CLIMBER_SCORE);
                }
            }
        });

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_A_DOWN) {
                    drive.slow(true);
                } else if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    drive.slow(false);
                } else if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    displayHeading.reset();
                }
            }
        });
    }
}
