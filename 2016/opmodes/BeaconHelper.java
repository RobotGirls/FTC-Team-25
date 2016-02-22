package opmodes;

import android.bluetooth.BluetoothClass;
import android.graphics.Color;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DeviceInterfaceModule;
import com.qualcomm.robotcore.hardware.Servo;

import team25core.ColorSensorTask;
import team25core.DeadReckon;
import team25core.DeadReckonTask;
import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;
import team25core.TwoWheelGearedDriveDeadReckon;

/*
 * FTC Team 5218: izzielau, February 18, 2016
 */

public class BeaconHelper {

    public enum Alliance {
        RED,
        BLUE,
    };

    public Robot robot;
    public ColorSensor color;
    public DeviceInterfaceModule core;
    public Servo rightPusher;
    public Servo leftPusher;
    public Servo climber;
    public DcMotor leftTread;
    public DcMotor rightTread;
    public DeadReckon deadReckonPush;
    public DeadReckonTask deadReckonPushTask;
    public Alliance alliance;
    public BeaconArms pushers;

    public int TICKS_PER_INCH = 0;
    public int TICKS_PER_DEGREE = 0;

    public BeaconHelper(Alliance alliance, Robot robot, ColorSensor color,
                        BeaconArms pushers, Servo climber,
                        DcMotor rightTread, DcMotor leftTread,
                        int ticksDegree, int ticksInch) {
        this.robot = robot;
        this.color = color;
        this.alliance = alliance;
        this.pushers = pushers;
        this.rightTread = rightTread;
        this.leftTread = leftTread;
        this.climber = climber;
        this.TICKS_PER_DEGREE = ticksDegree;
        this.TICKS_PER_INCH = ticksInch;
    }

    public void doBeaconWork()
    {
        pushers.colorDeploy();
        final SingleShotTimerTask sstt = new SingleShotTimerTask(robot, 1000) {
            @Override
            public void handleEvent(RobotEvent e) {
                pushAndDump();
            }
        };
        robot.addTask(sstt);
    }
    protected void pushAndDump() {
        robot.addTask(new ColorSensorTask(robot, color, core, true, true, 0) {
            public void handleEvent(RobotEvent e) {
                ColorSensorEvent event = (ColorSensorEvent) e;
                if (alliance == Alliance.RED) {
                    if (event.kind == EventKind.RED) {
                        // Hit with left pusher.
                        pushers.rightStow();
                        pushers.colorDeploy();
                        // Score.
                        pressBeacon();
                    } else if (event.kind == EventKind.BLUE) {
                        // Hit with right pusher.
                        pushers.leftStow();
                        pushers.rightDeploy();
                        // Score.
                        pressBeacon();
                    } else {
                        pushers.allStow();
                    }
                } else if (alliance == Alliance.BLUE) {
                    if (event.kind == EventKind.BLUE) {
                        // Hit with left pusher.
                        pushers.rightStow();
                        pushers.colorDeploy();
                        // Score.
                        pressBeacon();
                    } else if (event.kind == EventKind.RED) {
                        // Hit with right pusher.
                        pushers.leftStow();
                        pushers.rightDeploy();
                        // Score.
                        pressBeacon();
                    } else {
                        pushers.allStow();
                    }
                }
            }
        });
    }

    protected void pressBeacon () {
        climber.setPosition(NeverlandServoConstants.CLIMBER_SCORE);

        deadReckonPush = new TwoWheelGearedDriveDeadReckon(robot, TICKS_PER_INCH, TICKS_PER_DEGREE, leftTread, rightTread);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 1, 1.0);
        deadReckonPush.addSegment(DeadReckon.SegmentType.STRAIGHT, 5, -0.4);

        deadReckonPushTask = new DeadReckonTask(robot, deadReckonPush);
        robot.addTask(deadReckonPushTask);
    }
}
