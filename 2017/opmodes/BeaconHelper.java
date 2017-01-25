package opmodes;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.ColorSensorTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

/**
 * FTC Team 25: Created by Katelyn Biesiadecki on 12/7/2016.
 */

public class BeaconHelper
{
    private Robot robot;
    private Alliance alliance;
    private ContinuousBeaconArms pushers;
    private ColorSensorTask senseColorTask;
    private ColorSensor color;
    private DeviceInterfaceModule cdim;

    public enum Alliance {
        RED,
        BLUE
    }

    public BeaconHelper(Robot robot, Alliance alliance, ContinuousBeaconArms pushers, ColorSensor color, DeviceInterfaceModule cdim)
    {
        this.robot = robot;
        this.alliance = alliance;
        this.pushers = pushers;
        this.color = color;
        this.cdim = cdim;
    }

    public void doBeaconWork()
    {
        //  Kick off beacon work.
        RobotLog.i("141 Ready to sense color.");
        robot.addTask(new ColorSensorTask(robot, color, cdim, false, true, 0) {
            @Override
            public void handleEvent(RobotEvent e) {
                ColorSensorTask.ColorSensorEvent event = (ColorSensorTask.ColorSensorEvent) e;

                // The BeaconArms class (pushers) will determine which pusher to deploy,
                // depending on whether or not you've sensed your alliance (e.g. red alliance,
                // sensed red).
                if (alliance == Alliance.RED) {
                    if (event.kind == ColorSensorTask.EventKind.RED) {
                        pushers.deploy(true);
                        RobotLog.i("141 Detecting red.");
                    } else if (event.kind == ColorSensorTask.EventKind.BLUE) {
                        pushers.deploy(false);
                    }
                } else if (alliance == Alliance.BLUE) {
                    if (event.kind == ColorSensorTask.EventKind.BLUE) {
                        pushers.deploy(true);
                        RobotLog.i("141 Detecting blue.");
                    } else if (event.kind == ColorSensorTask.EventKind.RED) {
                        pushers.deploy(false);
                    }
                }

                waitAndStow();

                robot.removeTask(senseColorTask);
            }
        });
    }

    private void waitAndStow()
    {
        robot.addTask(new SingleShotTimerTask(robot, 3000) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                RobotLog.i("141 Stowing all pushers.");
                pushers.stowAll();
            }
        });
    }
}