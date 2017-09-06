package example;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;

import opmodes.Daisy;
import team25core.Alliance;
import team25core.FourWheelDirectDrivetrain;
import team25core.NavigateToTargetTask;
import team25core.Robot;
import team25core.RobotEvent;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 1/14/2017.
 */
@Autonomous(name = "Daisy: Vuforia Target Finding", group = "Team 25")
@Disabled
public class VuforiaNavigationExample extends Robot
{
    NavigateToTargetTask nttt;
    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor rearLeft;
    DcMotor rearRight;
    FourWheelDirectDrivetrain drivetrain;

    @Override
    public void init()
    {
        frontLeft   = hardwareMap.dcMotor.get("rearRight");
        frontRight  = hardwareMap.dcMotor.get("rearLeft");
        rearLeft    = hardwareMap.dcMotor.get("frontRight");
        rearRight   = hardwareMap.dcMotor.get("frontLeft");
        drivetrain = new FourWheelDirectDrivetrain(Daisy.TICKS_PER_INCH, frontRight, rearRight, frontLeft, rearLeft);
        nttt = new NavigateToTargetTask(this, drivetrain, NavigateToTargetTask.Targets.RED_NEAR, 300000, gamepad1, Alliance.RED);
        super.init();
    }

    @Override
    public void start()
    {
        super.start();
        addTask(nttt);
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        NavigateToTargetTask.NavigateToTargetEvent event = (NavigateToTargetTask.NavigateToTargetEvent) e;
        if (event.kind == NavigateToTargetTask.EventKind.FOUND_TARGET) {
            RobotLog.i("141 Found target");
        } else if (event.kind == NavigateToTargetTask.EventKind.TIMEOUT) {
            RobotLog.i("141 Timeout");
        }
    }
}
