
package opmodes;

/*
 * FTC Team 5218: izzielau, October 30, 2016
 */

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.DeadmanMotorTask;
import team25core.FourWheelDriveTask;
import team25core.GamepadTask;
import team25core.PersistentTelemetryTask;
import team25core.Robot;
import team25core.RobotEvent;

@TeleOp(name="5218 Mocha", group = "5218")
public class MochaTeleop extends Robot {

    private final static int LED_CHANNEL = 0;

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
    private DcMotor shooterLeft;
    private DcMotor shooterRight;
    private DcMotor sbod;
    private Servo beacon;

    private PersistentTelemetryTask ptt;

    @Override
    public void init()
    {

        // Drivetrain.
        frontRight = hardwareMap.dcMotor.get("motorFR");
        frontLeft = hardwareMap.dcMotor.get("motorFL");
        backRight = hardwareMap.dcMotor.get("motorBR");
        backLeft = hardwareMap.dcMotor.get("motorBL");

        // Class factory.
        // ClassFactory.createEasyMotorController(this, leftTread, rightTread);

        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        shooterLeft = hardwareMap.dcMotor.get("shooterLeft");
        shooterRight = hardwareMap.dcMotor.get("shooterRight");

        shooterLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooterLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        shooterRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        shooterRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        // Hook.
        sbod = hardwareMap.dcMotor.get("brush");

        // Servo.
        beacon = hardwareMap.servo.get("beacon");

        ptt = new PersistentTelemetryTask(this);
        addTask(ptt);
    }

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public void start() {
        super.start();

        /* DRIVER ONE */
        // Four motor drive.
        final FourWheelDriveTask drive = new FourWheelDriveTask(this, frontLeft, frontRight, backLeft, backRight);
        this.addTask(drive);

        // SBOD
        DeadmanMotorTask collect = new DeadmanMotorTask(this, sbod, 0.7, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_BUMPER);
        addTask(collect);
        DeadmanMotorTask dispense = new DeadmanMotorTask(this, sbod, -0.7, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.RIGHT_TRIGGER);
        addTask(dispense);

        DeadmanMotorTask collectSlow = new DeadmanMotorTask(this, sbod, 0.35, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.LEFT_BUMPER);
        addTask(collectSlow);
        DeadmanMotorTask dispenseSlow = new DeadmanMotorTask(this, sbod, -0.35, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.LEFT_TRIGGER);
        addTask(dispenseSlow);

        // Shooters
        DeadmanMotorTask shootFastLeft = new DeadmanMotorTask(this, shooterLeft, 0.3, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_X);
        addTask(shootFastLeft);
        DeadmanMotorTask shootFastRight = new DeadmanMotorTask(this, shooterRight, -0.3, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_X);
        addTask(shootFastRight);
        DeadmanMotorTask shootLeft = new DeadmanMotorTask(this, shooterLeft, 0.2, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_Y);
        addTask(shootLeft);
        DeadmanMotorTask shootRight = new DeadmanMotorTask(this, shooterRight, -0.2, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_Y);
        addTask(shootRight);
        DeadmanMotorTask shootSlowLeft = new DeadmanMotorTask(this, shooterLeft, 0.15, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_A);
        addTask(shootSlowLeft);
        DeadmanMotorTask shootSlowRight = new DeadmanMotorTask(this, shooterRight, -0.15, GamepadTask.GamepadNumber.GAMEPAD_2, DeadmanMotorTask.DeadmanButton.BUTTON_A);
        addTask(shootSlowRight);

        this.addTask(new GamepadTask(this, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e) {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    beacon.setPosition(1.0);
                } else if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    beacon.setPosition(0);
                } else if (event.kind == EventKind.BUTTON_B_DOWN) {
                    drive.slowDown(true);
                    drive.slowDown(0.25);
                } else if (event.kind == EventKind.BUTTON_A_DOWN) {
                    drive.slowDown(true);
                    drive.slowDown(1.0);
                }
            }
        });
    }

}
