package test;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import team25core.Robot;
import team25core.RobotEvent;
import team25core.TwoWheelDriveTask;
import team25core.TwoWheelGearedDriveDeadReckon;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/12/2016.
 */

@TeleOp(name="Lameingo Teleop", group="Team25")
public class LameingoTeleop extends Robot {
    DcMotor left;
    DcMotor right;
    TwoWheelDriveTask drive;

    @Override
    public void handleEvent(RobotEvent e)
    {
        // Nothing.
    }

    @Override
    public void init()
    {
        left = hardwareMap.dcMotor.get("leftMotor");
        right = hardwareMap.dcMotor.get("rightMotor");
        right.setDirection(DcMotorSimple.Direction.REVERSE);
        left.setDirection(DcMotorSimple.Direction.FORWARD);
        drive = new TwoWheelDriveTask(this, right, left);
    }

    @Override
    public void start()
    {
       this.addTask(drive);
    }

}
