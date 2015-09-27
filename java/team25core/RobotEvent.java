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

    public RobotEvent(RobotTask task)
    {
        this.task = task;
    }

    /*
     * Call the event handler for the particular task.  The
     * default event handler simply hands the task off the to
     * robot.
     */
    public void handleEvent()
    {
        task.handleEvent(this);
    }

    public String toString()
    {
        return "RobotEvent: ";
    }
}
