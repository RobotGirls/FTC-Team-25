package com.qualcomm.ftcrobotcontroller.opmodes;/*
 * FTC Team 25: cmacfarl, August 31, 2015
 */

public class RobotEvent
{
    protected Robot robot;

    public RobotEvent(Robot r)
    {
        robot = r;
    }

    public void handleEvent()
    {
        robot.handleEvent(this);
    }

    public String toString()
    {
        return "RobotEvent: ";
    }
}
