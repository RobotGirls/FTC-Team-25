package test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import team25core.MecanumWheelDriveTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/26/2016.
 */

@TeleOp(name = "Daisy: Mecanum Wheel Test", group = "Team25")
public class MecanumDriveTest extends Robot
{
    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor rearLeft;
    DcMotor rearRight;

    MecanumWheelDriveTask drive;

    @Override
    public void handleEvent(RobotEvent e)
    {
       // Nothing.
    }

    @Override
    public void init()
    {
        frontLeft = hardwareMap.dcMotor.get("frontLeft");
        frontRight = hardwareMap.dcMotor.get("frontRight");
        rearLeft = hardwareMap.dcMotor.get("rearLeft");
        rearRight = hardwareMap.dcMotor.get("rearRight");
        frontLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        rearLeft.setDirection(DcMotorSimple.Direction.REVERSE);
        frontRight.setDirection(DcMotorSimple.Direction.REVERSE);
        rearRight.setDirection(DcMotorSimple.Direction.REVERSE);
    }

    @Override
    public void start()
    {
        drive = new MecanumWheelDriveTask(this, frontLeft, frontRight, rearLeft, rearRight);
        this.addTask(drive);
    }
}
