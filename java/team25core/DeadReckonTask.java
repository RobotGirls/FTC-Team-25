package team25core;

/*
 * FTC Team 25: cmacfarl, September 01, 2015
 */

import com.qualcomm.robotcore.util.RobotLog;

public class DeadReckonTask extends RobotTask {

    public enum EventKind {
        SEGMENT_DONE,
        PATH_DONE,
    }

    public class DeadReckonEvent extends RobotEvent {

        public EventKind kind;
        public int segment_num;

        public DeadReckonEvent(RobotTask task, EventKind k, int segment_num)
        {
            super(task);
            kind = k;
            this.segment_num = segment_num;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "DeadReckon Event " + kind + " " + segment_num);
        }
    }

    protected DeadReckon dr;
    protected int num;
    protected boolean waiting;
    SingleShotTimerTask sst;
    int waitState = 0;

    public DeadReckonTask(Robot robot, DeadReckon dr)
    {
        super(robot);

        this.num = 0;
        this.dr = dr;
        this.waiting = false;
        this.waitState = 0;
    }

    @Override
    public void start()
    {
        // TODO: ??
    }

    @Override
    public void stop()
    {
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice()
    {
        DeadReckon.Segment segment;

        /*
         * Get current segment
         */
        segment = dr.getCurrentSegment();

        if (segment == null) {
            robot.queueEvent(new DeadReckonEvent(this, EventKind.PATH_DONE, num));
                /*
                 * Make sure it's stopped.
                 */
            RobotLog.i("251 Done with path, stopping all");
            dr.stop();
            return true;
        }

        switch (segment.state) {
        case INITIALIZE:
            dr.resetEncoders();
            segment.state = DeadReckon.SegmentState.ENCODER_RESET;
            break;
        case ENCODER_RESET:
            if (dr.areEncodersReset()) {
                segment.state = DeadReckon.SegmentState.SET_TARGET;
            } else {
                dr.resetEncoders();
            }
            break;
        case SET_TARGET:
            dr.encodersOn();
            dr.setTarget();
            segment.state = DeadReckon.SegmentState.CONSUME_SEGMENT;
            break;
        case CONSUME_SEGMENT:
            if (segment.type == DeadReckon.SegmentType.STRAIGHT) {
                dr.motorStraight(segment.speed);
            } else {
                dr.motorTurn(segment.speed);
            }
            segment.state = DeadReckon.SegmentState.ENCODER_TARGET;
            break;
        case ENCODER_TARGET:
            if (dr.hitTarget()) {
                segment.state = DeadReckon.SegmentState.STOP_MOTORS;
            }
            break;
        case STOP_MOTORS:
            dr.motorStraight(0.0);
            segment.state = DeadReckon.SegmentState.WAIT;
            waitState = 0;
        case WAIT:
            waitState++;
            /*
             * About 1/2 a second give or take, this is really stupid, nobody
             * would design a system like this from scratch.
             */
            if (waitState > 50) {
                segment.state = DeadReckon.SegmentState.DONE;
            }
        case DONE:
            num++;
            dr.nextSegment();
            segment.state = DeadReckon.SegmentState.INITIALIZE;
        }

        robot.telemetry.addData("Segment: ", num);
        robot.telemetry.addData("State: ", segment.state.toString());

        return false;
    }
}
