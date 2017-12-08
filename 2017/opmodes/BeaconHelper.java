package opmodes;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.util.RobotLog;

import team25core.ColorSensorTask;
import team25core.DeadReckonPath;
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
    private BeaconArms pushers;
    private ContinuousBeaconArms continuousPushers;
    private ColorSensorTask senseColorTask;
    private ColorSensor color;
    private DeviceInterfaceModule cdim;
    private DeadReckonPath pushBeacon;
    private DaisyBeaconAutonomous dba;

    public enum Alliance {
        RED,
        BLUE
    }

    public BeaconHelper(DaisyBeaconAutonomous dba, Robot robot, Alliance alliance, BeaconArms pushers, ColorSensor color, DeviceInterfaceModule cdim)
    {
        this.robot = robot;
        this.alliance = alliance;
        this.pushers = pushers;
        this.color = color;
        this.cdim = cdim;
        this.dba = dba;
    }

    public BeaconHelper(Robot robot, Alliance alliance, ContinuousBeaconArms pushers, ColorSensor color, DeviceInterfaceModule cdim)
    {
        this.robot = robot;
        this.alliance = alliance;
        this.continuousPushers = continuousPushers;
        this.color = color;
        this.cdim = cdim;
    }

    public void doBeaconWork()
    {
        //  Kick off beacon work.
        RobotLog.i("141 Ready to sense color.");
        ColorSensorTask colorSensorTask = new ColorSensorTask(robot, color, cdim, false, 0) {
            @Override
            public void handleEvent(RobotEvent e) {
                ColorSensorTask.ColorSensorEvent event = (ColorSensorTask.ColorSensorEvent) e;

                // The BeaconArms class (pushers) will determine which pusher to deploy,
                // depending on whether or not you've sensed your alliance (e.g. red alliance,
                // sensed red).

                /*
                if (event.kind == ColorSensorTask.EventKind.YES) {
                    pushers.deploy(true);
                } else if (event.kind == ColorSensorTask.EventKind.NO) {
                    pushers.deploy(false);
                }*/

                if (alliance == Alliance.RED) {
                    if (event.kind == EventKind.RED) {
                        RobotLog.i("141 Sensed RED");
                       pushers.deploy(true);
                    } else {
                        RobotLog.i("141 Sensed BLUE");
                        pushers.deploy(false);
                    }
                } else {
                    if (event.kind == EventKind.BLUE) {
                        RobotLog.i("141 Sensed BLUE");
                        pushers.deploy(true);
                    } else {
                        RobotLog.i("141 Sensed RED");
                        pushers.deploy(false);
                    }
                }
                waitAndStow();
                dba.goPushBeacon();

                robot.removeTask(senseColorTask);
            }
        };


        if (alliance == Alliance.RED) {
            colorSensorTask.setModeCompare(Daisy.RED_THRESHOLD);
        } else {
            colorSensorTask.setModeCompare(Daisy.BLUE_THRESHOLD);
        }


        colorSensorTask.setModeCompare(Daisy.RED_THRESHOLD);
        colorSensorTask.setMsDelay(Daisy.COLOR_MS_DELAY);
        colorSensorTask.setReflectColor(true, robot.hardwareMap);

        cdim.setDigitalChannelMode(Daisy.COLOR_PORT, DigitalChannelController.Mode.OUTPUT);
        cdim.setDigitalChannelState(Daisy.COLOR_PORT, false);

        robot.addTask(colorSensorTask);
    }

    private void waitAndStow()
    {
        robot.addTask(new SingleShotTimerTask(robot, 5000) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                RobotLog.i("141 Stowing all pushers.");
                pushers.stowAll();
            }
        });
    }
}