package team25core;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.util.RobotLog;

import org.swerverobotics.library.interfaces.Autonomous;

import opmodes.NeverlandLightConstants;

/*
 * FTC Team 5218: izzielau, February 18, 2016
 */
public class LineFollowerTask extends RobotTask {

    protected double lightValue;
    protected double inches;

    // Proportional variables.
    protected int raw;
    protected double lightError;
    protected double adjustedError;
    protected double PROPORTIONAL_K = .7;
    protected double BASE_POWER = 0.5;

    protected boolean zigzag;
    protected int ticksPerInch;

    protected LightSensor light;
    protected DcMotor left;
    protected DcMotor right;

    protected int IDEAL_VALUE = 25;
    protected int MAXIMUM = NeverlandLightConstants.LIGHT_MAXIMUM;
    protected int MINIMUM = NeverlandLightConstants.LIGHT_MINIMUM;

    public enum EventKind {
        FOLLOWING,
        DONE,
        ERROR,
    }

    public class LineFollowerEvent extends RobotEvent {
        public EventKind kind;
        public int val;

        public LineFollowerEvent(RobotTask task, EventKind kind) {
            super(task);
            this.kind = kind;
            this.val = 0;
        }
    }

    public LineFollowerTask(Robot robot, LightSensor light, DcMotor left, DcMotor right, int ticksInch, double inches, boolean zigzag)
    {
        super(robot);
        this.light = light;
        this.inches = inches;
        this.zigzag = zigzag;
        this.ticksPerInch = ticksInch;
        this.left = left;
        this.right = right;
    }

    @Override
    public void start()
    {
        RobotLog.i("251 LF Starting line follower task");
        left.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        right.setMode(DcMotorController.RunMode.RESET_ENCODERS);
        left.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);
        right.setMode(DcMotorController.RunMode.RUN_USING_ENCODERS);

        left.setDirection(DcMotor.Direction.REVERSE);
    }

    @Override
    public void stop()
    {

    }

    @Override
    public boolean timeslice() {
        raw = light.getLightDetectedRaw();
        lightValue = (1 - ((raw - MINIMUM)/(MAXIMUM - MINIMUM))) * 100;

        if (zigzag) {
            // Follows the left side of the line.
            if (left.getCurrentPosition() < inches * ticksPerInch) {
                RobotLog.i("251 LF Status: FOLLOWING");
                RobotLog.i("251 LF Target: %d, Raw: %d", ((int)(inches * ticksPerInch)), raw);
                RobotLog.i("251 LF Light value: %d", ((int)lightValue));
                if (lightValue > IDEAL_VALUE) {
                    RobotLog.i("251 LF Moving LEFT");
                    left.setPower(0.35);
                    right.setPower(0.15);
                } else {
                    RobotLog.i("251 LF Moving RIGHT");
                    left.setPower(0.15);
                    right.setPower(0.35);
                }
                return false;
            } else if (left.getCurrentPosition() >= inches * ticksPerInch) {
                RobotLog.i("251 LF Status: TARGET MET");

                LineFollowerEvent done = new LineFollowerEvent(this, EventKind.DONE);
                robot.queueEvent(done);

                left.setPower(0.0);
                right.setPower(0.0);

                return true;
            }
        } else if (!zigzag) {
            // Proportional line follower.
            if (left.getCurrentPosition() < inches * ticksPerInch) {
                lightError = IDEAL_VALUE - lightValue;
                adjustedError = (lightError * PROPORTIONAL_K);

                right.setPower(BASE_POWER + adjustedError);
                left.setPower(BASE_POWER - adjustedError);

                LineFollowerEvent following = new LineFollowerEvent(this, EventKind.FOLLOWING);
                robot.queueEvent(following);
            } else if (left.getCurrentPosition() >= inches * ticksPerInch) {
                LineFollowerEvent done = new LineFollowerEvent(this, EventKind.DONE);
                robot.queueEvent(done);

                left.setPower(0.0);
                right.setPower(0.0);

                return true;
            }
        }
        return false;
    }
}
