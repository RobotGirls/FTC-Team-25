package team25core;
/*
 * FTC Team 25: cmacfarl, August 31, 2015
 */

public class RobotEvent
{
    /*
     * The task this event is associated with
     */
    protected RobotTask task;
    protected Robot robot;

    /*
     * For events associated with tasks.
     */
    public RobotEvent(RobotTask task)
    {
        this.task = task;
        this.robot = null;
    }

    /*
     * For events detached from a task.
     */
    public RobotEvent(Robot robot)
    {
        this.robot = robot;
        this.task = null;
    }

    /*
     * Call the event handler for the particular task/robot.  The
     * default event handler simply hands the task off the to
     * robot.  Events should not be associated with a robot and a
     * task at the same time.
     */
    public void handleEvent()
    {
        if (task != null) {
            task.handleEvent(this);
        } else if (robot != null) {
            robot.handleEvent(this);
        }
    }

    public String toString()
    {
        return "RobotEvent: ";
    }
}
