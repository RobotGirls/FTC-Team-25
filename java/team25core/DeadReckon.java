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

    protected void consumeSegment()
    {
        if (currSegment.type == SegmentType.STRAIGHT) {
            RobotLog.i("251 Moving straight");
            motorStraight(currSegment.speed);
        } else {
            turning = true;
            target = lastHeading + (int)currSegment.distance;
            lastHeading = target;
            RobotLog.i("251 Turning " + currSegment.distance + " degrees " + " to target " + target);
            robot.addTask(new GyroTask(robot, gyro, target, false) {
                @Override
                public void handleEvent(RobotEvent ev) {
                    GyroEvent event = (GyroEvent) ev;
                    double speed;

                    if (event.kind == EventKind.HIT_TARGET || event.kind == EventKind.PAST_TARGET) {
                        turning = false;
                        RobotLog.i("251 Turned " + gyro.getHeading() + "/" + currSegment.distance);
                        motorStop();
                    } else if (event.kind == EventKind.THRESHOLD_80) {
                        // motorTurn(Math.max(0.20, currSegment.speed * 0.10));
                    } else if (event.kind == EventKind.THRESHOLD_90) {
                        // motorTurn(Math.max(0.15, currSegment.speed * 0.02));
                    } else if (event.kind == EventKind.ERROR_UPDATE) {
                        double e = Math.exp(1.0);
                        double logVal = Math.pow(e, (5.6 * (event.val / Math.abs(currSegment.distance))));
                        if (currSegment.distance < 0) {
                            speed = -(logVal / 100) - 0.01;
                        } else {
                            speed = (logVal / 100) + 0.01;
                        }
                        motorTurn(speed);
                    }
                }
            });
            motorTurn(currSegment.speed);
        }
    }

    protected void setupSegment()
    {
        motorStop();

        currSegment = segments.poll();
        setup = true;

        if (currSegment.type == SegmentType.STRAIGHT) {
            RobotLog.i("251 Setting up straight segment: " + foo);
            ResetMotorEncoderTask rmt = new ResetMotorEncoderTask(this.robot, masterMotor) {
                @Override
                public void handleEvent(RobotEvent e)
                {
                    RobotLog.i("251 Encoder reset done, consuming segment:" + foo);
                    resetEncoders();
                    setup = false;
                    consumeSegment();
                }
            };
            this.robot.addTask(rmt);
        } else {
            RobotLog.i("251 Setting up turn segment: " + foo);
            setup = false;
            consumeSegment();
            /*
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
            */
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
            motorStop();
        }
        return false;
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

    boolean done()
    {
        boolean segmentEmpty = segments.isEmpty();
        boolean busy = consumingSegment();

        return (segmentEmpty && !busy);
    }
}

