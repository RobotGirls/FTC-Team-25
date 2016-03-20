package team25core;

/*
 * FTC Team 5218: izzielau, October 6, 2015
 */

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class ServoOscillateTask extends RobotTask {

    protected ElapsedTime timer;

    private int TIME = 888;

    private int count = 0;
    private int direction;
    private int position;
    private int range;

    private Servo servo;

    public ServoOscillateTask(Robot robot, Servo servo, int startPoint, int range)
    {
        super(robot);
        this.position = startPoint;
        this.range = range;
        this.servo = servo;
    }

    @Override
    public void start() {
        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    }

    @Override
    public void stop() {
        robot.removeTask(this);
    }

    @Override
    public void handleEvent(RobotEvent e) {

    }

    @Override
    public boolean timeslice() {
        if (timer.time() > TIME) {
            if ((count % 2) == 0) {
                // If count is odd, move negative.
                direction = -range;
            } else if ((count % 2) == 1){
                // If count is even, move positive.
                direction = range;
            }

            position += direction;
            servo.setPosition((float)position/(float)256.0);
            timer.reset();
        }

        count++;
        robot.telemetry.addData("Position: ", position);
        return false;
    }
}
