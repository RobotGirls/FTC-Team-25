package team25core;

/*
 * FTC Team 5218: izzielau, October 6, 2015
 */

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class ServoOscillateTask extends RobotTask {

    protected enum ServoState {
        INITIAL,
        ENDPOINT,
    }

    ServoState state;

    protected ElapsedTime timer;

    private int TIME = 1251;

    private int position;
    private int range;
    private int endpoint;

    private Servo servo;

    public ServoOscillateTask(Robot robot, Servo servo, int startPoint, int range)
    {
        super(robot);
        this.position = startPoint;
        this.range = range;
        this.servo = servo;
        if ((startPoint + range) > 255) {
            this.endpoint = startPoint - range;
        } else {
            this.endpoint = startPoint + range;
        }
    }

    @Override
    public void start() {
        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    }

    @Override
    public void stop() {

    }

    public void stop(float end) {
        servo.setPosition(end/256.0);
        robot.removeTask(this);
    }

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public boolean timeslice() {
        if (timer.time() > TIME) {
            if (state == ServoState.INITIAL) {
                state = ServoState.ENDPOINT;
                servo.setPosition(endpoint);
            } else {
                state = ServoState.INITIAL;
                servo.setPosition(position);
            }
            timer.reset();
        }

        robot.telemetry.addData("Position: ", position);
        return false;
    }
}
