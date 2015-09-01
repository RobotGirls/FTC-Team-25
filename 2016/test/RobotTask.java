package com.qualcomm.ftcrobotcontroller.opmodes;/*
 * FTC Team 25: cmacfarl, August 31, 2015
 */

public interface RobotTask {

    public void start();

    public void stop();

    /*
     * Perform work for this task.
     *
     * The task should return false if there is more work to
     * do, true otherwise.
     */
    public boolean timeslice();

}
