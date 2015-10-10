package team25core;

/*
 * FTC Team 25: cmacfarl, August 31, 2015
 */


public abstract class RobotTask {

    protected Robot robot;
    public RobotTask(Robot robot)
    {
        this.robot = robot;
    }

    public abstract void start();
    public abstract void stop();

    public void handleEvent(RobotEvent e)
    {
        robot.handleEvent(e);
    }

    /*
     * Perform work for this task.
     *
     * The task should return false if there is more work to
     * do, true otherwise.
     */
    public abstract boolean timeslice();


}
