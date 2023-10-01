package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorMRRangeSensor;

import opmodes.BeaconArms;
import team25core.ColorSensorTask;
import team25core.DeadReckonPath;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TwoWheelDirectDrivetrain;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/29/2016.
 */
@Autonomous(name = "Lameingo: Red Beacon Test", group = "Team25")
@Disabled
public class LameingoBeaconTest extends Robot
{
    DcMotor left;
    DcMotor right;
    ColorSensor color;
    DeviceInterfaceModule cdim;
    ColorSensorTask senseColorTask;
    SensorMRRangeSensor range;
    DeadReckonPath pushPath;
    DeadReckonTask pushTask;
    BeaconArms buttonPushers;
    Servo leftPusher;
    Servo rightPusher;
    TwoWheelDirectDrivetrain drivetrain;

    @Override
    public void handleEvent(RobotEvent e)
    {
        if (e instanceof DeadReckonTask.DeadReckonEvent) {
            DeadReckonTask.DeadReckonEvent event = (DeadReckonTask.DeadReckonEvent) e;

            if (event.kind == DeadReckonTask.EventKind.PATH_DONE) { // eventually, this will likely become "sensor satisfied".
                //  kick off beacon work.
                senseColorTask = new ColorSensorTask(this, color, cdim, false, 0);
                /**
                 * FIXME: You need a class where you are keeping all your constants.
                 */
                senseColorTask.setModeCompare(278);
                addTask(senseColorTask);
            }
        } else if (e instanceof ColorSensorTask.ColorSensorEvent) {
            ColorSensorTask.ColorSensorEvent event = (ColorSensorTask.ColorSensorEvent) e;

            if (event.kind == ColorSensorTask.EventKind.RED) {
                buttonPushers.deploy(true);
                removeTask(senseColorTask);
                // add single shot timer task to wait for n seconds (wait for beacon to be pressed)
            } else if (event.kind == ColorSensorTask.EventKind.BLUE) {
                buttonPushers.deploy(false);
                removeTask(senseColorTask);
                // add single shot timer task to wait for n seconds (wait for beacon to be pressed)
            }
        }
    }

    @Override
    public void init()
    {
        left = hardwareMap.dcMotor.get("leftMotor");
        right = hardwareMap.dcMotor.get("rightMotor");
        color = hardwareMap.colorSensor.get("color");
        cdim = hardwareMap.deviceInterfaceModule.get("interface");
        leftPusher = hardwareMap.servo.get("leftPusher");
        rightPusher = hardwareMap.servo.get("rightPusher");

        drivetrain = new TwoWheelDirectDrivetrain(LameingoConfiguration.TICKS_PER_INCH, right, left);

        leftPusher.setPosition(LameingoConfiguration.LEFT_STOW_POS);
        rightPusher.setPosition(LameingoConfiguration.RIGHT_STOW_POS);
        pushPath = new DeadReckonPath();
        pushTask = new DeadReckonTask(this, pushPath, drivetrain);
        buttonPushers = new BeaconArms(this, leftPusher, rightPusher,LameingoConfiguration.LEFT_DEPLOY_POS,
                LameingoConfiguration.RIGHT_DEPLOY_POS, LameingoConfiguration.LEFT_STOW_POS,
                LameingoConfiguration.RIGHT_STOW_POS, true);
    }

    @Override
    public void start()
    {
       this.addTask(pushTask);
    }
}
