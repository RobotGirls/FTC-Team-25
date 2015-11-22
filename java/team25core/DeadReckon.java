package team25core;

/*
 * FTC Team 25: cmacfarl, August 21, 2015
 */

import com.qualcomm.robotcore.hardware.GyroSensor;

import java.util.LinkedList;
import java.util.Queue;

public abstract class DeadReckon {

    public enum SegmentType {
        STRAIGHT,
        TURN
    }

    public Queue<Segment> segments;
    protected int encoderTicksPerInch;
    protected GyroSensor gyro;
    protected Segment currSegment;
    protected Robot robot;
    protected boolean turning;

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

    public DeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyro)
    {
        this.encoderTicksPerInch = encoderTicksPerInch;
        this.gyro = gyro;
        this.robot = robot;
        this.currSegment = null;
        this.turning = false;
        segments = new LinkedList<Segment>();
    }

    public void addSegment(SegmentType type, double distance, double speed)
    {
        segments.add(new Segment(type, distance, speed));
    }

    protected void consumeSegment()
    {
        currSegment = segments.poll();

        if (currSegment.type == SegmentType.STRAIGHT) {
            resetEncoders((int)currSegment.distance * encoderTicksPerInch);
            motorStraight(currSegment.speed);
        } else {
            gyro.resetZAxisIntegrator();
            turning = true;
            robot.addTask(new GyroTask(robot, gyro, (int)currSegment.distance, true) {
                              @Override
                              public void handleEvent(RobotEvent e) {
                                  GyroEvent event = (GyroEvent) e;

                                  if (event.kind == EventKind.HIT_TARGET || event.kind == EventKind.PAST_TARGET) {
                                      motorStraight(0);
                                  } else if (event.kind == EventKind.THRESHOLD_80) {
                                      motorTurn(currSegment.speed * 0.10);
                                  } else if (event.kind == EventKind.THRESHOLD_90) {
                                      motorTurn(currSegment.speed * 0.02);
                                  }
                              }
                          });
            motorTurn(currSegment.speed);
        }
    }

    protected boolean consumingSegment()
    {
        if (currSegment.type == SegmentType.STRAIGHT) {
            return (isBusy());
        } else {
            return turning;
        }
    }

    public boolean runPath()
    {
        if (currSegment == null) {
            consumeSegment();
            return true;
        }

        if (!segments.isEmpty() && !consumingSegment()) {
            consumeSegment();
            return true;
        } else if (segments.isEmpty() && !consumingSegment()) {
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
        boolean segmentEmpty = segments.isEmpty();
        boolean busy = consumingSegment();

        return (segmentEmpty && !busy);
    }
}

