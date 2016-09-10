package team25core;

/*
 * FTC Team 5218: izzielau, March 14, 2016
 */

public class AutonomousEvent extends RobotEvent {
    public enum EventKind {
        BEACON_DONE,
    }

    public EventKind kind;

    public AutonomousEvent(Robot robot, EventKind kind) {
        super(robot);
        this.kind = kind;
    }
}

