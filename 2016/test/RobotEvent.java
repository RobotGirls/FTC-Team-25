package com.qualcomm.ftcrobotcontroller.opmodes;/*
 * FTC Team 25: cmacfarl, August 31, 2015
 */

public class RobotEvent
{
    /*
     * The task this event is associated with
     */
    protected RobotTask task;

    public RobotEvent(RobotTask task)
    {
        this.task = task;
    }

    public String toString()
    {
        return "RobotEvent: ";
    }
}
