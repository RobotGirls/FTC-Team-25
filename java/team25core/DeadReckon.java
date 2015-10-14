package team25core;
/*
 * FTC Team 25: cmacfarl, August 21, 2015
 */

import java.util.LinkedList;
import java.util.Queue;

public abstract class DeadReckon {

    public enum SegmentType {
        STRAIGHT,
        TURN
    }

    public Queue<Segment> segments;
    protected int encoderTicksPerInch;
    protected double encoderTicksPerDegree;

    public class Segment {

        protected SegmentType type;
        public double distance;
        public double speed;

        Segment(SegmentType type, double distance, double speed)
        {
            this.distance = distance;
            this.type = type;
            this.speed = speed;
        }
    }

    /*
     * The abstract functions are provided by the bot.
     */
    protected abstract void resetEncoders(int ticks);
    protected abstract void motorStraight(double speed);
    protected abstract void motorTurn(double speed);
    protected abstract boolean isBusy();

    public DeadReckon(int encoderTicksPerInch, double encoderTicksPerDegree)
    {
        this.encoderTicksPerInch = encoderTicksPerInch;
        this.encoderTicksPerDegree = encoderTicksPerDegree;
        segments = new LinkedList<Segment>();
    }

    public void addSegment(SegmentType type, double distance, double speed)
    {
        segments.add(new Segment(type, distance, speed));
    }

    protected void consumeSegment()
    {
        Segment seg = segments.poll();


        if (seg.type == SegmentType.STRAIGHT) {
            resetEncoders((int)seg.distance * encoderTicksPerInch);
            motorStraight(seg.speed);
        } else {
            resetEncoders((int)(seg.distance * encoderTicksPerDegree));
            motorTurn(seg.speed);
        }
    }

    public boolean runPath()
    {
        if (!segments.isEmpty() && !isBusy()) {
            consumeSegment();
            return true;
        } else if (segments.isEmpty() && !isBusy()) {
            motorStraight(0.0);
        }
        return false;
    }

    public void stop()
    {
        /*
         * Remove all remaining segments and stop the motors.
         */
        segments.clear();
        motorStraight(0.0);
        resetEncoders(0);
    }

    boolean done()
    {
        return (segments.isEmpty() && !isBusy());
    }
}

