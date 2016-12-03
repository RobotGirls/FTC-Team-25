package test;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcontroller.external.samples.SensorMRRangeSensor;

import opmodes.GeneralBeaconArms;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.TwoWheelGearedDriveDeadReckon;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/29/2016.
 */
@Autonomous(name = "Lameingo: Beacon Test", group = "Team25")
@Disabled
public class LameingoBeaconTest extends Robot
{
    DcMotor left;
    DcMotor right;
    ColorSensor color;
    SensorMRRangeSensor range;
    TwoWheelGearedDriveDeadReckon pushPath;
    DeadReckonTask pushTask;
    GeneralBeaconArms buttonPushers;
    Servo leftPusher;
    Servo rightPusher;


    @Override
    public void handleEvent(RobotEvent e)
    {
        DeadReckonTask.DeadReckonEvent event = (DeadReckonTask.DeadReckonEvent) e;
        if (event.kind == DeadReckonTask.EventKind.PATH_DONE) {
           // if red
           buttonPushers.deploy(true);
        }
    }

    @Override
    public void init()
    {
        pushPath = new TwoWheelGearedDriveDeadReckon(this, LameingoConfiguration.TICKS_PER_INCH,
               LameingoConfiguration.TICKS_PER_DEGREE, left, right);
        pushTask = new DeadReckonTask(this, pushPath);
        buttonPushers = new GeneralBeaconArms(leftPusher, rightPusher,LameingoConfiguration.LEFT_DEPLOY_POS,
                LameingoConfiguration.RIGHT_DEPLOY_POS, LameingoConfiguration.LEFT_STOW_POS,
                LameingoConfiguration.RIGHT_STOW_POS, true);
    }

    @Override
    public void start()
    {
       this.addTask(pushTask);
    }
}
