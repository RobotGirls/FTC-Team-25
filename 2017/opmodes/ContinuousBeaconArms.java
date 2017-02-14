package opmodes;

/*
 * FTC Team 25: Created by Katelyn Biesiadecki on 11/8/2016. Wahoo!
 */

import com.qualcomm.robotcore.hardware.Servo;

import team25core.Robot;
import team25core.RobotEvent;
import team25core.SingleShotTimerTask;

public class ContinuousBeaconArms
{
    Robot robot;
    Servo right;
    Servo left;
    boolean sensorOnLeft;

    public ContinuousBeaconArms(Robot robot, Servo left, Servo right, boolean isSensorOnLeft) {
        this.left = left;
        this.right = right;
        this.sensorOnLeft = isSensorOnLeft;
        this.robot = robot;
    }

    public void deploy(boolean sensedMyAlliance) {
        // If your alliance color is sensed (e.g. red alliance, red is sensed) and
        // your sensor is on the left, deploy the left arm, and so on.

        if (sensedMyAlliance == sensorOnLeft) {
            deployLeft();
        } else {
            deployRight();
        }
    }

    public void deployLeft()
    {
        left.setPosition(1.0);
        robot.addTask(new SingleShotTimerTask(robot, 3000) {
           @Override
            public void handleEvent(RobotEvent e)
           {
                left.setPosition(0.5);
           }
        });
    }

    public void deployRight()
    {
        right.setPosition(1.0);
        robot.addTask(new SingleShotTimerTask(robot, 3000) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                right.setPosition(0.5);
            }
        });
    }

    public void stowLeft()
    {
        left.setPosition(0.0);
        robot.addTask(new SingleShotTimerTask(robot, 3000) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                left.setPosition(0.5);
            }
        });
    }

    public void stowRight()
    {
        right.setPosition(0.0);
        robot.addTask(new SingleShotTimerTask(robot, 3000) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                left.setPosition(0.5);
            }
        });
    }

    public void stowAll()
    {
        stowLeft();
        stowRight();
    }
}
