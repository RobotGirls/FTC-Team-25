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

    public static int speed = 251;

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
        robot.addTask(gamepadTask);
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean timeslice()
    {
        // Toggle speed.
        robot.addTask(new GamepadTask(robot, GamepadTask.GamepadNumber.GAMEPAD_1) {
            public void handleEvent(RobotEvent e)
            {
                GamepadEvent event = (GamepadEvent) e;

                if (event.kind == EventKind.LEFT_BUMPER_DOWN) {
                    speed = 100;
                } else if (event.kind == EventKind.LEFT_TRIGGER_DOWN) {
                    speed = 500;
                }
            }
        });

        if (timer.time() > speed) {
            if (position >= 256) {
                direction = -1;
            } else if (position <= 0) {
                direction = 1;
            }

            position += direction;
            servo.setPosition((float)position/(float)256.0);
            timer.reset();
        }
        robot.telemetry.addData("Position: ", position);
        return false;
    }
}
