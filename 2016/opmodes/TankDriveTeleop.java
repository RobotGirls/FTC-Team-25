
package opmodes;

/*
 * FTC Team 25: izzielau, October 6, 2015
 */

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.DeadmanMotorTask;
import team25core.GamepadTask;
import team25core.GyroTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.TwoWheelDriveTask;

public class TankDriveTeleop extends Robot {

    private DcMotor leftTread;
    private DcMotor rightTread;
    private DcMotor hook;

    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftBumper;
    private Servo rightBumper;

    private DeviceInterfaceModule core;
    private GyroSensor gyro;
    private ColorSensor color;

    private GyroTask display;

    private final static int LED_CHANNEL = 0;
    private boolean displayEnabled = false;

    @Override
    public void init()
    {
        // Gyro.
        gyro = hardwareMap.gyroSensor.get("gyro");
        gyro.calibrate();

         // Servos.
        rightPusher = hardwareMap.servo.get("rightPusher");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightBumper = hardwareMap.servo.get("rightBumper");
        leftBumper = hardwareMap.servo.get("leftBumper");

        // Treads.
        rightTread = hardwareMap.dcMotor.get("rightTread");
        leftTread = hardwareMap.dcMotor.get("leftTread");

        rightTread.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        leftTread.setChannelMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        // Hook.
        hook = hardwareMap.dcMotor.get("hook");
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

        // Joystick control: controls two motor drive.
        TwoWheelDriveTask drive = new TwoWheelDriveTask(this, rightTread, leftTread);
        this.addTask(drive);

        // Right bumper: extends hook.
        DeadmanMotorTask hookExtend = new DeadmanMotorTask(this, hook, 1.0, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_BUMPER);
        this.addTask(hookExtend);

        // Right trigger: retracts hook.
        DeadmanMotorTask hookRetract = new DeadmanMotorTask(this, hook, -1.0, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_TRIGGER);
        this.addTask(hookRetract);

        // Buttons.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e)
            {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_X_DOWN) {
                    leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_DOWN);
                    rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_DOWN);
                } else if (event.kind == EventKind.BUTTON_A_DOWN) {
                    leftBumper.setPosition(NeverlandServoConstants.LEFT_BUMPER_UP);
                    rightBumper.setPosition(NeverlandServoConstants.RIGHT_BUMPER_UP);
                } else if (event.kind == EventKind.BUTTON_Y_DOWN) {
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                }
            }
        });
    }
}
