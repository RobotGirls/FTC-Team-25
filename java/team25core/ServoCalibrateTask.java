package team25core;

/*
 * FTC Team 25: cmacfarl, December 05, 2015
 */

import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

public class ServoCalibrateTask extends RobotTask {

    protected GamepadTask gamepadTask;
    protected ElapsedTime timer;
    protected int position;
    protected int direction;
    protected Servo servo;

    protected final static int speed = 500;

    public ServoCalibrateTask(Robot robot, Servo servo, int startPoint)
    {
        super(robot);
        this.position = startPoint;
        this.direction = 1;
        this.servo = servo;
    }

    public ServoCalibrateTask(Robot robot, Servo servo)
    {
        super(robot);
        this.position = 128;
        this.direction = 1;
        this.servo = servo;
    }

    @Override
    public void start()
    {
        timer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);

        gamepadTask = new GamepadTask(robot, GamepadTask.GamepadNumber.GAMEPAD_1) {
            @Override
            public void handleEvent(RobotEvent e) {
                GamepadEvent ge = (GamepadEvent)e;

                if (ge.kind == EventKind.BUTTON_X_DOWN) {
                    direction = -1;
                } else if (ge.kind == EventKind.BUTTON_B_DOWN) {
                    direction = 1;
                } else if (ge.kind == EventKind.BUTTON_Y_DOWN) {
                    direction = 0;
                }
            }
        };
    }

    @Override
    public void stop() { }

    @Override
    public boolean timeslice()
    {
        if (timer.time() > speed) {
            if (position >= 256) {
                direction = -1;
            } else if (position <= 0) {
                direction = 1;
            }
            position += direction;
            servo.setPosition(position);
        }
        robot.telemetry.addData("Position: ", position);
        return false;
    }
}
