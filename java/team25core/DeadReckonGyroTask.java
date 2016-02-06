package team25core;

/*
 * FTC Team 25: cmacfarl, September 01, 2015
 */

import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.robotcore.util.RobotLog;

public class DeadReckonGyroTask extends DeadReckonTask {

    ModernRoboticsI2cGyro gyro;

    public DeadReckonGyroTask(Robot robot, ModernRoboticsI2cGyro gyro, DeadReckon dr)
    {
        super(robot, dr);
        this.gyro = gyro;
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
            robot.queueEvent(new DeadReckonTask.DeadReckonEvent(this, DeadReckonTask.EventKind.PATH_DONE, num));
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
             * About 1/2 a second give or take, just insure we are stopped before moving on.
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
