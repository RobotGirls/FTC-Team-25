package com.qualcomm.ftcrobotcontroller.opmodes;/*
 * FTC Team 25: cmacfarl, September 01, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;

public class MonitorMotorTask implements RobotTask {

    /*
     * No Events.
     */

    protected Robot robot;
    protected DcMotor motor;

    public MonitorMotorTask(Robot robot, DcMotor motor)
    {
        this.motor = motor;
        this.robot = robot;
    }

    @Override
    public void start()
    {

    }

    @Override
    public void stop()
    {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice() {

        robot.telemetry.addData("Position: ", Math.abs(motor.getCurrentPosition()));
        robot.telemetry.addData("Target: ", Math.abs(motor.getTargetPosition()));

        /*
         * Never stops.
         */
        return false;
    }
}
