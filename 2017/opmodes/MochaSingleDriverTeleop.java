
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

@TeleOp(name="Baby Mocha", group = "5218")
@Disabled
public class MochaSingleDriverTeleop extends Robot {

    private final static int LED_CHANNEL = 0;

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor backLeft;
    private DcMotor backRight;
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

        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        backRight.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        backLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);

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
        DeadmanMotorTask collect = new DeadmanMotorTask(this, sbod, 0.15, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.RIGHT_BUMPER);
        addTask(collect);
        DeadmanMotorTask dispense = new DeadmanMotorTask(this, sbod, -0.15, GamepadTask.GamepadNumber.GAMEPAD_1, DeadmanMotorTask.DeadmanButton.RIGHT_TRIGGER);
        addTask(dispense);
    }
}
