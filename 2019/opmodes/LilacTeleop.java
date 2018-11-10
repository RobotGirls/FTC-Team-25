package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

import team25core.FourWheelDirectDrivetrain;
import team25core.MecanumWheelDriveTask;
import team25core.MechanumGearedDrivetrain;
import team25core.OneWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TankDriveTask;

/*
 * FTC Team 25: Created by Elizabeth, November 03, 2018
 */
@TeleOp(name="Lilac Teleop", group="Team 25")
@Disabled
public class LilacTeleop extends Robot {

    private enum Direction {
        CLOCKWISE,
        COUNTERCLOCKWISE,
    }

    private DcMotor frontLeft;
    private DcMotor frontRight;
    private DcMotor rearLeft;
    private DcMotor rearRight;
    //private DcMotor latchArm;

    private FourWheelDirectDrivetrain drivetrain;
    private MecanumWheelDriveTask drive;
    private OneWheelDriveTask driveArm;

    @Override
    public void init() {
        // Hardware mapping.
        rearLeft  = hardwareMap.dcMotor.get("frontLeft");
        rearRight = hardwareMap.dcMotor.get("frontRight");
        frontLeft   = hardwareMap.dcMotor.get("rearLeft");
        frontRight  = hardwareMap.dcMotor.get("rearRight");
       // latchArm        = hardwareMap.dcMotor.get("arm");

        // Reset encoders.
        frontLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);

    }

    @Override
    public void handleEvent(RobotEvent e) {
        // Nothing
    }

    @Override
    public void start() {

        drive = new MecanumWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);

        this.addTask(drive);
    }

}


