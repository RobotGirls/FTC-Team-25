package team25core;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

/*
 * FTC Team 25: cmacfarl, September 26, 2015
 */

public class DeadmanMotorTask extends RobotTask {

    public enum DeadmanButton {
        BUTTON_A,
        BUTTON_B,
        BUTTON_X,
        BUTTON_Y,
        LEFT_BUMPER,
        RIGHT_BUMPER,
        LEFT_TRIGGER,
        RIGHT_TRIGGER,
    };

    GamepadTask.GamepadNumber gamepad;
    DcMotor motor;
    double power;
    DeadmanButton button;
    boolean done;

    public DeadmanMotorTask(Robot robot, DcMotor motor, double power, GamepadTask.GamepadNumber gamepad, DeadmanButton button)
    {
        super(robot);

        this.gamepad = gamepad;
        this.button = button;
        this.power = power;
        done = false;
    }

    protected boolean isButtonTracked(GamepadTask.EventKind kind)
    {
        if ((kind == GamepadTask.EventKind.BUTTON_A_DOWN) || (kind == GamepadTask.EventKind.BUTTON_A_UP) && (button == DeadmanButton.BUTTON_A)) {
            return true;
        } else if ((kind == GamepadTask.EventKind.BUTTON_B_DOWN) || (kind == GamepadTask.EventKind.BUTTON_B_UP) && (button == DeadmanButton.BUTTON_B)) {
            return true;
        } else if ((kind == GamepadTask.EventKind.BUTTON_X_DOWN) || (kind == GamepadTask.EventKind.BUTTON_X_UP) && (button == DeadmanButton.BUTTON_X)) {
            return true;
        } else if ((kind == GamepadTask.EventKind.BUTTON_Y_DOWN) || (kind == GamepadTask.EventKind.BUTTON_Y_UP) && (button == DeadmanButton.BUTTON_Y)) {
            return true;
        } else if ((kind == GamepadTask.EventKind.LEFT_BUMPER_DOWN) || (kind == GamepadTask.EventKind.LEFT_BUMPER_UP) && (button == DeadmanButton.LEFT_BUMPER)) {
            return true;
        } else if ((kind == GamepadTask.EventKind.RIGHT_BUMPER_DOWN) || (kind == GamepadTask.EventKind.RIGHT_BUMPER_UP) && (button == DeadmanButton.RIGHT_BUMPER)) {
            return true;
        } else if ((kind == GamepadTask.EventKind.LEFT_TRIGGER_DOWN) || (kind == GamepadTask.EventKind.LEFT_TRIGGER_UP) && (button == DeadmanButton.LEFT_TRIGGER)) {
            return true;
        } else if ((kind == GamepadTask.EventKind.RIGHT_TRIGGER_DOWN) || (kind == GamepadTask.EventKind.RIGHT_TRIGGER_UP) && (button == DeadmanButton.RIGHT_TRIGGER)) {
            return true;
        } else {
            return false;
        }
    }

    protected void toggleMotor(GamepadTask.EventKind kind)
    {
        switch (kind) {
        case BUTTON_A_DOWN:
        case BUTTON_B_DOWN:
        case BUTTON_X_DOWN:
        case BUTTON_Y_DOWN:
        case LEFT_BUMPER_DOWN:
        case RIGHT_BUMPER_DOWN:
            motor.setPower(power);
            break;
        case BUTTON_A_UP:
        case BUTTON_B_UP:
        case BUTTON_X_UP:
        case BUTTON_Y_UP:
        case LEFT_BUMPER_UP:
        case RIGHT_BUMPER_UP:
            motor.setPower(0.0);
            break;
        }
    }

    @Override
    public void start()
    {
        robot.addTask(new GamepadTask(robot, gamepad) {
            @Override
            public void handleEvent(RobotEvent e)
            {
                GamepadEvent event = (GamepadEvent)e;

                if (isButtonTracked(event.kind)) {
                    toggleMotor(event.kind);
                }
            }
        });
    }

    @Override
    public void stop()
    {
        motor.setPower(0.0);
        robot.removeTask(this);
    }

    @Override
    public boolean timeslice() {
        return done;
    }
}
