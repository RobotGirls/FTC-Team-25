
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
import team25core.TwoMotorDriveTask;

public class TankDriveTeleop extends Robot {

    private DcMotor leftTread;
    private DcMotor rightTread;
    private DcMotor hook;

    private Servo leftPusher;
    private Servo rightPusher;
    private Servo leftFlag;
    private Servo rightFlag;

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

        // Color.
        color = hardwareMap.colorSensor.get("color");
        core = hardwareMap.deviceInterfaceModule.get("interface");

        core.setDigitalChannelMode(LED_CHANNEL, DigitalChannelController.Mode.OUTPUT);
        core.setDigitalChannelState(LED_CHANNEL, false);

        // Servos.
        rightPusher = hardwareMap.servo.get("rightPusher");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightFlag = hardwareMap.servo.get("rightFlag");
        leftFlag = hardwareMap.servo.get("leftFlag");

        rightFlag.setPosition(NeverlandServoConstants.RIGHT_FLAG_STOWED);
        leftFlag.setPosition(NeverlandServoConstants.LEFT_FLAG_STOWED);
        addTask(new SingleShotTimerTask(this, 1500) {
            public void handleEvent(RobotEvent e)
            {
                rightPusher.setPosition(NeverlandServoConstants.RIGHT_PUSHER_STOWED);
                leftPusher.setPosition(NeverlandServoConstants.LEFT_PUSHER_STOWED);
            }
        });

        // Treads.
        rightTread = hardwareMap.dcMotor.get("rightTread");
        leftTread = hardwareMap.dcMotor.get("leftTread");

        leftTread.setDirection(DcMotor.Direction.REVERSE);

        rightTread.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);
        leftTread.setChannelMode(DcMotorController.RunMode.RUN_TO_POSITION);

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
        TwoMotorDriveTask drive = new TwoMotorDriveTask(this, rightTread, leftTread);
        this.addTask(drive);

        // Right bumper: extends hook.
        DeadmanMotorTask hookExtend = new DeadmanMotorTask(this, hook, 0.75, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_BUMPER);
        this.addTask(hookExtend);

        // Right trigger: retracts hook.
        DeadmanMotorTask hookRetract = new DeadmanMotorTask(this, hook, -0.75, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_TRIGGER);
        this.addTask(hookRetract);

        // Buttons: moves flags to position - (X) raise left, (A) lower left, (Y) raise right, (B) lower right.
        // Display: gyro heading - (LB) start task.
        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_2) {
            public void handleEvent(RobotEvent e)
            {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.BUTTON_X_DOWN) {
                    leftFlag.setPosition(NeverlandServoConstants.LEFT_FLAG_NINETY);
                } else if (event.kind == EventKind.BUTTON_A_DOWN) {
                    leftFlag.setPosition(NeverlandServoConstants.LEFT_FLAG_DEPLOYED);
                } else if (event.kind == EventKind.BUTTON_Y_DOWN) {
                    rightFlag.setPosition(NeverlandServoConstants.RIGHT_FLAG_NINETY);
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    rightFlag.setPosition(NeverlandServoConstants.RIGHT_FLAG_DEPLOYED);
                } else if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    displayEnabled = true;
                    display = new GyroTask(robot, gyro, 360, true);
                }
            }
        });

        if (displayEnabled) {
            addTask(display);
        }
    }
}
