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
        TURN,
        SIDEWAYS,
        DIAGONAL
    }

    public enum SegmentState {
        INITIALIZE,
        ENCODER_RESET,
        SET_TARGET,
        CONSUME_SEGMENT,
        ENCODER_TARGET,
        STOP_MOTORS,
        WAIT,
        DONE,
    }

    protected enum TurnKind {
        TURN_USING_ENCODERS,
        TURN_USING_GYRO,
    }

    public Queue<Segment> segments;
    protected int encoderTicksPerInch;
    protected double encoderTicksPerDegree;
    protected GyroSensor gyro;
    protected DcMotor masterMotor;
    protected Segment currSegment;
    protected Robot robot;
    protected boolean turning;
    protected boolean setup;
    protected int lastHeading;
    protected int target;
    protected TurnKind turnKind;
    protected PersistentTelemetryTask ptt;

    public class Segment {

        public SegmentType type;
        public SegmentState state;
        public double distance;
        public double speed;

        Segment(SegmentType type, double distance, double speed)
        {
            this.state = SegmentState.INITIALIZE;
            this.distance = distance;
            this.type = type;
            this.speed = speed;
        }
    }

    /*
     * The abstract functions are provided by the bot.
     */
    protected abstract void resetEncoders();
    protected abstract void encodersOn();
    protected abstract void motorStraight(double speed);
    protected abstract void motorTurn(double speed);
    protected abstract void motorSideways(double speed);
    protected abstract void motorStop();

    protected abstract boolean isBusy();

    public DeadReckon(Robot robot, int encoderTicksPerInch, GyroSensor gyro, DcMotor masterMotor)
    {
        this.encoderTicksPerInch = encoderTicksPerInch;
        this.encoderTicksPerDegree = 0;
        this.gyro = gyro;
        this.masterMotor = masterMotor;
        this.robot = robot;
        this.currSegment = null;
        this.turning = false;
        this.setup = false;
        this.lastHeading = 0;
        this.turnKind = TurnKind.TURN_USING_GYRO;
        segments = new LinkedList<Segment>();
        ptt = new PersistentTelemetryTask(robot);
        robot.addTask(ptt);
    }

    public DeadReckon(Robot robot, int encoderTicksPerInch, double encoderTicksPerDegree, DcMotor masterMotor)
    {
        this.encoderTicksPerInch = encoderTicksPerInch;
        this.encoderTicksPerDegree = encoderTicksPerDegree;
        this.gyro = gyro;
        this.masterMotor = masterMotor;
        this.robot = robot;
        this.currSegment = null;
        this.turning = false;
        this.setup = false;
        this.lastHeading = 0;
        this.turnKind = TurnKind.TURN_USING_ENCODERS;
        segments = new LinkedList<Segment>();
        ptt = new PersistentTelemetryTask(robot);
        robot.addTask(ptt);
    }

    public void addSegment(SegmentType type, double distance, double speed)
    {
        segments.add(new Segment(type, distance, speed));
    }

    public void setTarget()
    {
        if (getCurrentSegment().type == SegmentType.STRAIGHT) {
            this.target = Math.abs((int)(getCurrentSegment().distance * encoderTicksPerInch));
        } else if (getCurrentSegment().type == SegmentType.SIDEWAYS) {
            // Eventually, we may have a encoder ticks per (sideways) inch... but for now, this:
            this.target = Math.abs((int)(getCurrentSegment().distance * encoderTicksPerInch));
        } else {
            this.target = Math.abs((int)(getCurrentSegment().distance * encoderTicksPerDegree));
        }
        ptt.addData("Target: ", this.target);
    }

    public boolean areEncodersReset()
    {
        if (masterMotor.getCurrentPosition() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public boolean hitTarget()
    {
        int position;

        position = Math.abs(masterMotor.getCurrentPosition());
        ptt.addData("Position: ", position);
        if (position >= target) {
            return true;
        } else {
            return false;
        }
    }

    public void nextSegment()
    {
        segments.remove();
    }

    public Segment getCurrentSegment()
    {
        return segments.peek();
    }

    public void stop()
    {
        /*
         * Remove all remaining segments and stop the motors.
         */
        segments.clear();
        motorStop();
        resetEncoders();
    }
}

