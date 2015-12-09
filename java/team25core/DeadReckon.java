package team25core;

/*
 * FTC Team 25: cmacfarl, August 21, 2015
 */

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.LinkedList;
import java.util.Queue;

public abstract class DeadReckon {
    static int foo = 0;

    public enum SegmentType {
        STRAIGHT,
        TURN
    }

    public Queue<Segment> segments;
    protected int encoderTicksPerInch;
    protected GyroSensor gyro;
    protected DcMotor masterMotor;
    protected Segment currSegment;
    protected Robot robot;
    protected boolean turning;
    protected boolean setup;

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

    public DeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyro, DcMotor masterMotor)
    {
        this.encoderTicksPerInch = encoderTicksPerInch;
        this.gyro = gyro;
        this.masterMotor = masterMotor;
        this.robot = robot;
        this.currSegment = null;
        this.turning = false;
        this.setup = false;
        segments = new LinkedList<Segment>();
    }

    public void addSegment(SegmentType type, double distance, double speed)
    {
        segments.add(new Segment(type, distance, speed));
    }

    protected void consumeSegment()
    {
        if (currSegment.type == SegmentType.STRAIGHT) {
            RobotLog.i("251 Moving straight");
            motorStraight(currSegment.speed);
        } else {
            RobotLog.i("251 Turning " + currSegment.distance + " degrees");
            turning = true;
            robot.addTask(new GyroTask(robot, gyro, (int)currSegment.distance, true) {
                @Override
                public void handleEvent(RobotEvent e) {
                    GyroEvent event = (GyroEvent) e;

                    if (event.kind == EventKind.HIT_TARGET || event.kind == EventKind.PAST_TARGET) {
                        turning = false;
                        motorStraight(0);
                    } else if (event.kind == EventKind.THRESHOLD_80) {
                        motorTurn(Math.max(0.20, currSegment.speed * 0.10));
                    } else if (event.kind == EventKind.THRESHOLD_90) {
                        motorTurn(Math.max(0.20, currSegment.speed * 0.02));
                    }
                }
            });
            motorTurn(currSegment.speed);
        }
    }

    protected void setupSegment()
    {
        motorStraight(0.0);

        currSegment = segments.poll();
        setup = true;

        if (currSegment.type == SegmentType.STRAIGHT) {
            RobotLog.i("251 Setting up straight segment: " + foo);
            ResetMotorEncoderTask rmt = new ResetMotorEncoderTask(this.robot, masterMotor) {
                @Override
                public void handleEvent(RobotEvent e)
                {
                    RobotLog.i("251 Encoder reset done, consuming segment:" + foo);
                    resetEncoders((int) currSegment.distance * encoderTicksPerInch);
                    setup = false;
                    consumeSegment();
                }
            };
            this.robot.addTask(rmt);
        } else {
            RobotLog.i("251 Setting up turn segment: " + foo);
            ResetGyroHeadingTask rgt = new ResetGyroHeadingTask(this.robot, gyro) {
                @Override
                public void handleEvent(RobotEvent e)
                {
                    RobotLog.i("251 Gyro reset done, consuming segment:" + foo);
                    setup = false;
                    consumeSegment();
                }
            };
            this.robot.addTask(rgt);
        }
    }

    protected boolean consumingSegment()
    {
        if (setup) {
            return true;
        } else if (currSegment.type == SegmentType.STRAIGHT) {
            return (isBusy());
        } else {
            return turning;
        }
    }

    public boolean runPath()
    {
        if (currSegment == null) {
            setupSegment();
            return false;
        }

        if (!segments.isEmpty() && !consumingSegment()) {
            foo++;
            setupSegment();
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

