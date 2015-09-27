package team25core;

import com.qualcomm.robotcore.hardware.Gamepad;

/*
 * FTC Team 25: cmacfarl, September 26, 2015
 */

public class DeadmanMotorTask extends RobotTask {

    Gamepad gamepad;

    public DeadmanMotorTask(Robot robot, Gamepad gamepad)
    {
        super(robot);

        this.gamepad = gamepad;
    }

    @Override
    public void start() {

        robot.addTask(new GamepadTask(robot, gamepad));
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean timeslice() {
        return false;
    }
}
