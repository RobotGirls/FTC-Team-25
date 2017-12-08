package opmodes;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import team25core.Alliance;
import team25core.FourWheelDirectDrivetrain;
import team25core.NavigateToTargetTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.RobotNavigation;
import team25core.TwoWheelDirectDrivetrain;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 1/14/2017.
 */
@Autonomous(name = "TEST Vuforia", group = "Team 25")
@Disabled
public class DaisyVuforiaOrientation extends Robot
{
    enum TargetState {
        FIND_TARGET,
        LOST_TARGET,
        INITIAL_APPROACH,
        AT_TARGET,
        ALIGNED,
    }

    DcMotor frontLeft;
    DcMotor frontRight;
    DcMotor rearLeft;
    DcMotor rearRight;
    FourWheelDirectDrivetrain drivetrain;
    NavigateToTargetTask nttt;
    RobotNavigation nav;
    TargetState state;
    double bearing;

    private static final VuforiaLocalizer.CameraDirection CAMERA_CHOICE = Daisy.CAMERA_CHOICE;

    @Override
    public void init()
    {
        frontLeft   = hardwareMap.dcMotor.get("rearRight");
        frontRight  = hardwareMap.dcMotor.get("rearLeft");
        rearLeft    = hardwareMap.dcMotor.get("frontRight");
        rearRight   = hardwareMap.dcMotor.get("frontLeft");
        frontLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        frontRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearLeft.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rearRight.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        drivetrain = new FourWheelDirectDrivetrain(frontRight, rearRight, frontLeft, rearLeft);
        nttt = new NavigateToTargetTask(this, drivetrain, NavigateToTargetTask.Targets.RED_FAR, 1000000, gamepad1, Alliance.RED);

        // *rap break*
        // yo yo yo
        // cappuccino
        // lizzie plays the piano
        // hellooooooooo!!!!
        // chipotle from zzzzeeee
        // to be or not to be
        // that's the question for me
        // lizzie chews gum every day

        nav = new RobotNavigation(this, drivetrain);
        VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();
        parameters.vuforiaLicenseKey = Daisy.KEY;
        parameters.cameraDirection = CAMERA_CHOICE;
        parameters.useExtendedTracking = false;

        VuforiaLocalizer vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        VuforiaTrackables targets = vuforia.loadTrackablesFromAsset("FTC_2016-17");
        targets.get(0).setName("Blue Near");
        targets.get(1).setName("Red Far");
        targets.get(2).setName("Blue Far");
        targets.get(3).setName("Red Near");

        OpenGLMatrix phoneLocationOnRobot = Daisy.PHONE_LOCATION_ON_ROBOT;

        nttt.init(targets, parameters, phoneLocationOnRobot);
    }

    @Override
    public void start()
    {
        this.addTask(nttt);
        //nttt.findTarget();
    }

    @Override
    public void handleEvent(RobotEvent e)
    {
        //Nothing.
    }
}
