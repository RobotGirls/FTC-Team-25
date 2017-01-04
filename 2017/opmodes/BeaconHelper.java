package opmodes;

import android.bluetooth.BluetoothClass;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;

import team25core.ColorSensorTask;
import team25core.DeadReckonTask;
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
    private GeneralBeaconArms pushers;
    private ColorSensorTask senseColorTask;
    private ColorSensor color;
    private DeviceInterfaceModule cdim;

    public enum Alliance {
        RED,
        BLUE
    }

    public BeaconHelper(Robot robot, Alliance alliance, GeneralBeaconArms pushers, ColorSensor color, DeviceInterfaceModule cdim)
    {
        // Who knows, maybe this will have some more parameters at some point.
        // Until then, this.
        this.robot = robot;
        this.alliance = alliance;
        this.pushers = pushers;
        this.color = color;
        this.cdim = cdim;
    }

    public void doBeaconWork()
    {
        //  Kick off beacon work.

        robot.addTask(new ColorSensorTask(robot, color, cdim, false, true, 0) {
            @Override
            public void handleEvent(RobotEvent e) {
                ColorSensorTask.ColorSensorEvent event = (ColorSensorTask.ColorSensorEvent) e;

                // The GeneralBeaconArms class (pushers) will determine which pusher to deploy,
                // depending on whether or not you've sensed your alliance (e.g. red alliance,
                // sensed red).
                if (alliance == Alliance.RED) {
                    if (event.kind == ColorSensorTask.EventKind.RED) {
                        pushers.deploy(true);
                    } else if (event.kind == ColorSensorTask.EventKind.BLUE) {
                        pushers.deploy(false);
                    }
                } else if (alliance == Alliance.BLUE) {
                    if (event.kind == ColorSensorTask.EventKind.BLUE) {
                        pushers.deploy(true);
                    } else if (event.kind == ColorSensorTask.EventKind.RED) {
                        pushers.deploy(false);
                    }
                }

                robot.removeTask(senseColorTask);
            }
        });
    }
}