package com.qualcomm.ftcrobotcontroller.opmodes;

/*
 * FTC Team 25: cmacfarl, August 31, 2015
 *
 * Use to calibrate a single servo.  When the task runs it will move
 * a servo to the servo midpoint and start incrementing the servo position.
 *
 * Call reverse to reverse the direction of travel.  The servo will travel
 * to the start/end point and then stop at which point this task is done
 * and will not run anymore unless added back onto the robot's task queue
 * by the main robot.
 */

import com.qualcomm.modernrobotics.ModernRoboticsMatrixServoController;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.RateLimit;

public class ServoCalibrateTask implements RobotTask {

    public enum EventKind {
        SERVO_START,
        SERVO_DONE,
    }

    protected enum Direction {
        FORWARD,
        BACKWARD,
    }

    public class ServoEvent extends RobotEvent {

        EventKind kind;
        int currentPos;

        public ServoEvent(Robot r, EventKind k, int currentPos)
        {
            super(r);
            kind = k;
            this.currentPos = currentPos;
        }

        @Override
        public String toString()
        {
            return (super.toString() + "Servo Event " + kind + " " + currentPos);
        }
    }

    protected int startPos;
    protected int endPos;
    protected int currentPos;
    protected Robot robot;
    protected Servo servo;
    protected Direction direction;

    protected RateLimit rl = new RateLimit(100) {
        @Override
        protected void execute() {
            servo.setPosition(((double)currentPos/250));
        }
    };

    public ServoCalibrateTask(Robot robot, Servo servo, int startPos, int endPos)
    {
        this.servo = servo;
        this.startPos = startPos;
        this.endPos = endPos;
        this.robot = robot;
        this.direction = Direction.FORWARD;
    }

    public void reverse()
    {
        switch (direction) {
        case FORWARD:
            direction = Direction.BACKWARD;
            break;
        case BACKWARD:
            direction = Direction.FORWARD;
            break;
        }
    }

    @Override
    public void start()
    {
        currentPos = (endPos - startPos) / 2;
        robot.queueEvent(new ServoEvent(robot, EventKind.SERVO_START, currentPos));
    }

    /*
     * Perform any cleanup work on the task.
     *
     * stop() must remove the task from the robot's task queue when finished.
     */
    @Override
    public void stop()
    {
        robot.queueEvent(new ServoEvent(robot, EventKind.SERVO_DONE, currentPos));
        robot.removeTask(this);
    }

    /*
     * Return false to keep this task in the queue, true to remove it.
     */
    @Override
    public boolean timeslice()
    {
        if (rl.run()) {
            switch (direction) {
            case FORWARD:
                if (currentPos == endPos) {
                    return true;
                } else {
                    currentPos++;
                }
                break;
            case BACKWARD:
                if (currentPos == startPos) {
                    return true;
                } else {
                    currentPos--;
                }
                break;
            }
        }
        return false;
    }
}
